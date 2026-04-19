package com.erp.web.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaBootstrap {
    private final JdbcTemplate jdbcTemplate;

    public SchemaBootstrap(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureCoreAuthData() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "username VARCHAR(100) NOT NULL UNIQUE," +
                        "password_hash VARCHAR(255) NOT NULL," +
                        "role VARCHAR(20) NOT NULL," +
                        "faculty_id INT NULL," +
                        "active TINYINT(1) NOT NULL DEFAULT 1," +
                        "FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)" +
                        ")"
        );
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN theme VARCHAR(32) NULL");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN theme VARCHAR(32) NULL");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN motion_pref VARCHAR(16) NULL");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN motion_pref VARCHAR(16) NULL");
        } catch (Exception ignored) {
        }
        jdbcTemplate.update(
                "INSERT IGNORE INTO users(username, password_hash, role, faculty_id) " +
                        "VALUES('admin', SHA2('admin123', 256), 'ADMIN', NULL)"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS password_reset (" +
                        "token VARCHAR(80) PRIMARY KEY," +
                        "username VARCHAR(100) NOT NULL," +
                        "expires_at DATETIME NOT NULL," +
                        "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE" +
                        ")"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS audit_log (" +
                        "audit_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "username VARCHAR(100) NOT NULL," +
                        "action VARCHAR(80) NOT NULL," +
                        "detail VARCHAR(500) NULL," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        );

        try {
            jdbcTemplate.execute("ALTER TABLE classroom ADD COLUMN building VARCHAR(50) NOT NULL DEFAULT 'MAIN'");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("UPDATE classroom SET building='MAIN' WHERE building IS NULL OR building=''");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE classroom ADD COLUMN floor_no INT NOT NULL DEFAULT 0");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE classroom ADD COLUMN room_type VARCHAR(30) NOT NULL DEFAULT 'LECTURE'");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE classroom ADD COLUMN equipment_tags VARCHAR(255) NULL");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS class_cancellation (" +
                            "cancellation_id INT PRIMARY KEY AUTO_INCREMENT," +
                            "timetable_id INT NOT NULL," +
                            "cancel_date DATE NOT NULL," +
                            "reason VARCHAR(255) NULL," +
                            "created_by VARCHAR(100) NULL," +
                            "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "UNIQUE KEY uk_timetable_cancel_date (timetable_id, cancel_date)," +
                            "FOREIGN KEY (timetable_id) REFERENCES timetable(id) ON DELETE CASCADE" +
                            ")"
            );
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS room_reservation (" +
                            "reservation_id INT PRIMARY KEY AUTO_INCREMENT," +
                            "room_id INT NOT NULL," +
                            "slot_id INT NOT NULL," +
                            "semester VARCHAR(20) NOT NULL," +
                            "reserve_date DATE NOT NULL," +
                            "faculty_id INT NULL," +
                            "reservation_type VARCHAR(20) NOT NULL DEFAULT 'RESERVE'," +
                            "reason VARCHAR(255) NULL," +
                            "created_by VARCHAR(100) NULL," +
                            "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "UNIQUE KEY uk_room_slot_date (room_id, slot_id, reserve_date)," +
                            "FOREIGN KEY (room_id) REFERENCES classroom(room_id)," +
                            "FOREIGN KEY (slot_id) REFERENCES timeslot(slot_id)," +
                            "FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)" +
                            ")"
            );
        } catch (Exception ignored) {
        }
    }
}
