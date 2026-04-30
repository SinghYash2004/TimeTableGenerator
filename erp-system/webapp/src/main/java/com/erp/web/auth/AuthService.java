package com.erp.web.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {
    private final JdbcTemplate jdbcTemplate;

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UserSession authenticate(String username, String password) {
        try {
            String sql = "SELECT u.username, u.role, u.faculty_id, f.department_id, u.theme, u.motion_pref, u.password_hash " +
                    "FROM users u LEFT JOIN faculty f ON f.faculty_id = u.faculty_id " +
                    "WHERE u.username = ? AND u.active = true";
            List<UserSession> rows = jdbcTemplate.query(sql, (rs, i) -> {
                String dbHash = rs.getString("password_hash");
                if (dbHash != null && dbHash.equals(md5(password))) {
                    return new UserSession(
                            rs.getString("username"),
                            rs.getString("role"),
                            (Integer) rs.getObject("faculty_id"),
                            (Integer) rs.getObject("department_id"),
                            rs.getString("theme"),
                            rs.getString("motion_pref")
                    );
                }
                return null;
            }, username);
            
            return rows.stream().filter(java.util.Objects::nonNull).findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
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
                rs.getBoolean("active")
        ));
    }

    public void createUser(String username, String password, String role, Integer facultyId) {
        String normalizedRole = role == null ? "FACULTY" : role.trim().toUpperCase();
        Integer userFacultyId = "ADMIN".equals(normalizedRole) ? null : facultyId;
        String sql = "INSERT INTO users(username, password_hash, role, faculty_id, active) VALUES (?, ?, ?, ?, true)";
        jdbcTemplate.update(sql, username, md5(password), normalizedRole, userFacultyId);
    }

    public void registerSelfSignup(String email, String password) throws DataIntegrityViolationException {
        String sql = "INSERT INTO users(username, password_hash, role, faculty_id, active) VALUES (?, ?, 'FACULTY', NULL, true)";
        jdbcTemplate.update(sql, email, md5(password));
    }

    public void updateUser(int userId, String username, String role, Integer facultyId) {
        String normalizedRole = role == null ? "FACULTY" : role.trim().toUpperCase();
        Integer userFacultyId = "ADMIN".equals(normalizedRole) ? null : facultyId;
        String sql = "UPDATE users SET username=?, role=?, faculty_id=? WHERE user_id=?";
        jdbcTemplate.update(sql, username, normalizedRole, userFacultyId, userId);
    }

    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET active=? WHERE user_id=?";
        jdbcTemplate.update(sql, active, userId);
    }

    public void resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash=? WHERE user_id=?";
        jdbcTemplate.update(sql, md5(newPassword), userId);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String sql = "UPDATE users SET password_hash=? " +
                "WHERE username=? AND password_hash=? AND active = true";
        int updated = jdbcTemplate.update(sql, md5(newPassword), username, md5(oldPassword));
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
                "SELECT COUNT(*) FROM users WHERE username = ? AND active = true",
                Integer.class,
                cleanUsername
        );
        if (count == null || count <= 0) {
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        jdbcTemplate.update("DELETE FROM password_reset WHERE username = ?", cleanUsername);
        jdbcTemplate.update(
                "INSERT INTO password_reset(token, username, expires_at) VALUES(?, ?, NOW() + interval '30 minutes')",
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
        jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE username = ?", md5(newPassword), username);
        jdbcTemplate.update("DELETE FROM password_reset WHERE token = ?", cleanToken);
        return true;
    }
}
