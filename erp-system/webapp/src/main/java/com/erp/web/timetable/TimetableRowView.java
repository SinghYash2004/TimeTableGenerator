package com.erp.web.timetable;

public class TimetableRowView {
    private final int entryId;
    private final String semester;
    private final String departmentName;
    private final int departmentId;
    private final int sectionId;
    private final String sectionName;
    private final int subjectId;
    private final String day;
    private final int period;
    private final String subjectName;
    private final int facultyId;
    private final String facultyName;
    private final int roomId;
    private final String roomCode;
    private final int slotId;
    private final boolean facultyConflict;
    private final boolean roomConflict;
    private final boolean sectionConflict;
    private final boolean overloadWarning;
    private final boolean canceledAny;

    public TimetableRowView(
            int entryId,
            String semester,
            String departmentName,
            int departmentId,
            int sectionId,
            String sectionName,
            int subjectId,
            String day,
            int period,
            String subjectName,
            int facultyId,
            String facultyName,
            int roomId,
            String roomCode,
            int slotId,
            boolean facultyConflict,
            boolean roomConflict,
            boolean sectionConflict,
            boolean overloadWarning,
            boolean canceledAny
    ) {
        this.entryId = entryId;
        this.semester = semester;
        this.departmentName = departmentName;
        this.departmentId = departmentId;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.subjectId = subjectId;
        this.day = day;
        this.period = period;
        this.subjectName = subjectName;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.slotId = slotId;
        this.facultyConflict = facultyConflict;
        this.roomConflict = roomConflict;
        this.sectionConflict = sectionConflict;
        this.overloadWarning = overloadWarning;
        this.canceledAny = canceledAny;
    }

    public int getEntryId() {
        return entryId;
    }

    public String getSemester() {
        return semester;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getSlotId() {
        return slotId;
    }

    public boolean isFacultyConflict() {
        return facultyConflict;
    }

    public boolean isRoomConflict() {
        return roomConflict;
    }

    public boolean isSectionConflict() {
        return sectionConflict;
    }

    public boolean isOverloadWarning() {
        return overloadWarning;
    }

    public boolean isCanceledAny() {
        return canceledAny;
    }
}
