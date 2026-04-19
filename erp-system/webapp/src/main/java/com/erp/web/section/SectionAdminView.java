package com.erp.web.section;

public class SectionAdminView {
    private final int sectionId;
    private final String sectionName;
    private final int semesterNo;
    private final int strength;
    private final int departmentId;
    private final String departmentName;

    public SectionAdminView(
            int sectionId,
            String sectionName,
            int semesterNo,
            int strength,
            int departmentId,
            String departmentName
    ) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.semesterNo = semesterNo;
        this.strength = strength;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
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

    public String getDepartmentName() {
        return departmentName;
    }
}
