package com.erp.web.timeslot;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TimeSlotRepository {
    private final JdbcTemplate jdbcTemplate;

    public TimeSlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TimeSlotView> findAll() {
        String sql = "SELECT slot_id, day, period FROM timeslot ORDER BY day, period";
        return jdbcTemplate.query(sql, (rs, i) -> new TimeSlotView(
                rs.getInt("slot_id"),
                rs.getString("day"),
                rs.getInt("period")
        ));
    }

    public TimeSlotView findById(int slotId) {
        String sql = "SELECT slot_id, day, period FROM timeslot WHERE slot_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, i) -> new TimeSlotView(
                rs.getInt("slot_id"),
                rs.getString("day"),
                rs.getInt("period")
        ), slotId);
    }

    public List<TimeSlotView> findByDayAndPeriodRange(String day, int startPeriod, int endPeriod) {
        String sql = "SELECT slot_id, day, period FROM timeslot WHERE day = ? AND period BETWEEN ? AND ? ORDER BY period";
        return jdbcTemplate.query(sql, (rs, i) -> new TimeSlotView(
                rs.getInt("slot_id"),
                rs.getString("day"),
                rs.getInt("period")
        ), day, startPeriod, endPeriod);
    }

    public List<TimeSlotView> findNextByDayPeriod(String day, int period, int limit) {
        String sql = "SELECT slot_id, day, period FROM timeslot WHERE day = ? AND period > ? ORDER BY period LIMIT ?";
        return jdbcTemplate.query(sql, (rs, i) -> new TimeSlotView(
                rs.getInt("slot_id"),
                rs.getString("day"),
                rs.getInt("period")
        ), day, period, limit);
    }

    public void save(String day, int period) {
        String sql = "INSERT INTO timeslot(day, period) VALUES(?, ?)";
        jdbcTemplate.update(sql, day, period);
    }

    public void update(int slotId, String day, int period) {
        String sql = "UPDATE timeslot SET day=?, period=? WHERE slot_id=?";
        jdbcTemplate.update(sql, day, period, slotId);
    }

    public void delete(int slotId) {
        String sql = "DELETE FROM timeslot WHERE slot_id=?";
        jdbcTemplate.update(sql, slotId);
    }
}
