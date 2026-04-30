package com.erp.web.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {
    private final JdbcTemplate jdbcTemplate;

    public AuditLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(String username, String action, String detail) {
        String cleanUser = username == null ? "unknown" : username;
        String cleanAction = action == null ? "UNKNOWN" : action;
        String cleanDetail = detail == null ? "" : detail;
        jdbcTemplate.update(
                "INSERT INTO audit_log(username, action, detail, created_at) VALUES(?, ?, ?, NOW())",
                cleanUser, cleanAction, cleanDetail
        );
    }

    public List<AuditLogEntry> recent(int limit) {
        int safeLimit = Math.max(1, Math.min(200, limit));
        String sql = "SELECT username, action, detail, TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') AS created_at " +
                "FROM audit_log ORDER BY audit_id DESC LIMIT ?";
        return jdbcTemplate.query(sql, (rs, i) -> new AuditLogEntry(
                rs.getString("username"),
                rs.getString("action"),
                rs.getString("detail"),
                rs.getString("created_at")
        ), safeLimit);
    }
}
