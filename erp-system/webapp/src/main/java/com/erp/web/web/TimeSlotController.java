package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.timeslot.TimeSlotRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TimeSlotController {
    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotController(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @GetMapping("/timeslots")
    public String timeslots(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("timeslots", timeSlotRepository.findAll());
        model.addAttribute("user", user);
        return "timeslots";
    }

    @PostMapping("/timeslots/add")
    public String add(
            HttpSession session,
            @RequestParam String day,
            @RequestParam int period
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (day == null || day.isBlank() || period <= 0) {
            return "redirect:/timeslots";
        }
        timeSlotRepository.save(day.trim(), period);
        return "redirect:/timeslots";
    }

    @PostMapping("/timeslots/update")
    public String update(
            HttpSession session,
            @RequestParam int slotId,
            @RequestParam String day,
            @RequestParam int period
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (slotId <= 0 || day == null || day.isBlank() || period <= 0) {
            return "redirect:/timeslots";
        }
        timeSlotRepository.update(slotId, day.trim(), period);
        return "redirect:/timeslots";
    }

    @PostMapping("/timeslots/delete")
    public String delete(HttpSession session, @RequestParam int slotId) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (slotId <= 0) {
            return "redirect:/timeslots";
        }
        timeSlotRepository.delete(slotId);
        return "redirect:/timeslots";
    }
}
