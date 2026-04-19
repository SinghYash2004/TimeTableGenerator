package com.erp.web.classroom;

public class RoomView {
    private final int roomId;
    private final String roomCode;
    private final String building;
    private final int floorNo;
    private final String roomType;
    private final String equipmentTags;
    private final int capacity;
    private final double costPerHour;

    public RoomView(int roomId, String roomCode, String building, int floorNo, String roomType, String equipmentTags, int capacity, double costPerHour) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.building = building;
        this.floorNo = floorNo;
        this.roomType = roomType;
        this.equipmentTags = equipmentTags;
        this.capacity = capacity;
        this.costPerHour = costPerHour;
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

    public double getCostPerHour() {
        return costPerHour;
    }
}
