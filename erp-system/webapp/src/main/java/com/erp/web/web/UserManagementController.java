package com.erp.web.web;

import com.erp.web.auth.AuthService;
import com.erp.web.auth.UserSession;
import com.erp.web.audit.AuditLogService;
import com.erp.web.faculty.FacultyLiteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserManagementController {
    private final AuthService authService;
    private final FacultyLiteRepository facultyLiteRepository;
    private final AuditLogService auditLogService;

    public UserManagementController(AuthService authService, FacultyLiteRepository facultyLiteRepository, AuditLogService auditLogService) {
        this.authService = authService;
        this.facultyLiteRepository = facultyLiteRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/settings/users")
    public String users(HttpSession session, @RequestParam(required = false) String message, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", user);
        model.addAttribute("users", authService.findAllUsers());
        model.addAttribute("faculty", facultyLiteRepository.findAll());
        model.addAttribute("message", message);
        return "users";
    }

    @PostMapping("/settings/users/create")
    public String createUser(
            HttpSession session,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) Integer facultyId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }

        String cleanUsername = username == null ? "" : username.trim();
        String cleanRole = role == null ? "" : role.trim().toUpperCase();
        if (!cleanUsername.matches("[A-Za-z0-9_.-]{3,100}")) {
            return "redirect:/settings/users?message=Invalid%20username.%20Use%203-100%20chars:%20letters,%20numbers,%20._-";
        }
        if (password == null || password.length() < 6) {
            return "redirect:/settings/users?message=Password%20must%20be%20at%20least%206%20characters.";
        }
        if (!"ADMIN".equals(cleanRole) && !"FACULTY".equals(cleanRole)) {
            return "redirect:/settings/users?message=Role%20must%20be%20ADMIN%20or%20FACULTY.";
        }
        if ("FACULTY".equals(cleanRole) && (facultyId == null || facultyId <= 0)) {
            return "redirect:/settings/users?message=Faculty%20role%20requires%20a%20valid%20faculty%20selection.";
        }

        try {
            authService.createUser(cleanUsername, password, cleanRole, facultyId);
            auditLogService.record(user.getUsername(), "USER_CREATE", "username=" + cleanUsername + ", role=" + cleanRole);
            return "redirect:/settings/users?message=User%20created%20successfully.";
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/settings/users?message=Unable%20to%20create%20user.%20Username%20may%20already%20exist.";
        }
    }

    @PostMapping("/settings/users/update")
    public String updateUser(
            HttpSession session,
            @RequestParam int userId,
            @RequestParam String username,
            @RequestParam String role,
            @RequestParam(required = false) Integer facultyId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        String cleanUsername = username == null ? "" : username.trim();
        String cleanRole = role == null ? "" : role.trim().toUpperCase();
        if (userId <= 0 || !cleanUsername.matches("[A-Za-z0-9_.-]{3,100}")) {
            return "redirect:/settings/users?message=Invalid%20username.";
        }
        if (!"ADMIN".equals(cleanRole) && !"FACULTY".equals(cleanRole)) {
            return "redirect:/settings/users?message=Role%20must%20be%20ADMIN%20or%20FACULTY.";
        }
        if ("FACULTY".equals(cleanRole) && (facultyId == null || facultyId <= 0)) {
            return "redirect:/settings/users?message=Faculty%20role%20requires%20a%20valid%20faculty%20selection.";
        }
        try {
            authService.updateUser(userId, cleanUsername, cleanRole, facultyId);
            auditLogService.record(user.getUsername(), "USER_UPDATE", "userId=" + userId + ", username=" + cleanUsername + ", role=" + cleanRole);
            return "redirect:/settings/users?message=User%20updated%20successfully.";
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/settings/users?message=Unable%20to%20update%20user.%20Username%20may%20already%20exist.";
        }
    }

    @PostMapping("/settings/users/status")
    public String updateStatus(
            HttpSession session,
            @RequestParam int userId,
            @RequestParam boolean active
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (userId <= 0) {
            return "redirect:/settings/users?message=Invalid%20user.";
        }
        if (user.getUsername().equalsIgnoreCase("admin") && !active) {
            return "redirect:/settings/users?message=Cannot%20deactivate%20primary%20admin.";
        }
        authService.setActive(userId, active);
        auditLogService.record(user.getUsername(), "USER_STATUS", "userId=" + userId + ", active=" + active);
        return "redirect:/settings/users?message=User%20status%20updated.";
    }

    @PostMapping("/settings/users/reset")
    public String resetPassword(
            HttpSession session,
            @RequestParam int userId,
            @RequestParam String password
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (userId <= 0) {
            return "redirect:/settings/users?message=Invalid%20user.";
        }
        if (password == null || password.length() < 6) {
            return "redirect:/settings/users?message=Password%20must%20be%20at%20least%206%20characters.";
        }
        authService.resetPassword(userId, password);
        auditLogService.record(user.getUsername(), "USER_PASSWORD_RESET", "userId=" + userId);
        return "redirect:/settings/users?message=Password%20reset%20successfully.";
    }
}
