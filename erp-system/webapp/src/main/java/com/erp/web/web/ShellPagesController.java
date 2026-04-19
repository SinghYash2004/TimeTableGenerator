package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.audit.AuditLogService;
import com.erp.web.dashboard.DashboardRepository;
import com.erp.web.department.DepartmentRepository;
import com.erp.web.reports.ReportsRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShellPagesController {
    private final DashboardRepository dashboardRepository;
    private final com.erp.web.timetable.TimetableService timetableService;
    private final ReportsRepository reportsRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    public ShellPagesController(
            DashboardRepository dashboardRepository,
            com.erp.web.timetable.TimetableService timetableService,
            ReportsRepository reportsRepository,
            DepartmentRepository departmentRepository,
            AuditLogService auditLogService
    ) {
        this.dashboardRepository = dashboardRepository;
        this.timetableService = timetableService;
        this.reportsRepository = reportsRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/optimization")
    public String optimization(HttpSession session, @RequestParam(required = false) String message, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", user);
        model.addAttribute("summary", dashboardRepository.loadSummary());
        model.addAttribute("message", message);
        return "optimization";
    }

    @PostMapping("/optimization/apply")
    public String applyOptimization(
            HttpSession session,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam int semesterNo,
            @RequestParam(defaultValue = "GRAPH") String algorithm
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        var result = timetableService.generate(semester, departmentId, semesterNo, java.util.List.of(), algorithm);
        auditLogService.record(user.getUsername(), "OPTIMIZATION_APPLY",
                "semester=" + semester + ", department=" + departmentId + ", algorithm=" + algorithm + ", entries=" + result.entries().size());
        return "redirect:/optimization?message=Optimization%20applied.%20Entries:%20" + result.entries().size() + "%20Conflicts:%20" + result.conflicts();
    }

    @PostMapping("/optimization/compare")
    public String compareOptimization(
            HttpSession session,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam int semesterNo,
            @RequestParam(required = false) String sectionIds,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        var ids = parseIds(sectionIds);
        var comparison = timetableService.compareAlgorithms(semester, departmentId, semesterNo, ids);
        model.addAttribute("summary", dashboardRepository.loadSummary());
        model.addAttribute("comparison", comparison);
        model.addAttribute("message", "Comparison completed.");
        auditLogService.record(user.getUsername(), "OPTIMIZATION_COMPARE",
                "semester=" + semester + ", department=" + departmentId + ", sections=" + ids.size());
        return "optimization";
    }

    @PostMapping("/optimization/preview")
    public String previewOptimization(
            HttpSession session,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam int semesterNo,
            @RequestParam(defaultValue = "GRAPH") String algorithm,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        var preview = timetableService.preview(semester, departmentId, semesterNo, java.util.List.of(), algorithm);
        model.addAttribute("user", user);
        model.addAttribute("summary", dashboardRepository.loadSummary());
        model.addAttribute("preview", preview);
        model.addAttribute("message", "What-if preview ready. No data was saved.");
        auditLogService.record(user.getUsername(), "OPTIMIZATION_PREVIEW",
                "semester=" + semester + ", department=" + departmentId + ", algorithm=" + algorithm + ", entries=" + preview.entries().size());
        return "optimization";
    }

    private java.util.List<Integer> parseIds(String raw) {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        for (String p : raw.split(",")) {
            try {
                ids.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    @GetMapping("/reports")
    public String reports(
            HttpSession session,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer departmentId,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        String effectiveSemester = (semester == null || semester.isBlank())
                ? reportsRepository.latestSemester()
                : semester.trim();
        Integer effectiveDepartment = (departmentId != null && departmentId > 0) ? departmentId : null;
        model.addAttribute("summary", dashboardRepository.loadSummary());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("semester", effectiveSemester);
        model.addAttribute("departmentId", effectiveDepartment);
        model.addAttribute("conflicts", reportsRepository.loadConflicts(effectiveSemester, effectiveDepartment));
        model.addAttribute("overloads", reportsRepository.loadFacultyOverloads(effectiveSemester, effectiveDepartment));
        model.addAttribute("rooms", reportsRepository.loadRoomUtilization(effectiveSemester, effectiveDepartment));
        return "reports";
    }

    @GetMapping("/settings")
    public String settings(
            HttpSession session,
            @RequestParam(required = false) String message,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("message", message);
        if (user.isAdmin()) {
            model.addAttribute("auditLogs", auditLogService.recent(50));
        }
        return "settings";
    }

    @GetMapping("/reports/export")
    public void exportReports(
            HttpSession session,
            @RequestParam String type,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer departmentId,
            HttpServletResponse response
    ) throws Exception {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            response.sendError(401);
            return;
        }
        String effectiveSemester = (semester == null || semester.isBlank())
                ? reportsRepository.latestSemester()
                : semester.trim();
        Integer effectiveDepartment = (departmentId != null && departmentId > 0) ? departmentId : null;

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"report_" + type + "_" + effectiveSemester + ".csv\"");
        StringBuilder sb = new StringBuilder();
        if ("conflicts".equalsIgnoreCase(type)) {
            sb.append("type,department,slot,day,period,entity,count\n");
            for (var row : reportsRepository.loadConflicts(effectiveSemester, effectiveDepartment)) {
                sb.append(row.getType()).append(',')
                        .append(row.getDepartmentName()).append(',')
                        .append(row.getSlotId()).append(',')
                        .append(row.getDay()).append(',')
                        .append(row.getPeriod()).append(',')
                        .append(row.getEntityName()).append(',')
                        .append(row.getCount()).append('\n');
            }
        } else if ("overloads".equalsIgnoreCase(type)) {
            sb.append("faculty,department,assigned,max_hours\n");
            for (var row : reportsRepository.loadFacultyOverloads(effectiveSemester, effectiveDepartment)) {
                sb.append(row.getFacultyName()).append(',')
                        .append(row.getDepartmentName()).append(',')
                        .append(row.getAssigned()).append(',')
                        .append(row.getMaxHours()).append('\n');
            }
        } else if ("rooms".equalsIgnoreCase(type)) {
            sb.append("room,assigned_slots,total_slots,utilization_percent\n");
            for (var row : reportsRepository.loadRoomUtilization(effectiveSemester, effectiveDepartment)) {
                sb.append(row.getRoomCode()).append(',')
                        .append(row.getAssignedSlots()).append(',')
                        .append(row.getTotalSlots()).append(',')
                        .append(String.format(java.util.Locale.US, "%.2f", row.getUtilizationPercent())).append('\n');
            }
        } else {
            response.sendError(400);
            return;
        }
        auditLogService.record(user.getUsername(), "REPORT_EXPORT",
                "type=" + type + ", semester=" + effectiveSemester + ", department=" + (effectiveDepartment == null ? "ALL" : effectiveDepartment));
        response.getWriter().write(sb.toString());
    }
}
