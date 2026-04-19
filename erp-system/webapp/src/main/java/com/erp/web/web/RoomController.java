package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.classroom.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoomController {
    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/rooms")
    public String rooms(HttpSession session, Model model) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("buildings", roomRepository.findBuildings());
        model.addAttribute("roomTypes", roomRepository.findRoomTypes());
        model.addAttribute("user", user);
        return "rooms";
    }

    @PostMapping("/rooms/add")
    public String add(
            HttpSession session,
            @RequestParam String roomCode,
            @RequestParam(defaultValue = "MAIN") String building,
            @RequestParam(defaultValue = "0") int floorNo,
            @RequestParam(defaultValue = "LECTURE") String roomType,
            @RequestParam(required = false) String equipmentTags,
            @RequestParam int capacity,
            @RequestParam double costPerHour
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (roomCode == null || roomCode.isBlank() || building == null || building.isBlank() || roomType == null || roomType.isBlank() || capacity <= 0 || costPerHour < 0) {
            return "redirect:/rooms";
        }
        roomRepository.save(
                roomCode.trim(),
                building.trim(),
                floorNo,
                roomType.trim(),
                normalizeTags(equipmentTags),
                capacity,
                costPerHour
        );
        return "redirect:/rooms";
    }

    @PostMapping("/rooms/update")
    public String update(
            HttpSession session,
            @RequestParam int roomId,
            @RequestParam String roomCode,
            @RequestParam(defaultValue = "MAIN") String building,
            @RequestParam(defaultValue = "0") int floorNo,
            @RequestParam(defaultValue = "LECTURE") String roomType,
            @RequestParam(required = false) String equipmentTags,
            @RequestParam int capacity,
            @RequestParam double costPerHour
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (roomId <= 0 || roomCode == null || roomCode.isBlank() || building == null || building.isBlank() || roomType == null || roomType.isBlank() || capacity <= 0 || costPerHour < 0) {
            return "redirect:/rooms";
        }
        roomRepository.update(
                roomId,
                roomCode.trim(),
                building.trim(),
                floorNo,
                roomType.trim(),
                normalizeTags(equipmentTags),
                capacity,
                costPerHour
        );
        return "redirect:/rooms";
    }

    private String normalizeTags(String tags) {
        if (tags == null) {
            return null;
        }
        String trimmed = tags.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @PostMapping("/rooms/delete")
    public String delete(HttpSession session, @RequestParam int roomId) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (roomId <= 0) {
            return "redirect:/rooms";
        }
        roomRepository.delete(roomId);
        return "redirect:/rooms";
    }
}
