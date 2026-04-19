package model;

public class Section {
    private int sectionId;
    private String sectionName;
    private int semesterNo;
    private int strength;
    private int departmentId;

    public Section(int sectionId, String sectionName, int semesterNo, int strength, int departmentId) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.semesterNo = semesterNo;
        this.strength = strength;
        this.departmentId = departmentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public int getSemesterNo() {
        return semesterNo;
    }

    public int getStrength() {
        return strength;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    @Override
    public String toString() {
        return sectionId + " | " + sectionName + " | sem=" + semesterNo + " | strength=" + strength + " | dept=" + departmentId;
    }
}
