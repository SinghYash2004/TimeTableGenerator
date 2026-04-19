package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.availability.FacultyAvailabilityRepository;
import com.erp.web.faculty.FacultyRepository;
import com.erp.web.timeslot.TimeSlotRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AvailabilityController {
    private final FacultyAvailabilityRepository availabilityRepository;
    private final FacultyRepository facultyRepository;
    private final TimeSlotRepository timeSlotRepository;

    public AvailabilityController(
            FacultyAvailabilityRepository availabilityRepository,
            FacultyRepository facultyRepository,
            TimeSlotRepository timeSlotRepository
    ) {
        this.availabilityRepository = availabilityRepository;
        this.facultyRepository = facultyRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @GetMapping("/availability")
    public String availability(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("availability", availabilityRepository.findAll());
        model.addAttribute("faculty", facultyRepository.findAll());
        model.addAttribute("timeslots", timeSlotRepository.findAll());
        model.addAttribute("user", user);
        return "availability";
    }

    @PostMapping("/availability/add")
    public String add(
            HttpSession session,
            @RequestParam int facultyId,
            @RequestParam int slotId,
            @RequestParam(defaultValue = "true") boolean available
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (facultyId <= 0 || slotId <= 0) {
            return "redirect:/availability";
        }
        availabilityRepository.upsert(facultyId, slotId, available);
        return "redirect:/availability";
    }

    @PostMapping("/availability/update")
    public String update(
            HttpSession session,
            @RequestParam int facultyId,
            @RequestParam int slotId,
            @RequestParam(defaultValue = "true") boolean available
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (facultyId <= 0 || slotId <= 0) {
            return "redirect:/availability";
        }
        availabilityRepository.upsert(facultyId, slotId, available);
        return "redirect:/availability";
    }

    @PostMapping("/availability/delete")
    public String delete(
            HttpSession session,
            @RequestParam int facultyId,
            @RequestParam int slotId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (facultyId <= 0 || slotId <= 0) {
            return "redirect:/availability";
        }
        availabilityRepository.delete(facultyId, slotId);
        return "redirect:/availability";
    }
}
