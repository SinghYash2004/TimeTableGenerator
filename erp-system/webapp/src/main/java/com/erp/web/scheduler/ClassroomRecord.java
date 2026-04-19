package com.erp.web.scheduler;

public class ClassroomRecord {
    public final int id;
    public final int capacity;
    public final double costPerHour;

    public ClassroomRecord(int id, int capacity, double costPerHour) {
        this.id = id;
        this.capacity = capacity;
        this.costPerHour = costPerHour;
    }
}
