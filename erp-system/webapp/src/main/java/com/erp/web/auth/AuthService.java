package com.erp.web.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {
    private final JdbcTemplate jdbcTemplate;

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserSession authenticate(String username, String password) {
        String sql = "SELECT u.username, u.role, u.faculty_id, f.department_id, u.theme, u.motion_pref " +
                "FROM users u LEFT JOIN faculty f ON f.faculty_id = u.faculty_id " +
                "WHERE u.username = ? AND u.password_hash = SHA2(?, 256) AND u.active = 1";
        List<UserSession> rows = jdbcTemplate.query(sql, (rs, i) -> new UserSession(
                rs.getString("username"),
                rs.getString("role"),
                (Integer) rs.getObject("faculty_id"),
                (Integer) rs.getObject("department_id"),
                rs.getString("theme"),
                rs.getString("motion_pref")
        ), username, password);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<UserAdminView> findAllUsers() {
        String sql = "SELECT u.user_id, u.username, u.role, u.faculty_id, u.active, f.name AS faculty_name, d.name AS department_name " +
                "FROM users u " +
                "LEFT JOIN faculty f ON f.faculty_id = u.faculty_id " +
                "LEFT JOIN department d ON d.department_id = f.department_id " +
                "ORDER BY u.user_id";
        return jdbcTemplate.query(sql, (rs, i) -> new UserAdminView(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("role"),
                (Integer) rs.getObject("faculty_id"),
                rs.getString("faculty_name"),
                rs.getString("department_name"),
                rs.getInt("active") == 1
        ));
    }

    public void createUser(String username, String password, String role, Integer facultyId) {
        String normalizedRole = role == null ? "FACULTY" : role.trim().toUpperCase();
        Integer userFacultyId = "ADMIN".equals(normalizedRole) ? null : facultyId;
        String sql = "INSERT INTO users(username, password_hash, role, faculty_id, active) VALUES (?, SHA2(?, 256), ?, ?, 1)";
        jdbcTemplate.update(sql, username, password, normalizedRole, userFacultyId);
    }

    public void registerSelfSignup(String email, String password) throws DataIntegrityViolationException {
        String sql = "INSERT INTO users(username, password_hash, role, faculty_id, active) VALUES (?, SHA2(?, 256), 'FACULTY', NULL, 1)";
        jdbcTemplate.update(sql, email, password);
    }

    public void updateUser(int userId, String username, String role, Integer facultyId) {
        String normalizedRole = role == null ? "FACULTY" : role.trim().toUpperCase();
        Integer userFacultyId = "ADMIN".equals(normalizedRole) ? null : facultyId;
        String sql = "UPDATE users SET username=?, role=?, faculty_id=? WHERE user_id=?";
        jdbcTemplate.update(sql, username, normalizedRole, userFacultyId, userId);
    }

    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET active=? WHERE user_id=?";
        jdbcTemplate.update(sql, active ? 1 : 0, userId);
    }

    public void resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash=SHA2(?, 256) WHERE user_id=?";
        jdbcTemplate.update(sql, newPassword, userId);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String sql = "UPDATE users SET password_hash=SHA2(?, 256) " +
                "WHERE username=? AND password_hash=SHA2(?, 256) AND active = 1";
        int updated = jdbcTemplate.update(sql, newPassword, username, oldPassword);
        return updated > 0;
    }

    public void updatePreferences(String username, String theme, String motionPref) {
        String sql = "UPDATE users SET theme=?, motion_pref=? WHERE username=?";
        jdbcTemplate.update(sql, theme, motionPref, username);
    }

    public String createPasswordResetToken(String username) {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isBlank()) {
            return null;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ? AND active = 1",
                Integer.class,
                cleanUsername
        );
        if (count == null || count <= 0) {
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        jdbcTemplate.update("DELETE FROM password_reset WHERE username = ?", cleanUsername);
        jdbcTemplate.update(
                "INSERT INTO password_reset(token, username, expires_at) VALUES(?, ?, DATE_ADD(NOW(), INTERVAL 30 MINUTE))",
                token, cleanUsername
        );
        return token;
    }

    public boolean resetPasswordWithToken(String token, String newPassword) {
        String cleanToken = token == null ? "" : token.trim();
        if (cleanToken.isBlank()) {
            return false;
        }
        List<String> usernames = jdbcTemplate.query(
                "SELECT username FROM password_reset WHERE token = ? AND expires_at > NOW()",
                (rs, i) -> rs.getString("username"),
                cleanToken
        );
        if (usernames.isEmpty()) {
            return false;
        }
        String username = usernames.get(0);
        jdbcTemplate.update("UPDATE users SET password_hash = SHA2(?, 256) WHERE username = ?", newPassword, username);
        jdbcTemplate.update("DELETE FROM password_reset WHERE token = ?", cleanToken);
        return true;
    }
}
