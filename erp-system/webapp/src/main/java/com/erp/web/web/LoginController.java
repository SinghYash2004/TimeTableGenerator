package com.erp.web.web;

import com.erp.web.auth.AuthService;
import com.erp.web.auth.UserSession;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false, defaultValue = "signin") String mode,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String resetToken,
            Model model
    ) {
        model.addAttribute("mode", mode);
        model.addAttribute("message", message);
        model.addAttribute("resetToken", resetToken);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String rememberMe,
            HttpSession session,
            HttpServletResponse response,
            Model model
    ) {
        UserSession user = authService.authenticate(username, password);
        if (user == null) {
            model.addAttribute("error", "Invalid credentials.");
            model.addAttribute("mode", "signin");
            return "login";
        }
        session.setAttribute("user", user);
        if (rememberMe != null) {
            int maxAge = 60 * 60 * 24 * 30;
            session.setMaxInactiveInterval(maxAge);
            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            cookie.setMaxAge(maxAge);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/login/signup")
    public String signup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        if (cleanName.length() < 2) {
            model.addAttribute("signupError", "Name must be at least 2 characters.");
            model.addAttribute("mode", "signup");
            return "login";
        }
        if (!cleanEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            model.addAttribute("signupError", "Enter a valid email address.");
            model.addAttribute("mode", "signup");
            return "login";
        }
        if (password == null || password.length() < 6) {
            model.addAttribute("signupError", "Password must be at least 6 characters.");
            model.addAttribute("mode", "signup");
            return "login";
        }
        try {
            authService.registerSelfSignup(cleanEmail, password);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("signupError", "This email is already registered.");
            model.addAttribute("mode", "signup");
            return "login";
        }
        UserSession user = authService.authenticate(cleanEmail, password);
        if (user == null) {
            model.addAttribute("signupError", "Account created, but auto sign-in failed. Please sign in.");
            model.addAttribute("mode", "signin");
            return "login";
        }
        session.setAttribute("user", user);
        return "redirect:/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

    @PostMapping("/login/forgot")
    public String forgotPassword(
            @RequestParam String username,
            Model model
    ) {
        String token = authService.createPasswordResetToken(username);
        if (token == null) {
            model.addAttribute("message", "Unable to create reset token. Check username.");
            model.addAttribute("mode", "forgot");
            return "login";
        }
        model.addAttribute("message", "Reset token generated. Use it below to set a new password.");
        model.addAttribute("resetToken", token);
        model.addAttribute("mode", "reset");
        return "login";
    }

    @PostMapping("/login/reset")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            Model model
    ) {
        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("message", "Password must be at least 6 characters.");
            model.addAttribute("mode", "reset");
            model.addAttribute("resetToken", token);
            return "login";
        }
        boolean ok = authService.resetPasswordWithToken(token, newPassword);
        if (!ok) {
            model.addAttribute("message", "Reset token is invalid or expired.");
            model.addAttribute("mode", "reset");
            model.addAttribute("resetToken", token);
            return "login";
        }
        model.addAttribute("message", "Password reset successful. Please sign in.");
        model.addAttribute("mode", "signin");
        return "login";
    }
}
