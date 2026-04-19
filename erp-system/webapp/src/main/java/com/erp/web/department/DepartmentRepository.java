package com.erp.web.department;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DepartmentRepository {
    private final JdbcTemplate jdbcTemplate;

    public DepartmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DepartmentView> findAll() {
        String sql = "SELECT d.department_id, d.name, d.budget_limit, " +
                "COALESCE((SELECT COUNT(*) FROM faculty f WHERE f.department_id = d.department_id), 0) AS faculty_count, " +
                "COALESCE((SELECT COUNT(*) FROM subject s WHERE s.department_id = d.department_id), 0) AS subject_count, " +
                "COALESCE((SELECT SUM(f.cost_per_hour + c.cost_per_hour) " +
                "          FROM timetable t " +
                "          JOIN faculty f ON f.faculty_id = t.faculty_id " +
                "          JOIN classroom c ON c.room_id = t.room_id " +
                "          WHERE t.department_id = d.department_id), 0) AS used_cost " +
                "FROM department d ORDER BY d.department_id";
        return jdbcTemplate.query(sql, (rs, i) -> new DepartmentView(
                rs.getInt("department_id"),
                rs.getString("name"),
                rs.getDouble("budget_limit"),
                rs.getInt("faculty_count"),
                rs.getInt("subject_count"),
                budgetPercent(rs.getDouble("used_cost"), rs.getDouble("budget_limit")),
                riskLabel(budgetPercent(rs.getDouble("used_cost"), rs.getDouble("budget_limit")))
        ));
    }

    public void save(String name, double budgetLimit) {
        String sql = "INSERT INTO department(name, budget_limit) VALUES(?, ?) AS new " +
                "ON DUPLICATE KEY UPDATE budget_limit=new.budget_limit";
        jdbcTemplate.update(sql, name, budgetLimit);
    }

    public void update(int departmentId, String name, double budgetLimit) {
        String sql = "UPDATE department SET name=?, budget_limit=? WHERE department_id=?";
        jdbcTemplate.update(sql, name, budgetLimit, departmentId);
    }

    public void delete(int departmentId) {
        String sql = "DELETE FROM department WHERE department_id=?";
        jdbcTemplate.update(sql, departmentId);
    }

    private double budgetPercent(double usedCost, double budgetLimit) {
        if (budgetLimit <= 0) {
            return 0;
        }
        return Math.min(100.0, (usedCost * 100.0) / budgetLimit);
    }

    private String riskLabel(double percent) {
        if (percent >= 90) {
            return "High";
        }
        if (percent >= 70) {
            return "Medium";
        }
        return "Low";
    }
}
