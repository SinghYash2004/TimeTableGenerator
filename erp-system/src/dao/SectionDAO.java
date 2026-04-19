package dao;

import config.DBConnection;
import model.Section;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {

    public void save(Section section) throws Exception {
        String sql = "INSERT INTO section(section_name, semester_no, strength, department_id) VALUES(?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE strength=VALUES(strength)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, section.getSectionName());
            ps.setInt(2, section.getSemesterNo());
            ps.setInt(3, section.getStrength());
            ps.setInt(4, section.getDepartmentId());
            ps.executeUpdate();
        }
    }

    public void update(int sectionId, String sectionName, int semesterNo, int strength, int departmentId) throws Exception {
        String sql = "UPDATE section SET section_name=?, semester_no=?, strength=?, department_id=? WHERE section_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sectionName);
            ps.setInt(2, semesterNo);
            ps.setInt(3, strength);
            ps.setInt(4, departmentId);
            ps.setInt(5, sectionId);
            ps.executeUpdate();
        }
    }

    public void delete(int sectionId) throws Exception {
        String sql = "DELETE FROM section WHERE section_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    public List<Section> getAll() throws Exception {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT section_id, section_name, semester_no, strength, department_id FROM section ORDER BY section_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Section(
                        rs.getInt("section_id"),
                        rs.getString("section_name"),
                        rs.getInt("semester_no"),
                        rs.getInt("strength"),
                        rs.getInt("department_id")
                ));
            }
        }
        return list;
    }

    public List<Section> getByDepartmentAndSemester(int departmentId, int semesterNo) throws Exception {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT section_id, section_name, semester_no, strength, department_id " +
                "FROM section WHERE department_id=? AND semester_no=? ORDER BY section_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            ps.setInt(2, semesterNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Section(
                            rs.getInt("section_id"),
                            rs.getString("section_name"),
                            rs.getInt("semester_no"),
                            rs.getInt("strength"),
                            rs.getInt("department_id")
                    ));
                }
            }
        }
        return list;
    }
}
