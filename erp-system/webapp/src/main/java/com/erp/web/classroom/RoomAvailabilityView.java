package com.erp.web.classroom;

public class RoomAvailabilityView {
    private final int roomId;
    private final String roomCode;
    private final String building;
    private final int capacity;

    public RoomAvailabilityView(int roomId, String roomCode, String building, int capacity) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.building = building;
        this.capacity = capacity;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getBuilding() {
        return building;
    }

    public int getCapacity() {
        return capacity;
    }
}
