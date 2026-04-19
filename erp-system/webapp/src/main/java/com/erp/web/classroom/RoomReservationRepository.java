package com.erp.web.classroom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class RoomReservationRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoomReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean insert(
            int roomId,
            int slotId,
            String semester,
            LocalDate date,
            Integer facultyId,
            String reservationType,
            String reason,
            String createdBy
    ) {
        String sql = "INSERT INTO room_reservation(room_id, slot_id, semester, reserve_date, faculty_id, reservation_type, reason, created_by) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, roomId, slotId, semester, date, facultyId, reservationType, reason, createdBy);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    public void delete(int roomId, int slotId, LocalDate date) {
        String sql = "DELETE FROM room_reservation WHERE room_id=? AND slot_id=? AND reserve_date=?";
        jdbcTemplate.update(sql, roomId, slotId, date);
    }

    public List<RoomReservationView> recent(int limit) {
        int safeLimit = Math.max(1, Math.min(200, limit));
        String sql =
                "SELECT c.room_code, c.building, ts.day, ts.period, " +
                        "DATE_FORMAT(r.reserve_date, '%Y-%m-%d') AS reserve_date, " +
                        "r.reservation_type, r.created_by, r.reason " +
                        "FROM room_reservation r " +
                        "JOIN classroom c ON c.room_id = r.room_id " +
                        "JOIN timeslot ts ON ts.slot_id = r.slot_id " +
                        "ORDER BY r.reservation_id DESC LIMIT ?";
        return jdbcTemplate.query(sql, (rs, i) -> new RoomReservationView(
                rs.getString("room_code"),
                rs.getString("building"),
                rs.getString("day"),
                rs.getInt("period"),
                rs.getString("reserve_date"),
                rs.getString("reservation_type"),
                rs.getString("created_by"),
                rs.getString("reason")
        ), safeLimit);
    }
}
