package model;

public class Department {
    private int departmentId;
    private String name;
    private double budgetLimit;

    public Department(int departmentId, String name, double budgetLimit) {
        this.departmentId = departmentId;
        this.name = name;
        this.budgetLimit = budgetLimit;
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

    @Override
    public String toString() {
        return departmentId + " | " + name + " | budget=" + budgetLimit;
    }
}
