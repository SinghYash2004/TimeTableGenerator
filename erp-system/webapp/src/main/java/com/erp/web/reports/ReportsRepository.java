package com.erp.web.reports;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportsRepository {
    private final JdbcTemplate jdbcTemplate;

    public ReportsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String latestSemester() {
        String semester = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(semester), '2026S1') FROM timetable", String.class);
        return semester == null ? "2026S1" : semester;
    }

    public List<ConflictView> loadConflicts(String semester, Integer departmentId) {
        String deptFilter = (departmentId == null || departmentId <= 0) ? "" : " AND t.department_id = ?";
        Object[] args = (departmentId == null || departmentId <= 0)
                ? new Object[]{semester, semester, semester}
                : new Object[]{semester, departmentId, semester, departmentId, semester, departmentId};

        String sql =
                "SELECT 'FACULTY' AS type, d.name AS department_name, t.slot_id, ts.day, ts.period, f.name AS entity_name, COUNT(*) AS cnt " +
                        "FROM timetable t " +
                        "JOIN faculty f ON f.faculty_id = t.faculty_id " +
                        "JOIN department d ON d.department_id = t.department_id " +
                        "JOIN timeslot ts ON ts.slot_id = t.slot_id " +
                        "WHERE t.semester = ? " + deptFilter +
                        "GROUP BY t.slot_id, t.faculty_id, d.name, ts.day, ts.period HAVING COUNT(*) > 1 " +
                        "UNION ALL " +
                        "SELECT 'ROOM' AS type, d.name AS department_name, t.slot_id, ts.day, ts.period, c.room_code AS entity_name, COUNT(*) AS cnt " +
                        "FROM timetable t " +
                        "JOIN classroom c ON c.room_id = t.room_id " +
                        "JOIN department d ON d.department_id = t.department_id " +
                        "JOIN timeslot ts ON ts.slot_id = t.slot_id " +
                        "WHERE t.semester = ? " + deptFilter +
                        "GROUP BY t.slot_id, t.room_id, d.name, ts.day, ts.period HAVING COUNT(*) > 1 " +
                        "UNION ALL " +
                        "SELECT 'SECTION' AS type, d.name AS department_name, t.slot_id, ts.day, ts.period, sec.section_name AS entity_name, COUNT(*) AS cnt " +
                        "FROM timetable t " +
                        "JOIN section sec ON sec.section_id = t.section_id " +
                        "JOIN department d ON d.department_id = t.department_id " +
                        "JOIN timeslot ts ON ts.slot_id = t.slot_id " +
                        "WHERE t.semester = ? " + deptFilter +
                        "GROUP BY t.slot_id, t.section_id, d.name, ts.day, ts.period HAVING COUNT(*) > 1 " +
                        "ORDER BY day, period, type";
        return jdbcTemplate.query(sql, (rs, i) -> new ConflictView(
                rs.getString("type"),
                rs.getString("department_name"),
                rs.getInt("slot_id"),
                rs.getString("day"),
                rs.getInt("period"),
                rs.getString("entity_name"),
                rs.getInt("cnt")
        ), args);
    }

    public List<FacultyOverloadView> loadFacultyOverloads(String semester, Integer departmentId) {
        String deptFilter = (departmentId == null || departmentId <= 0) ? "" : " AND f.department_id = ?";
        Object[] args = (departmentId == null || departmentId <= 0)
                ? new Object[]{semester}
                : new Object[]{semester, departmentId};
        String sql = "SELECT f.name AS faculty_name, d.name AS department_name, f.max_hours_per_week, COUNT(t.id) AS assigned " +
                "FROM faculty f " +
                "JOIN department d ON d.department_id = f.department_id " +
                "LEFT JOIN timetable t ON t.faculty_id = f.faculty_id AND t.semester = ? " +
                "WHERE 1=1 " + deptFilter +
                "GROUP BY f.faculty_id, f.name, d.name, f.max_hours_per_week " +
                "HAVING COUNT(t.id) > f.max_hours_per_week " +
                "ORDER BY assigned DESC";
        return jdbcTemplate.query(sql, (rs, i) -> new FacultyOverloadView(
                rs.getString("faculty_name"),
                rs.getString("department_name"),
                rs.getInt("assigned"),
                rs.getInt("max_hours_per_week")
        ), args);
    }

    public List<RoomUtilizationView> loadRoomUtilization(String semester, Integer departmentId) {
        String deptFilter = (departmentId == null || departmentId <= 0) ? "" : " AND t.department_id = ?";
        Object[] args = (departmentId == null || departmentId <= 0)
                ? new Object[]{semester}
                : new Object[]{semester, departmentId};
        Integer totalSlots = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM timeslot", Integer.class);
        int total = totalSlots == null ? 0 : totalSlots;

        String sql = "SELECT c.room_code, COUNT(t.id) AS assigned " +
                "FROM classroom c " +
                "LEFT JOIN timetable t ON t.room_id = c.room_id AND t.semester = ? " + deptFilter +
                "GROUP BY c.room_id, c.room_code ORDER BY assigned DESC";
        return jdbcTemplate.query(sql, (rs, i) -> {
            int assigned = rs.getInt("assigned");
            double percent = total <= 0 ? 0.0 : (assigned * 100.0) / total;
            return new RoomUtilizationView(
                    rs.getString("room_code"),
                    assigned,
                    total,
                    percent
            );
        }, args);
    }
}
