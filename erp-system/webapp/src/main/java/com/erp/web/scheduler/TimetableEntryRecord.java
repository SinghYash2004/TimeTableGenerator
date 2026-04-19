package com.erp.web.scheduler;

public class TimetableEntryRecord {
    public final String semester;
    public final int facultyId;
    public final int subjectId;
    public final int roomId;
    public final int slotId;
    public final int departmentId;
    public final int sectionId;

    public TimetableEntryRecord(String semester, int facultyId, int subjectId, int roomId, int slotId, int departmentId, int sectionId) {
        this.semester = semester;
        this.facultyId = facultyId;
        this.subjectId = subjectId;
        this.roomId = roomId;
        this.slotId = slotId;
        this.departmentId = departmentId;
        this.sectionId = sectionId;
    }
}
