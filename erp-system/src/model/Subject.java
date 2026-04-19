package model;

public class Subject {
    private int subjectId;
    private String name;
    private int weeklyHours;
    private int departmentId;

    public Subject(int subjectId, String name, int weeklyHours, int departmentId) {
        this.subjectId = subjectId;
        this.name = name;
        this.weeklyHours = weeklyHours;
        this.departmentId = departmentId;
    }

    public Subject(String name, int weeklyHours) {
        this(0, name, weeklyHours, 0);
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

    @Override
    public String toString() {
        return subjectId + " | " + name + " | weeklyHours=" + weeklyHours;
    }
}
