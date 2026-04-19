package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.department.DepartmentRepository;
import com.erp.web.subject.SubjectRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SubjectController {
    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;

    public SubjectController(SubjectRepository subjectRepository, DepartmentRepository departmentRepository) {
        this.subjectRepository = subjectRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/subjects")
    public String subjects(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("user", user);
        return "subjects";
    }

    @PostMapping("/subjects/add")
    public String add(
            HttpSession session,
            @RequestParam String name,
            @RequestParam int weeklyHours,
            @RequestParam int departmentId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (name == null || name.isBlank() || weeklyHours <= 0 || departmentId <= 0) {
            return "redirect:/subjects";
        }
        subjectRepository.save(name.trim(), weeklyHours, departmentId);
        return "redirect:/subjects";
    }

    @PostMapping("/subjects/update")
    public String update(
            HttpSession session,
            @RequestParam int subjectId,
            @RequestParam String name,
            @RequestParam int weeklyHours,
            @RequestParam int departmentId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (subjectId <= 0 || name == null || name.isBlank() || weeklyHours <= 0 || departmentId <= 0) {
            return "redirect:/subjects";
        }
        subjectRepository.update(subjectId, name.trim(), weeklyHours, departmentId);
        return "redirect:/subjects";
    }

    @PostMapping("/subjects/delete")
    public String delete(HttpSession session, @RequestParam int subjectId) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (subjectId <= 0) {
            return "redirect:/subjects";
        }
        subjectRepository.delete(subjectId);
        return "redirect:/subjects";
    }
}
