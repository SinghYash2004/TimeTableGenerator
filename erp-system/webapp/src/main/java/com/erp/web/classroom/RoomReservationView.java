package com.erp.web.classroom;

public class RoomReservationView {
    private final String roomCode;
    private final String building;
    private final String day;
    private final int period;
    private final String reserveDate;
    private final String reservationType;
    private final String createdBy;
    private final String reason;

    public RoomReservationView(
            String roomCode,
            String building,
            String day,
            int period,
            String reserveDate,
            String reservationType,
            String createdBy,
            String reason
    ) {
        this.roomCode = roomCode;
        this.building = building;
        this.day = day;
        this.period = period;
        this.reserveDate = reserveDate;
        this.reservationType = reservationType;
        this.createdBy = createdBy;
        this.reason = reason;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getBuilding() {
        return building;
    }

    public String getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }

    public String getReserveDate() {
        return reserveDate;
    }

    public String getReservationType() {
        return reservationType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getReason() {
        return reason;
    }
}
