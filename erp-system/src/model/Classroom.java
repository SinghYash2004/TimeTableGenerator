package model;

public class Classroom {
    private int roomId;
    private String roomCode;
    private int capacity;
    private double costPerHour;

    public Classroom(int roomId, String roomCode, int capacity, double costPerHour) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.capacity = capacity;
        this.costPerHour = costPerHour;
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

    public double getCostPerHour() {
        return costPerHour;
    }

    @Override
    public String toString() {
        return roomId + " | " + roomCode + " | capacity=" + capacity;
    }
}
