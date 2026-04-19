package com.erp.web.auth;

public class UserSession {
    private final String username;
    private final String role;
    private final Integer facultyId;
    private final Integer departmentId;
    private final String theme;
    private final String motionPref;

    public UserSession(String username, String role, Integer facultyId, Integer departmentId) {
        this(username, role, facultyId, departmentId, "light", "system");
    }

    public UserSession(String username, String role, Integer facultyId, Integer departmentId, String theme, String motionPref) {
        this.username = username;
        this.role = role == null ? "FACULTY" : role.toUpperCase();
        this.facultyId = facultyId;
        this.departmentId = departmentId;
        this.theme = (theme == null || theme.isBlank()) ? "light" : theme;
        this.motionPref = (motionPref == null || motionPref.isBlank()) ? "system" : motionPref;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Integer getFacultyId() {
        return facultyId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public String getTheme() {
        return theme;
    }

    public String getMotionPref() {
        return motionPref;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
