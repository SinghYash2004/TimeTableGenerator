package com.erp.web.classroom;

public class RoomStatusView {
    private final int roomId;
    private final String roomCode;
    private final String building;
    private final int floorNo;
    private final String roomType;
    private final String equipmentTags;
    private final int capacity;
    private final String status;
    private final String blockedReason;

    public RoomStatusView(
            int roomId,
            String roomCode,
            String building,
            int floorNo,
            String roomType,
            String equipmentTags,
            int capacity,
            String status,
            String blockedReason
    ) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.building = building;
        this.floorNo = floorNo;
        this.roomType = roomType;
        this.equipmentTags = equipmentTags;
        this.capacity = capacity;
        this.status = status;
        this.blockedReason = blockedReason;
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

    public int getFloorNo() {
        return floorNo;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getEquipmentTags() {
        return equipmentTags;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public String getBlockedReason() {
        return blockedReason;
    }
}
