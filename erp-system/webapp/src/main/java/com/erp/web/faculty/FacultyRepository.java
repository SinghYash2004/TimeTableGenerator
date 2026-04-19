package com.erp.web.faculty;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FacultyRepository {
    private final JdbcTemplate jdbcTemplate;

    public FacultyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FacultyAdminView> findAll() {
        String sql = "SELECT f.faculty_id, f.name, f.department_id, d.name AS department_name, " +
                "f.max_hours_per_week, f.cost_per_hour " +
                "FROM faculty f JOIN department d ON d.department_id = f.department_id " +
                "ORDER BY f.faculty_id";
        return jdbcTemplate.query(sql, (rs, i) -> new FacultyAdminView(
                rs.getInt("faculty_id"),
                rs.getString("name"),
                rs.getInt("department_id"),
                rs.getString("department_name"),
                rs.getInt("max_hours_per_week"),
                rs.getDouble("cost_per_hour")
        ));
    }

    public List<FacultyAdminView> findByDepartment(int departmentId) {
        String sql = "SELECT f.faculty_id, f.name, f.department_id, d.name AS department_name, " +
                "f.max_hours_per_week, f.cost_per_hour " +
                "FROM faculty f JOIN department d ON d.department_id = f.department_id " +
                "WHERE f.department_id = ? ORDER BY f.faculty_id";
        return jdbcTemplate.query(sql, (rs, i) -> new FacultyAdminView(
                rs.getInt("faculty_id"),
                rs.getString("name"),
                rs.getInt("department_id"),
                rs.getString("department_name"),
                rs.getInt("max_hours_per_week"),
                rs.getDouble("cost_per_hour")
        ), departmentId);
    }

    public void save(String name, int departmentId, int maxHoursPerWeek, double costPerHour) {
        String sql = "INSERT INTO faculty(name, department_id, max_hours_per_week, cost_per_hour) VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(sql, name, departmentId, maxHoursPerWeek, costPerHour);
    }

    public void update(int facultyId, String name, int departmentId, int maxHoursPerWeek, double costPerHour) {
        String sql = "UPDATE faculty SET name=?, department_id=?, max_hours_per_week=?, cost_per_hour=? WHERE faculty_id=?";
        jdbcTemplate.update(sql, name, departmentId, maxHoursPerWeek, costPerHour, facultyId);
    }

    public void delete(int facultyId) {
        String sql = "DELETE FROM faculty WHERE faculty_id=?";
        jdbcTemplate.update(sql, facultyId);
    }
}
