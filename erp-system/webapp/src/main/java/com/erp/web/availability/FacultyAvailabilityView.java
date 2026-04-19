package com.erp.web.availability;

public class FacultyAvailabilityView {
    private final int facultyId;
    private final String facultyName;
    private final int slotId;
    private final String day;
    private final int period;
    private final boolean available;

    public FacultyAvailabilityView(
            int facultyId,
            String facultyName,
            int slotId,
            String day,
            int period,
            boolean available
    ) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.slotId = slotId;
        this.day = day;
        this.period = period;
        this.available = available;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
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

    public boolean isAvailable() {
        return available;
    }
}
