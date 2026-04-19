package com.erp.web.section;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SectionAdminRepository {
    private final JdbcTemplate jdbcTemplate;

    public SectionAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SectionAdminView> findAll() {
        String sql = "SELECT s.section_id, s.section_name, s.semester_no, s.strength, " +
                "s.department_id, d.name AS department_name " +
                "FROM section s JOIN department d ON d.department_id = s.department_id " +
                "ORDER BY s.section_id";
        return jdbcTemplate.query(sql, (rs, i) -> new SectionAdminView(
                rs.getInt("section_id"),
                rs.getString("section_name"),
                rs.getInt("semester_no"),
                rs.getInt("strength"),
                rs.getInt("department_id"),
                rs.getString("department_name")
        ));
    }

    public List<SectionAdminView> findByDepartmentAndSemester(int departmentId, int semesterNo) {
        String sql = "SELECT s.section_id, s.section_name, s.semester_no, s.strength, " +
                "s.department_id, d.name AS department_name " +
                "FROM section s JOIN department d ON d.department_id = s.department_id " +
                "WHERE s.department_id = ? AND s.semester_no = ? " +
                "ORDER BY s.section_id";
        return jdbcTemplate.query(sql, (rs, i) -> new SectionAdminView(
                rs.getInt("section_id"),
                rs.getString("section_name"),
                rs.getInt("semester_no"),
                rs.getInt("strength"),
                rs.getInt("department_id"),
                rs.getString("department_name")
        ), departmentId, semesterNo);
    }

    public void save(String sectionName, int semesterNo, int strength, int departmentId) {
        String sql = "INSERT INTO section(section_name, semester_no, strength, department_id) VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(sql, sectionName, semesterNo, strength, departmentId);
    }

    public void update(int sectionId, String sectionName, int semesterNo, int strength, int departmentId) {
        String sql = "UPDATE section SET section_name=?, semester_no=?, strength=?, department_id=? WHERE section_id=?";
        jdbcTemplate.update(sql, sectionName, semesterNo, strength, departmentId, sectionId);
    }

    public void delete(int sectionId) {
        String sql = "DELETE FROM section WHERE section_id=?";
        jdbcTemplate.update(sql, sectionId);
    }
}
