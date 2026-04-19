package dao;

import config.DBConnection;
import model.Faculty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FacultyDAO {

    public void save(Faculty faculty) throws Exception {
        String sql = "INSERT INTO faculty(name, department_id, max_hours_per_week, cost_per_hour) " +
                "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE max_hours_per_week=VALUES(max_hours_per_week), cost_per_hour=VALUES(cost_per_hour)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, faculty.getName());
            ps.setInt(2, faculty.getDepartmentId());
            ps.setInt(3, faculty.getMaxHoursPerWeek());
            ps.setDouble(4, faculty.getCostPerHour());
            ps.executeUpdate();
        }
    }

    public List<Faculty> getByDepartment(int departmentId) throws Exception {
        List<Faculty> list = new ArrayList<>();
        String sql = "SELECT faculty_id, name, department_id, max_hours_per_week, cost_per_hour FROM faculty WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Faculty(
                            rs.getInt("faculty_id"),
                            rs.getString("name"),
                            rs.getInt("department_id"),
                            rs.getInt("max_hours_per_week"),
                            rs.getDouble("cost_per_hour")
                    ));
                }
            }
        }
        return list;
    }

    public List<Faculty> getAll() throws Exception {
        List<Faculty> list = new ArrayList<>();
        String sql = "SELECT faculty_id, name, department_id, max_hours_per_week, cost_per_hour FROM faculty";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Faculty(
                        rs.getInt("faculty_id"),
                        rs.getString("name"),
                        rs.getInt("department_id"),
                        rs.getInt("max_hours_per_week"),
                        rs.getDouble("cost_per_hour")
                ));
            }
        }
        return list;
    }

    public void update(int facultyId, String name, int departmentId, int maxHoursPerWeek, double costPerHour) throws Exception {
        String sql = "UPDATE faculty SET name=?, department_id=?, max_hours_per_week=?, cost_per_hour=? WHERE faculty_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, departmentId);
            ps.setInt(3, maxHoursPerWeek);
            ps.setDouble(4, costPerHour);
            ps.setInt(5, facultyId);
            ps.executeUpdate();
        }
    }

    public void delete(int facultyId) throws Exception {
        String sql = "DELETE FROM faculty WHERE faculty_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.executeUpdate();
        }
    }

    public void seedFullAvailability() throws Exception {
        String insertMissing = "INSERT IGNORE INTO faculty_availability(faculty_id, slot_id, available) " +
                "SELECT f.faculty_id, t.slot_id, 1 FROM faculty f CROSS JOIN timeslot t";
        String normalize = "UPDATE faculty_availability SET available = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psInsert = conn.prepareStatement(insertMissing);
             PreparedStatement psNormalize = conn.prepareStatement(normalize)) {
            psInsert.executeUpdate();
            psNormalize.executeUpdate();
        }
    }

    public Map<Integer, Set<Integer>> getAvailabilityMap() throws Exception {
        Map<Integer, Set<Integer>> availableSlotsByFaculty = new HashMap<>();
        String sql = "SELECT faculty_id, slot_id FROM faculty_availability WHERE available = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int facultyId = rs.getInt("faculty_id");
                int slotId = rs.getInt("slot_id");
                availableSlotsByFaculty.computeIfAbsent(facultyId, k -> new HashSet<>()).add(slotId);
            }
        } catch (SQLException e) {
            // Availability data is optional. Empty map means unrestricted availability.
            return new HashMap<>();
        }
        return availableSlotsByFaculty;
    }
}
