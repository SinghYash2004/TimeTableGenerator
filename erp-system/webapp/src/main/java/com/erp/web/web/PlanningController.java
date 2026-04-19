package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.department.DepartmentRepository;
import com.erp.web.section.SectionAdminRepository;
import com.erp.web.subject.SubjectRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlanningController {
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final SectionAdminRepository sectionRepository;

    public PlanningController(
            DepartmentRepository departmentRepository,
            SubjectRepository subjectRepository,
            SectionAdminRepository sectionRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.sectionRepository = sectionRepository;
    }

    @GetMapping("/planning")
    public String planning(
            HttpSession session,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer semesterNo,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        var departments = departmentRepository.findAll();
        Integer effectiveDepartment = departmentId;
        if (effectiveDepartment == null || effectiveDepartment <= 0) {
            effectiveDepartment = departments.isEmpty() ? null : departments.get(0).getDepartmentId();
        }
        int effectiveSemester = (semesterNo == null || semesterNo <= 0) ? 1 : semesterNo;

        model.addAttribute("departments", departments);
        model.addAttribute("departmentId", effectiveDepartment);
        model.addAttribute("semesterNo", effectiveSemester);
        model.addAttribute("user", user);

        if (effectiveDepartment != null) {
            model.addAttribute("subjects", subjectRepository.findByDepartment(effectiveDepartment));
            model.addAttribute("sections", sectionRepository.findByDepartmentAndSemester(effectiveDepartment, effectiveSemester));
        } else {
            model.addAttribute("subjects", java.util.List.of());
            model.addAttribute("sections", java.util.List.of());
        }

        return "planning";
    }
}
