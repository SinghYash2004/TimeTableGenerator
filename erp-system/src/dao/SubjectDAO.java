package dao;

import config.DBConnection;
import model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    public void save(Subject subject) throws Exception {
        String sql = "INSERT INTO subject(name, weekly_hours, department_id) VALUES(?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE weekly_hours=VALUES(weekly_hours)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getName());
            ps.setInt(2, subject.getWeeklyHours());
            ps.setInt(3, subject.getDepartmentId());
            ps.executeUpdate();
        }
    }

    public List<Subject> getByDepartment(int departmentId) throws Exception {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT subject_id, name, weekly_hours, department_id FROM subject WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Subject(
                            rs.getInt("subject_id"),
                            rs.getString("name"),
                            rs.getInt("weekly_hours"),
                            rs.getInt("department_id")
                    ));
                }
            }
        }
        return list;
    }

    public List<Subject> getAll() throws Exception {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT subject_id, name, weekly_hours, department_id FROM subject ORDER BY subject_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("subject_id"),
                        rs.getString("name"),
                        rs.getInt("weekly_hours"),
                        rs.getInt("department_id")
                ));
            }
        }
        return list;
    }

    public void update(int subjectId, String name, int weeklyHours, int departmentId) throws Exception {
        String sql = "UPDATE subject SET name=?, weekly_hours=?, department_id=? WHERE subject_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, weeklyHours);
            ps.setInt(3, departmentId);
            ps.setInt(4, subjectId);
            ps.executeUpdate();
        }
    }

    public void delete(int subjectId) throws Exception {
        String sql = "DELETE FROM subject WHERE subject_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.executeUpdate();
        }
    }
}
