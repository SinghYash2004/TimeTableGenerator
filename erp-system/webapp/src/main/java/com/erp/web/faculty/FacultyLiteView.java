package com.erp.web.faculty;

public class FacultyLiteView {
    private final int facultyId;
    private final String name;
    private final String departmentName;
    private final int maxHoursPerWeek;

    public FacultyLiteView(int facultyId, String name, String departmentName, int maxHoursPerWeek) {
        this.facultyId = facultyId;
        this.name = name;
        this.departmentName = departmentName;
        this.maxHoursPerWeek = maxHoursPerWeek;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public String getName() {
        return name;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }
}
