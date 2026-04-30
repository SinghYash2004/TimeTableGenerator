package com.erp.web.dashboard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepository {
    private final JdbcTemplate jdbcTemplate;

    public DashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardSummary loadSummary() {
        String semester = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(semester), '2026S1') FROM timetable", String.class);
        if (semester == null) {
            semester = "2026S1";
        }

        Integer departments = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM department", Integer.class);
        Integer faculty = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM faculty", Integer.class);
        Integer subjects = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subject", Integer.class);
        Integer timetable = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM timetable WHERE semester = ?", Integer.class, semester);

        int facultyCount = faculty == null ? 0 : faculty;
        int timetableCount = timetable == null ? 0 : timetable;

        Integer overloadedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" +
                        "SELECT f.faculty_id, f.max_hours_per_week, COUNT(t.id) AS assigned " +
                        "FROM faculty f LEFT JOIN timetable t ON t.faculty_id = f.faculty_id AND t.semester = CAST(? AS VARCHAR) " +
                        "GROUP BY f.faculty_id, f.max_hours_per_week " +
                        "HAVING COUNT(t.id) > f.max_hours_per_week" +
                        ") x",
                Integer.class,
                semester
        );
        double overloadPercent = facultyCount <= 0 ? 0 : ((overloadedCount == null ? 0 : overloadedCount) * 100.0 / facultyCount);

        Integer roomCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM classroom", Integer.class);
        Integer slotCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM timeslot", Integer.class);
        double roomUtil = 0;
        if (roomCount != null && slotCount != null && roomCount > 0 && slotCount > 0) {
            roomUtil = (timetableCount * 100.0) / (roomCount * slotCount);
        }

        Double totalBudget = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(budget_limit), 0) FROM department", Double.class);
        Double timetableCost = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(f.cost_per_hour + c.cost_per_hour), 0) " +
                        "FROM timetable t " +
                        "JOIN faculty f ON f.faculty_id = t.faculty_id " +
                        "JOIN classroom c ON c.room_id = t.room_id " +
                        "WHERE t.semester = ?",
                Double.class,
                semester
        );
        double budgetUsage = (totalBudget == null || totalBudget <= 0) ? 0 : ((timetableCost == null ? 0 : timetableCost) * 100.0 / totalBudget);

        Integer facultyConflicts = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(cnt - 1), 0) FROM (" +
                        "SELECT slot_id, faculty_id, COUNT(*) AS cnt FROM timetable WHERE semester = ? " +
                        "GROUP BY slot_id, faculty_id HAVING COUNT(*) > 1" +
                        ") x",
                Integer.class,
                semester
        );
        Integer roomConflicts = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(cnt - 1), 0) FROM (" +
                        "SELECT slot_id, room_id, COUNT(*) AS cnt FROM timetable WHERE semester = ? " +
                        "GROUP BY slot_id, room_id HAVING COUNT(*) > 1" +
                        ") x",
                Integer.class,
                semester
        );
        Integer sectionConflicts = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(cnt - 1), 0) FROM (" +
                        "SELECT slot_id, section_id, COUNT(*) AS cnt FROM timetable WHERE semester = ? " +
                        "GROUP BY slot_id, section_id HAVING COUNT(*) > 1" +
                        ") x",
                Integer.class,
                semester
        );
        int totalConflicts = (facultyConflicts == null ? 0 : facultyConflicts)
                + (roomConflicts == null ? 0 : roomConflicts)
                + (sectionConflicts == null ? 0 : sectionConflicts);
        int aiScore = (int) Math.max(0, 100 - totalConflicts * 2 - Math.round(overloadPercent / 2f));

        return new DashboardSummary(
                totalConflicts,
                overloadPercent,
                roomUtil,
                budgetUsage,
                aiScore,
                departments == null ? 0 : departments,
                facultyCount,
                subjects == null ? 0 : subjects,
                timetableCount
        );
    }
}
