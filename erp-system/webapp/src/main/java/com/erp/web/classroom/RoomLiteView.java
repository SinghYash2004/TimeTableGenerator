package com.erp.web.classroom;

public class RoomLiteView {
    private final int roomId;
    private final String roomCode;
    private final int capacity;

    public RoomLiteView(int roomId, String roomCode, int capacity) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.capacity = capacity;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getCapacity() {
        return capacity;
    }
}
