package com.erp.web.section;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SectionRepository {
    private final JdbcTemplate jdbcTemplate;

    public SectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SectionView> findByDepartmentAndSemester(int departmentId, int semesterNo) {
        String sql = "SELECT section_id, section_name, semester_no, department_id FROM section " +
                "WHERE department_id = ? AND semester_no = ? ORDER BY section_name";
        return jdbcTemplate.query(sql, (rs, i) -> new SectionView(
                rs.getInt("section_id"),
                rs.getString("section_name"),
                rs.getInt("semester_no"),
                rs.getInt("department_id")
        ), departmentId, semesterNo);
    }
}
