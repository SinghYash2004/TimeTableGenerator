package dao;

import config.DBConnection;
import model.Classroom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAO {

    public void save(Classroom classroom) throws Exception {
        String sql = "INSERT INTO classroom(room_code, capacity, cost_per_hour) VALUES(?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE capacity=VALUES(capacity), cost_per_hour=VALUES(cost_per_hour)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classroom.getRoomCode());
            ps.setInt(2, classroom.getCapacity());
            ps.setDouble(3, classroom.getCostPerHour());
            ps.executeUpdate();
        }
    }

    public List<Classroom> getAll() throws Exception {
        List<Classroom> list = new ArrayList<>();
        String sql = "SELECT room_id, room_code, capacity, cost_per_hour FROM classroom";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Classroom(
                        rs.getInt("room_id"),
                        rs.getString("room_code"),
                        rs.getInt("capacity"),
                        rs.getDouble("cost_per_hour")
                ));
            }
        }
        return list;
    }

    public void update(int roomId, String roomCode, int capacity, double costPerHour) throws Exception {
        String sql = "UPDATE classroom SET room_code=?, capacity=?, cost_per_hour=? WHERE room_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomCode);
            ps.setInt(2, capacity);
            ps.setDouble(3, costPerHour);
            ps.setInt(4, roomId);
            ps.executeUpdate();
        }
    }

    public void delete(int roomId) throws Exception {
        String sql = "DELETE FROM classroom WHERE room_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        }
    }
}
