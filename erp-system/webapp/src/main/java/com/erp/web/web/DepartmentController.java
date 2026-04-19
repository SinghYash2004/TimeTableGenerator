package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.department.DepartmentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DepartmentController {
    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/departments")
    public String page(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", user);
        model.addAttribute("departments", departmentRepository.findAll());
        return "departments";
    }

    @PostMapping("/departments/add")
    public String add(
            HttpSession session,
            @RequestParam String name,
            @RequestParam double budgetLimit
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (name == null || name.isBlank() || budgetLimit <= 0) {
            return "redirect:/departments";
        }
        departmentRepository.save(name.trim(), budgetLimit);
        return "redirect:/departments";
    }

    @PostMapping("/departments/update")
    public String update(
            HttpSession session,
            @RequestParam int departmentId,
            @RequestParam String name,
            @RequestParam double budgetLimit
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (departmentId <= 0 || name == null || name.isBlank() || budgetLimit <= 0) {
            return "redirect:/departments";
        }
        departmentRepository.update(departmentId, name.trim(), budgetLimit);
        return "redirect:/departments";
    }

    @PostMapping("/departments/delete")
    public String delete(HttpSession session, @RequestParam int departmentId) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (departmentId <= 0) {
            return "redirect:/departments";
        }
        departmentRepository.delete(departmentId);
        return "redirect:/departments";
    }
}
