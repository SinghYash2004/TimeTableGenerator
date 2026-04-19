package com.erp.web.scheduler;

public class FacultyRecord {
    public final int id;
    public final int departmentId;
    public final int maxHours;
    public final double costPerHour;

    public FacultyRecord(int id, int departmentId, int maxHours, double costPerHour) {
        this.id = id;
        this.departmentId = departmentId;
        this.maxHours = maxHours;
        this.costPerHour = costPerHour;
    }
}
