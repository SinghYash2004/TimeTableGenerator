package com.erp.web.availability;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FacultyAvailabilityRepository {
    private final JdbcTemplate jdbcTemplate;

    public FacultyAvailabilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FacultyAvailabilityView> findAll() {
        String sql = "SELECT fa.faculty_id, f.name AS faculty_name, fa.slot_id, t.day, t.period, fa.available " +
                "FROM faculty_availability fa " +
                "JOIN faculty f ON f.faculty_id = fa.faculty_id " +
                "JOIN timeslot t ON t.slot_id = fa.slot_id " +
                "ORDER BY f.name, t.day, t.period";
        return jdbcTemplate.query(sql, (rs, i) -> new FacultyAvailabilityView(
                rs.getInt("faculty_id"),
                rs.getString("faculty_name"),
                rs.getInt("slot_id"),
                rs.getString("day"),
                rs.getInt("period"),
                rs.getInt("available") == 1
        ));
    }

    public void upsert(int facultyId, int slotId, boolean available) {
        String sql = "INSERT INTO faculty_availability(faculty_id, slot_id, available) " +
                "VALUES(?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE available=VALUES(available)";
        jdbcTemplate.update(sql, facultyId, slotId, available ? 1 : 0);
    }

    public void delete(int facultyId, int slotId) {
        String sql = "DELETE FROM faculty_availability WHERE faculty_id=? AND slot_id=?";
        jdbcTemplate.update(sql, facultyId, slotId);
    }
}
