package com.erp.web.faculty;

public class FacultyAdminView {
    private final int facultyId;
    private final String name;
    private final int departmentId;
    private final String departmentName;
    private final int maxHoursPerWeek;
    private final double costPerHour;

    public FacultyAdminView(
            int facultyId,
            String name,
            int departmentId,
            String departmentName,
            int maxHoursPerWeek,
            double costPerHour
    ) {
        this.facultyId = facultyId;
        this.name = name;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.maxHoursPerWeek = maxHoursPerWeek;
        this.costPerHour = costPerHour;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public String getName() {
        return name;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public double getCostPerHour() {
        return costPerHour;
    }
}
