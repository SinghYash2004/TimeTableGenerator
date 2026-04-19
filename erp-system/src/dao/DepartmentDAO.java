package dao;

import config.DBConnection;
import model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public void save(Department department) throws Exception {
        String sql = "INSERT INTO department(name, budget_limit) VALUES(?, ?) " +
                "ON DUPLICATE KEY UPDATE budget_limit=VALUES(budget_limit)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getName());
            ps.setDouble(2, department.getBudgetLimit());
            ps.executeUpdate();
        }
    }

    public void update(int departmentId, String name, double budgetLimit) throws Exception {
        String sql = "UPDATE department SET name = ?, budget_limit = ? WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, budgetLimit);
            ps.setInt(3, departmentId);
            ps.executeUpdate();
        }
    }

    public void delete(int departmentId) throws Exception {
        String sql = "DELETE FROM department WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            ps.executeUpdate();
        }
    }

    public List<Department> getAll() throws Exception {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT department_id, name, budget_limit FROM department ORDER BY department_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Department(
                        rs.getInt("department_id"),
                        rs.getString("name"),
                        rs.getDouble("budget_limit")
                ));
            }
        }
        return list;
    }

    public Department getByName(String name) throws Exception {
        String sql = "SELECT department_id, name, budget_limit FROM department WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Department(
                            rs.getInt("department_id"),
                            rs.getString("name"),
                            rs.getDouble("budget_limit")
                    );
                }
            }
        }
        return null;
    }

    public Department getById(int departmentId) throws Exception {
        String sql = "SELECT department_id, name, budget_limit FROM department WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Department(
                            rs.getInt("department_id"),
                            rs.getString("name"),
                            rs.getDouble("budget_limit")
                    );
                }
            }
        }
        return null;
    }
}
