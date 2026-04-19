package com.erp.web.web;

import com.erp.web.auth.AuthService;
import com.erp.web.auth.UserSession;
import com.erp.web.audit.AuditLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SettingsController {
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public SettingsController(AuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/settings/password")
    public String changePassword(
            HttpSession session,
            @RequestParam String currentPassword,
            @RequestParam String newPassword
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (newPassword == null || newPassword.length() < 6) {
            return "redirect:/settings?message=Password%20must%20be%20at%20least%206%20characters.";
        }
        boolean ok = authService.changePassword(user.getUsername(), currentPassword, newPassword);
        if (!ok) {
            return "redirect:/settings?message=Current%20password%20is%20incorrect.";
        }
        auditLogService.record(user.getUsername(), "PASSWORD_CHANGE", "Password updated");
        return "redirect:/settings?message=Password%20updated%20successfully.";
    }

    @PostMapping("/settings/preferences")
    public String updatePreferences(
            HttpSession session,
            @RequestParam String theme,
            @RequestParam String motion
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String cleanTheme = switch ((theme == null ? "" : theme.toLowerCase())) {
            case "dark" -> "dark";
            case "peach-aqua" -> "peach-aqua";
            case "pearl-charcoal" -> "pearl-charcoal";
            case "cloudy-ocean" -> "cloudy-ocean";
            case "soft-olive" -> "soft-olive";
            default -> "light";
        };
        String cleanMotion = switch ((motion == null ? "" : motion.toLowerCase())) {
            case "reduce" -> "reduce";
            case "full" -> "full";
            default -> "system";
        };
        authService.updatePreferences(user.getUsername(), cleanTheme, cleanMotion);
        session.setAttribute(
                "user",
                new UserSession(
                        user.getUsername(),
                        user.getRole(),
                        user.getFacultyId(),
                        user.getDepartmentId(),
                        cleanTheme,
                        cleanMotion
                )
        );
        auditLogService.record(user.getUsername(), "PREFERENCES_UPDATE", "theme=" + cleanTheme + ", motion=" + cleanMotion);
        return "redirect:/settings?message=Preferences%20saved.";
    }
}
