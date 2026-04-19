package com.erp.web.auth;

public class UserAdminView {
    private final int userId;
    private final String username;
    private final String role;
    private final Integer facultyId;
    private final String facultyName;
    private final String departmentName;
    private final boolean active;

    public UserAdminView(
            int userId,
            String username,
            String role,
            Integer facultyId,
            String facultyName,
            String departmentName,
            boolean active
    ) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.departmentName = departmentName;
        this.active = active;
    }

    public int getUserId() {
        return userId;
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

    public String getFacultyName() {
        return facultyName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public boolean isActive() {
        return active;
    }
}
