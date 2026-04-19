package dao;

import config.DBConnection;
import model.TimetableEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public void clearBySemesterAndDepartment(String semester, int departmentId) throws Exception {
        String sql = "DELETE FROM timetable WHERE semester = ? AND department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setInt(2, departmentId);
            ps.executeUpdate();
        }
    }

    public void saveAll(List<TimetableEntry> entries) throws Exception {
        String sql = "INSERT INTO timetable(semester, faculty_id, subject_id, room_id, slot_id, department_id, section_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (TimetableEntry entry : entries) {
                ps.setString(1, entry.getSemester());
                ps.setInt(2, entry.getFacultyId());
                ps.setInt(3, entry.getSubjectId());
                ps.setInt(4, entry.getRoomId());
                ps.setInt(5, entry.getSlotId());
                ps.setInt(6, entry.getDepartmentId());
                ps.setInt(7, entry.getSectionId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<TimetableEntry> getBySemesterAndDepartment(String semester, int departmentId) throws Exception {
        List<TimetableEntry> entries = new ArrayList<>();
        String sql = "SELECT id, semester, faculty_id, subject_id, room_id, slot_id, department_id, section_id " +
                "FROM timetable WHERE semester = ? AND department_id = ? ORDER BY slot_id, section_id, room_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setInt(2, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new TimetableEntry(
                            rs.getInt("id"),
                            rs.getString("semester"),
                            rs.getInt("faculty_id"),
                            rs.getInt("subject_id"),
                            rs.getInt("room_id"),
                            rs.getInt("slot_id"),
                            rs.getInt("department_id"),
                            rs.getInt("section_id")
                    ));
                }
            }
        }
        return entries;
    }
}
