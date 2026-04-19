package com.erp.web.audit;

public class AuditLogEntry {
    private final String username;
    private final String action;
    private final String detail;
    private final String createdAt;

    public AuditLogEntry(String username, String action, String detail, String createdAt) {
        this.username = username;
        this.action = action;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
