package com.erp.web.timetable;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class ClassCancellationRepository {
    private final JdbcTemplate jdbcTemplate;

    public ClassCancellationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(int timetableId, LocalDate cancelDate, String reason, String createdBy) {
        String sql = "INSERT INTO class_cancellation(timetable_id, cancel_date, reason, created_by) " +
                "VALUES(?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE reason=VALUES(reason), created_by=VALUES(created_by)";
        jdbcTemplate.update(sql, timetableId, cancelDate, reason, createdBy);
    }

    public void delete(int timetableId, LocalDate cancelDate) {
        String sql = "DELETE FROM class_cancellation WHERE timetable_id=? AND cancel_date=?";
        jdbcTemplate.update(sql, timetableId, cancelDate);
    }
}
