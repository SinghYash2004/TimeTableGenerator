package com.erp.web.timeslot;

public class TimeSlotView {
    private final int slotId;
    private final String day;
    private final int period;

    public TimeSlotView(int slotId, String day, int period) {
        this.slotId = slotId;
        this.day = day;
        this.period = period;
    }

    public int getSlotId() {
        return slotId;
    }

    public String getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }
}
