package com.erp.web.classroom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoomLiteRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoomLiteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoomLiteView> findAll() {
        String sql = "SELECT room_id, room_code, capacity FROM classroom ORDER BY room_id";
        return jdbcTemplate.query(sql, (rs, i) -> new RoomLiteView(
                rs.getInt("room_id"),
                rs.getString("room_code"),
                rs.getInt("capacity")
        ));
    }
}
