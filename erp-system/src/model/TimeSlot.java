package model;

public class TimeSlot {
    private int slotId;
    private String day;
    private int period;

    public TimeSlot(int slotId, String day, int period) {
        this.slotId = slotId;
        this.day = day;
        this.period = period;
    }

    public TimeSlot(String day, String time) {
        this(0, day, parsePeriod(time));
    }

    private static int parsePeriod(String time) {
        try {
            return Integer.parseInt(time.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
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

    public String getTime() {
        return "P" + period;
    }

    @Override
    public String toString() {
        return day + " P" + period;
    }
}
