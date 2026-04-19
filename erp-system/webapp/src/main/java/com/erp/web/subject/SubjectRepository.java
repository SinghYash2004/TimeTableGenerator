package com.erp.web.subject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public SubjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SubjectView> findAll() {
        String sql = "SELECT s.subject_id, s.name, s.weekly_hours, s.department_id, d.name AS department_name " +
                "FROM subject s JOIN department d ON d.department_id = s.department_id " +
                "ORDER BY s.subject_id";
        return jdbcTemplate.query(sql, (rs, i) -> new SubjectView(
                rs.getInt("subject_id"),
                rs.getString("name"),
                rs.getInt("weekly_hours"),
                rs.getInt("department_id"),
                rs.getString("department_name")
        ));
    }

    public List<SubjectView> findByDepartment(int departmentId) {
        String sql = "SELECT s.subject_id, s.name, s.weekly_hours, s.department_id, d.name AS department_name " +
                "FROM subject s JOIN department d ON d.department_id = s.department_id " +
                "WHERE s.department_id = ? ORDER BY s.subject_id";
        return jdbcTemplate.query(sql, (rs, i) -> new SubjectView(
                rs.getInt("subject_id"),
                rs.getString("name"),
                rs.getInt("weekly_hours"),
                rs.getInt("department_id"),
                rs.getString("department_name")
        ), departmentId);
    }

    public void save(String name, int weeklyHours, int departmentId) {
        String sql = "INSERT INTO subject(name, weekly_hours, department_id) VALUES(?, ?, ?)";
        jdbcTemplate.update(sql, name, weeklyHours, departmentId);
    }

    public void update(int subjectId, String name, int weeklyHours, int departmentId) {
        String sql = "UPDATE subject SET name=?, weekly_hours=?, department_id=? WHERE subject_id=?";
        jdbcTemplate.update(sql, name, weeklyHours, departmentId, subjectId);
    }

    public void delete(int subjectId) {
        String sql = "DELETE FROM subject WHERE subject_id=?";
        jdbcTemplate.update(sql, subjectId);
    }
}
