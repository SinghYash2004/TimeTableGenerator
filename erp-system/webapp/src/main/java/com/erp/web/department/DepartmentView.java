package com.erp.web.department;

public class DepartmentView {
    private final int departmentId;
    private final String name;
    private final double budgetLimit;
    private final int facultyCount;
    private final int subjectCount;
    private final double budgetUsedPercent;
    private final String aiRisk;

    public DepartmentView(int departmentId, String name, double budgetLimit, int facultyCount, int subjectCount, double budgetUsedPercent, String aiRisk) {
        this.departmentId = departmentId;
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.facultyCount = facultyCount;
        this.subjectCount = subjectCount;
        this.budgetUsedPercent = budgetUsedPercent;
        this.aiRisk = aiRisk;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public int getFacultyCount() {
        return facultyCount;
    }

    public int getSubjectCount() {
        return subjectCount;
    }

    public double getBudgetUsedPercent() {
        return budgetUsedPercent;
    }

    public String getAiRisk() {
        return aiRisk;
    }
}
