package com.erp.web.reports;

public class FacultyOverloadView {
    private final String facultyName;
    private final String departmentName;
    private final int assigned;
    private final int maxHours;

    public FacultyOverloadView(String facultyName, String departmentName, int assigned, int maxHours) {
        this.facultyName = facultyName;
        this.departmentName = departmentName;
        this.assigned = assigned;
        this.maxHours = maxHours;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getAssigned() {
        return assigned;
    }

    public int getMaxHours() {
        return maxHours;
    }
}
