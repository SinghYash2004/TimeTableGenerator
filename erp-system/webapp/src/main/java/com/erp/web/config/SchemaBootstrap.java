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
                        "user_id SERIAL PRIMARY KEY," +
                        "username VARCHAR(100) NOT NULL UNIQUE," +
                        "password_hash VARCHAR(255) NOT NULL," +
                        "role VARCHAR(20) NOT NULL," +
                        "faculty_id INT," +
                        "active BOOLEAN NOT NULL DEFAULT TRUE," +
                        "CONSTRAINT fk_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)" +
                        ")"
        );

        jdbcTemplate.update(
                "INSERT INTO users(username, password_hash, role, faculty_id) " +
                        "SELECT 'admin', md5('admin123'), 'ADMIN', NULL " +
                        "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='admin')"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS password_reset (" +
                        "token VARCHAR(80) PRIMARY KEY," +
                        "username VARCHAR(100) NOT NULL," +
                        "expires_at TIMESTAMP NOT NULL," +
                        "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE" +
                        ")"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS audit_log (" +
                        "audit_id SERIAL PRIMARY KEY," +
                        "username VARCHAR(100) NOT NULL," +
                        "action VARCHAR(80) NOT NULL," +
                        "detail VARCHAR(500)," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        );
    }
}