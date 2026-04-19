package com.erp.web.reports;

public class ConflictView {
    private final String type;
    private final String departmentName;
    private final int slotId;
    private final String day;
    private final int period;
    private final String entityName;
    private final int count;

    public ConflictView(
            String type,
            String departmentName,
            int slotId,
            String day,
            int period,
            String entityName,
            int count
    ) {
        this.type = type;
        this.departmentName = departmentName;
        this.slotId = slotId;
        this.day = day;
        this.period = period;
        this.entityName = entityName;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public String getDepartmentName() {
        return departmentName;
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

    public String getEntityName() {
        return entityName;
    }

    public int getCount() {
        return count;
    }
}
