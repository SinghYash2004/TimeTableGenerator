package com.erp.web.timetable;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TimetableRepository {
    private final JdbcTemplate jdbcTemplate;

    public TimetableRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TimetableRowView> findBySemesterAndDepartment(String semester, int departmentId) {
        String sql = "SELECT t.id, t.semester, t.department_id, d.name AS department_name, COALESCE(sec.section_id, 0) AS section_id, " +
                "COALESCE(sec.section_name, '-') AS section_name, " +
                "t.subject_id, ts.day, ts.period, s.name AS subject_name, " +
                "t.faculty_id, f.name AS faculty_name, t.room_id, c.room_code, t.slot_id " +
                ", CASE WHEN EXISTS (" +
                "   SELECT 1 FROM timetable x WHERE x.semester=t.semester AND x.slot_id=t.slot_id AND x.faculty_id=t.faculty_id AND x.id<>t.id" +
                "  ) THEN 1 ELSE 0 END AS faculty_conflict " +
                ", CASE WHEN EXISTS (" +
                "   SELECT 1 FROM timetable x WHERE x.semester=t.semester AND x.slot_id=t.slot_id AND x.room_id=t.room_id AND x.id<>t.id" +
                "  ) THEN 1 ELSE 0 END AS room_conflict " +
                ", CASE WHEN EXISTS (" +
                "   SELECT 1 FROM timetable x WHERE x.semester=t.semester AND x.slot_id=t.slot_id AND x.section_id=t.section_id AND x.id<>t.id" +
                "  ) THEN 1 ELSE 0 END AS section_conflict " +
                ", CASE WHEN (" +
                "   SELECT COUNT(*) FROM timetable x WHERE x.semester=t.semester AND x.faculty_id=t.faculty_id" +
                "  ) > f.max_hours_per_week THEN 1 ELSE 0 END AS overload_warning " +
                ", CASE WHEN EXISTS (" +
                "   SELECT 1 FROM class_cancellation cc WHERE cc.timetable_id=t.id" +
                "  ) THEN 1 ELSE 0 END AS canceled_any " +
                "FROM timetable t " +
                "JOIN department d ON d.department_id = t.department_id " +
                "JOIN subject s ON s.subject_id = t.subject_id " +
                "JOIN faculty f ON f.faculty_id = t.faculty_id " +
                "JOIN classroom c ON c.room_id = t.room_id " +
                "JOIN timeslot ts ON ts.slot_id = t.slot_id " +
                "LEFT JOIN section sec ON sec.section_id = t.section_id " +
                "WHERE t.semester = ? AND t.department_id = ? " +
                "ORDER BY ts.day, ts.period, sec.section_name, s.name";
        return jdbcTemplate.query(sql, (rs, i) -> new TimetableRowView(
                rs.getInt("id"),
                rs.getString("semester"),
                rs.getString("department_name"),
                rs.getInt("department_id"),
                rs.getInt("section_id"),
                rs.getString("section_name"),
                rs.getInt("subject_id"),
                rs.getString("day"),
                rs.getInt("period"),
                rs.getString("subject_name"),
                rs.getInt("faculty_id"),
                rs.getString("faculty_name"),
                rs.getInt("room_id"),
                rs.getString("room_code"),
                rs.getInt("slot_id"),
                rs.getInt("faculty_conflict") == 1,
                rs.getInt("room_conflict") == 1,
                rs.getInt("section_conflict") == 1,
                rs.getInt("overload_warning") == 1,
                rs.getInt("canceled_any") == 1
        ), semester, departmentId);
    }

    public void insert(String semester, int facultyId, int subjectId, int roomId, int slotId, int departmentId, Integer sectionId) {
        String sql = "INSERT INTO timetable(semester, faculty_id, subject_id, room_id, slot_id, department_id, section_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, semester, facultyId, subjectId, roomId, slotId, departmentId, sectionId);
    }

    public void update(
            int entryId,
            String semester,
            int facultyId,
            int subjectId,
            int roomId,
            int slotId,
            int departmentId,
            Integer sectionId
    ) {
        String sql = "UPDATE timetable SET semester=?, faculty_id=?, subject_id=?, room_id=?, slot_id=?, department_id=?, section_id=? " +
                "WHERE id=?";
        jdbcTemplate.update(sql, semester, facultyId, subjectId, roomId, slotId, departmentId, sectionId, entryId);
    }

    public void delete(int entryId) {
        String sql = "DELETE FROM timetable WHERE id=?";
        jdbcTemplate.update(sql, entryId);
    }

    public Integer findFacultyIdForEntry(int entryId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT faculty_id FROM timetable WHERE id=?",
                    Integer.class,
                    entryId
            );
        } catch (Exception ex) {
            return null;
        }
    }
}
