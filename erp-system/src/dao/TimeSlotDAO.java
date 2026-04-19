package dao;

import config.DBConnection;
import model.TimeSlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotDAO {

    public void save(TimeSlot slot) throws Exception {
        String sql = "INSERT INTO timeslot(day, period) VALUES(?, ?) ON DUPLICATE KEY UPDATE day=VALUES(day)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot.getDay());
            ps.setInt(2, slot.getPeriod());
            ps.executeUpdate();
        }
    }

    public void update(int slotId, String day, int period) throws Exception {
        String sql = "UPDATE timeslot SET day = ?, period = ? WHERE slot_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setInt(2, period);
            ps.setInt(3, slotId);
            ps.executeUpdate();
        }
    }

    public void delete(int slotId) throws Exception {
        String sql = "DELETE FROM timeslot WHERE slot_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.executeUpdate();
        }
    }

    public List<TimeSlot> getAll() throws Exception {
        List<TimeSlot> slots = new ArrayList<>();
        String sql = "SELECT slot_id, day, period FROM timeslot ORDER BY slot_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                slots.add(new TimeSlot(
                        rs.getInt("slot_id"),
                        rs.getString("day"),
                        rs.getInt("period")
                ));
            }
        }
        return slots;
    }

    public void upsertDefaultWeekSlots() throws Exception {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
        String sql = "INSERT INTO timeslot(day, period) VALUES(?, ?) ON DUPLICATE KEY UPDATE day=VALUES(day)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String day : days) {
                for (int period = 1; period <= 4; period++) {
                    ps.setString(1, day);
                    ps.setInt(2, period);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }
}
