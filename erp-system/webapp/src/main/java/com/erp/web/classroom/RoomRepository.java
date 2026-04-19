package com.erp.web.classroom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoomRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoomView> findAll() {
        String sql = "SELECT room_id, room_code, building, floor_no, room_type, equipment_tags, capacity, cost_per_hour FROM classroom ORDER BY room_id";
        return jdbcTemplate.query(sql, (rs, i) -> new RoomView(
                rs.getInt("room_id"),
                rs.getString("room_code"),
                rs.getString("building"),
                rs.getInt("floor_no"),
                rs.getString("room_type"),
                rs.getString("equipment_tags"),
                rs.getInt("capacity"),
                rs.getDouble("cost_per_hour")
        ));
    }

    public List<String> findBuildings() {
        String sql = "SELECT DISTINCT building FROM classroom ORDER BY building";
        return jdbcTemplate.query(sql, (rs, i) -> rs.getString("building"));
    }

    public List<String> findRoomTypes() {
        String sql = "SELECT DISTINCT room_type FROM classroom ORDER BY room_type";
        return jdbcTemplate.query(sql, (rs, i) -> rs.getString("room_type"));
    }

    public void save(String roomCode, String building, int floorNo, String roomType, String equipmentTags, int capacity, double costPerHour) {
        String sql = "INSERT INTO classroom(room_code, building, floor_no, room_type, equipment_tags, capacity, cost_per_hour) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, roomCode, building, floorNo, roomType, equipmentTags, capacity, costPerHour);
    }

    public void update(int roomId, String roomCode, String building, int floorNo, String roomType, String equipmentTags, int capacity, double costPerHour) {
        String sql = "UPDATE classroom SET room_code=?, building=?, floor_no=?, room_type=?, equipment_tags=?, capacity=?, cost_per_hour=? WHERE room_id=?";
        jdbcTemplate.update(sql, roomCode, building, floorNo, roomType, equipmentTags, capacity, costPerHour, roomId);
    }

    public void delete(int roomId) {
        String sql = "DELETE FROM classroom WHERE room_id=?";
        jdbcTemplate.update(sql, roomId);
    }
}
