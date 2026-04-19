package com.erp.web.faculty;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FacultyLiteRepository {
    private final JdbcTemplate jdbcTemplate;

    public FacultyLiteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FacultyLiteView> findAll() {
        String sql = "SELECT f.faculty_id, f.name, d.name AS department_name, f.max_hours_per_week " +
                "FROM faculty f JOIN department d ON d.department_id = f.department_id ORDER BY f.faculty_id";
        return jdbcTemplate.query(sql, (rs, i) -> new FacultyLiteView(
                rs.getInt("faculty_id"),
                rs.getString("name"),
                rs.getString("department_name"),
                rs.getInt("max_hours_per_week")
        ));
    }
}
