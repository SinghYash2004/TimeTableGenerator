package com.erp.web.subject;

public class SubjectView {
    private final int subjectId;
    private final String name;
    private final int weeklyHours;
    private final int departmentId;
    private final String departmentName;

    public SubjectView(int subjectId, String name, int weeklyHours, int departmentId, String departmentName) {
        this.subjectId = subjectId;
        this.name = name;
        this.weeklyHours = weeklyHours;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }
}
