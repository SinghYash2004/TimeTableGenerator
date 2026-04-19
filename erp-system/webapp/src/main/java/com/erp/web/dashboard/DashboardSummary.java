package com.erp.web.dashboard;

public class DashboardSummary {
    private final int totalTimetableConflicts;
    private final double facultyOverloadPercent;
    private final double roomUtilizationPercent;
    private final double budgetUsagePercent;
    private final int aiOptimizationScore;
    private final int departmentCount;
    private final int facultyCount;
    private final int subjectCount;
    private final int timetableEntries;

    public DashboardSummary(
            int totalTimetableConflicts,
            double facultyOverloadPercent,
            double roomUtilizationPercent,
            double budgetUsagePercent,
            int aiOptimizationScore,
            int departmentCount,
            int facultyCount,
            int subjectCount,
            int timetableEntries
    ) {
        this.totalTimetableConflicts = totalTimetableConflicts;
        this.facultyOverloadPercent = facultyOverloadPercent;
        this.roomUtilizationPercent = roomUtilizationPercent;
        this.budgetUsagePercent = budgetUsagePercent;
        this.aiOptimizationScore = aiOptimizationScore;
        this.departmentCount = departmentCount;
        this.facultyCount = facultyCount;
        this.subjectCount = subjectCount;
        this.timetableEntries = timetableEntries;
    }

    public int getTotalTimetableConflicts() {
        return totalTimetableConflicts;
    }

    public double getFacultyOverloadPercent() {
        return facultyOverloadPercent;
    }

    public double getRoomUtilizationPercent() {
        return roomUtilizationPercent;
    }

    public double getBudgetUsagePercent() {
        return budgetUsagePercent;
    }

    public int getAiOptimizationScore() {
        return aiOptimizationScore;
    }

    public int getDepartmentCount() {
        return departmentCount;
    }

    public int getFacultyCount() {
        return facultyCount;
    }

    public int getSubjectCount() {
        return subjectCount;
    }

    public int getTimetableEntries() {
        return timetableEntries;
    }
}
