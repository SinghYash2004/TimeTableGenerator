package model;

public class Faculty {
    private int facultyId;
    private String name;
    private int departmentId;
    private int maxHoursPerWeek;
    private double costPerHour;

    public Faculty(int facultyId, String name, int departmentId, int maxHoursPerWeek, double costPerHour) {
        this.facultyId = facultyId;
        this.name = name;
        this.departmentId = departmentId;
        this.maxHoursPerWeek = maxHoursPerWeek;
        this.costPerHour = costPerHour;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public String getName() {
        return name;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public double getCostPerHour() {
        return costPerHour;
    }

    @Override
    public String toString() {
        return facultyId + " | " + name + " | dept=" + departmentId + " | maxHrs=" + maxHoursPerWeek;
    }
}
