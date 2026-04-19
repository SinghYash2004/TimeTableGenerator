package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.dashboard.DashboardSummary;
import com.erp.web.dashboard.DashboardRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

@Controller
public class DashboardController {
    private final DashboardRepository dashboardRepository;

    public DashboardController(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user != null) {
            return "redirect:/dashboard";
        }
        return "landing";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("summary", dashboardRepository.loadSummary());
        return "dashboard";
    }

    @GetMapping("/api/dashboard/summary")
    @ResponseBody
    public ResponseEntity<?> dashboardSummary(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        DashboardSummary summary = dashboardRepository.loadSummary();
        DashboardLiveSummary payload = new DashboardLiveSummary(
                summary.getTotalTimetableConflicts(),
                summary.getFacultyOverloadPercent(),
                summary.getRoomUtilizationPercent(),
                summary.getBudgetUsagePercent(),
                summary.getAiOptimizationScore(),
                buildInsight(summary)
        );
        return ResponseEntity.ok(payload);
    }

    private String buildInsight(DashboardSummary summary) {
        if (summary.getTotalTimetableConflicts() > 25) {
            return "High conflict pressure detected. Prioritize graph-coloring optimization for upcoming slots.";
        }
        if (summary.getFacultyOverloadPercent() > 30) {
            return "Faculty overload trend is elevated. Rebalance weekly allocation before next schedule cycle.";
        }
        if (summary.getAiOptimizationScore() >= 80) {
            return "System health is stable with high optimization confidence across active departments.";
        }
        return "Operational metrics remain within acceptable variance. Continue monitoring utilization spread.";
    }

    public record DashboardLiveSummary(
            int conflicts,
            double overload,
            double roomUtil,
            double budget,
            int score,
            String insight
    ) {
    }
}
