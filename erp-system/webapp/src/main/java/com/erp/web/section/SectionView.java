package com.erp.web.section;

public class SectionView {
    private final int sectionId;
    private final String sectionName;
    private final int semesterNo;
    private final int departmentId;

    public SectionView(int sectionId, String sectionName, int semesterNo, int departmentId) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.semesterNo = semesterNo;
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

    public int getDepartmentId() {
        return departmentId;
    }
}
