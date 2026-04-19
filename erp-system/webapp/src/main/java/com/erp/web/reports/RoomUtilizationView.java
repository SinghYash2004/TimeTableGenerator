package com.erp.web.reports;

public class RoomUtilizationView {
    private final String roomCode;
    private final int assignedSlots;
    private final int totalSlots;
    private final double utilizationPercent;

    public RoomUtilizationView(String roomCode, int assignedSlots, int totalSlots, double utilizationPercent) {
        this.roomCode = roomCode;
        this.assignedSlots = assignedSlots;
        this.totalSlots = totalSlots;
        this.utilizationPercent = utilizationPercent;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getAssignedSlots() {
        return assignedSlots;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public double getUtilizationPercent() {
        return utilizationPercent;
    }
}
