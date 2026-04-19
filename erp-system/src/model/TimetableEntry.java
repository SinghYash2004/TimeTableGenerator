package model;

public class TimetableEntry {
    private int id;
    private String semester;
    private int facultyId;
    private int subjectId;
    private int roomId;
    private int slotId;
    private int departmentId;
    private int sectionId;

    public TimetableEntry(int id, String semester, int facultyId, int subjectId, int roomId, int slotId, int departmentId, int sectionId) {
        this.id = id;
        this.semester = semester;
        this.facultyId = facultyId;
        this.subjectId = subjectId;
        this.roomId = roomId;
        this.slotId = slotId;
        this.departmentId = departmentId;
        this.sectionId = sectionId;
    }

    public int getId() {
        return id;
    }

    public String getSemester() {
        return semester;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public int getSectionId() {
        return sectionId;
    }
}
