package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.department.DepartmentRepository;
import com.erp.web.section.SectionAdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SectionController {
    private final SectionAdminRepository sectionRepository;
    private final DepartmentRepository departmentRepository;

    public SectionController(SectionAdminRepository sectionRepository, DepartmentRepository departmentRepository) {
        this.sectionRepository = sectionRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/sections")
    public String sections(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("sections", sectionRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("user", user);
        return "sections";
    }

    @PostMapping("/sections/add")
    public String add(
            HttpSession session,
            @RequestParam String sectionName,
            @RequestParam int semesterNo,
            @RequestParam int strength,
            @RequestParam int departmentId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (sectionName == null || sectionName.isBlank() || semesterNo <= 0 || strength <= 0 || departmentId <= 0) {
            return "redirect:/sections";
        }
        sectionRepository.save(sectionName.trim(), semesterNo, strength, departmentId);
        return "redirect:/sections";
    }

    @PostMapping("/sections/update")
    public String update(
            HttpSession session,
            @RequestParam int sectionId,
            @RequestParam String sectionName,
            @RequestParam int semesterNo,
            @RequestParam int strength,
            @RequestParam int departmentId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (sectionId <= 0 || sectionName == null || sectionName.isBlank() || semesterNo <= 0 || strength <= 0 || departmentId <= 0) {
            return "redirect:/sections";
        }
        sectionRepository.update(sectionId, sectionName.trim(), semesterNo, strength, departmentId);
        return "redirect:/sections";
    }

    @PostMapping("/sections/delete")
    public String delete(HttpSession session, @RequestParam int sectionId) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (sectionId <= 0) {
            return "redirect:/sections";
        }
        sectionRepository.delete(sectionId);
        return "redirect:/sections";
    }
}
