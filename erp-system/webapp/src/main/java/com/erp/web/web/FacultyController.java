package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.department.DepartmentRepository;
import com.erp.web.faculty.FacultyRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FacultyController {
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    public FacultyController(FacultyRepository facultyRepository, DepartmentRepository departmentRepository) {
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/faculty")
    public String faculty(
            HttpSession session,
            @RequestParam(required = false) String message,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("faculty", facultyRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("user", user);
        model.addAttribute("message", message);
        return "faculty";
    }

    @PostMapping("/faculty/add")
    public String add(
            HttpSession session,
            @RequestParam String name,
            @RequestParam int departmentId,
            @RequestParam int maxHoursPerWeek,
            @RequestParam double costPerHour
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (name == null || name.isBlank() || departmentId <= 0 || maxHoursPerWeek <= 0 || costPerHour < 0) {
            return "redirect:/faculty";
        }
        facultyRepository.save(name.trim(), departmentId, maxHoursPerWeek, costPerHour);
        return "redirect:/faculty";
    }

    @PostMapping("/faculty/update")
    public String update(
            HttpSession session,
            @RequestParam int facultyId,
            @RequestParam String name,
            @RequestParam int departmentId,
            @RequestParam int maxHoursPerWeek,
            @RequestParam double costPerHour
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (facultyId <= 0 || name == null || name.isBlank() || departmentId <= 0 || maxHoursPerWeek <= 0 || costPerHour < 0) {
            return "redirect:/faculty";
        }
        facultyRepository.update(facultyId, name.trim(), departmentId, maxHoursPerWeek, costPerHour);
        return "redirect:/faculty";
    }

    @PostMapping("/faculty/delete")
    public String delete(HttpSession session, @RequestParam int facultyId) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (facultyId <= 0) {
            return "redirect:/faculty";
        }
        try {
            facultyRepository.delete(facultyId);
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/faculty?message=Cannot%20delete%20faculty.%20Remove%20dependent%20records%20first.";
        }
        return "redirect:/faculty";
    }
}
