package com.erp.web.web;

import com.erp.web.audit.AuditLogService;
import com.erp.web.auth.UserSession;
import com.erp.web.classroom.RoomAvailabilityRepository;
import com.erp.web.classroom.RoomReservationRepository;
import com.erp.web.timeslot.TimeSlotRepository;
import com.erp.web.timeslot.TimeSlotView;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RoomReservationController {
    private final RoomReservationRepository roomReservationRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AuditLogService auditLogService;

    public RoomReservationController(
            RoomReservationRepository roomReservationRepository,
            RoomAvailabilityRepository roomAvailabilityRepository,
            TimeSlotRepository timeSlotRepository,
            AuditLogService auditLogService
    ) {
        this.roomReservationRepository = roomReservationRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/rooms/reserve")
    public String reserveRoom(
            HttpSession session,
            @RequestParam int roomId,
            @RequestParam int slotId,
            @RequestParam String semester,
            @RequestParam String date,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Integer repeatWeeks,
            @RequestParam(defaultValue = "RESERVE") String reservationType,
            @RequestParam(required = false) String reason
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (roomId <= 0 || slotId <= 0 || semester == null || semester.isBlank()) {
            return "redirect:/rooms/free?message=Invalid%20reservation%20data.";
        }
        LocalDate startDate = parseDate(date);
        if (startDate == null) {
            return "redirect:/rooms/free?message=Invalid%20reservation%20date.";
        }
        int effectiveDuration = (duration == null || duration <= 0) ? 1 : duration;
        int effectiveRepeatWeeks = (repeatWeeks == null || repeatWeeks <= 0) ? 1 : repeatWeeks;
        String type = (reservationType == null || reservationType.isBlank()) ? "RESERVE" : reservationType.trim().toUpperCase();
        if ("MAINTENANCE".equals(type) && !user.isAdmin()) {
            return "redirect:/rooms/free?message=Only%20admins%20can%20set%20maintenance.";
        }

        List<TimeSlotView> slots = getSlotsForRange(slotId, effectiveDuration);
        if (slots.isEmpty()) {
            return "redirect:/rooms/free?message=Not%20enough%20consecutive%20slots.";
        }

        int inserted = 0;
        int skipped = 0;
        for (int i = 0; i < effectiveRepeatWeeks; i++) {
            LocalDate dateIter = startDate.plusWeeks(i);
            if (!roomAvailabilityRepository.isRoomFreeForSlots(semester.trim(), roomId, toSlotIds(slots), dateIter)) {
                skipped += slots.size();
                continue;
            }
            for (TimeSlotView slot : slots) {
                boolean ok = roomReservationRepository.insert(
                        roomId,
                        slot.getSlotId(),
                        semester.trim(),
                        dateIter,
                        user.getFacultyId(),
                        type,
                        normalizeReason(reason),
                        user.getUsername()
                );
                if (ok) {
                    inserted++;
                } else {
                    skipped++;
                }
            }
        }
        auditLogService.record(user.getUsername(), "ROOM_RESERVE",
                "roomId=" + roomId + ", slotId=" + slotId + ", duration=" + effectiveDuration +
                        ", repeatWeeks=" + effectiveRepeatWeeks + ", inserted=" + inserted + ", skipped=" + skipped);
        return "redirect:/rooms/free?message=Reserved%20" + inserted + "%20slots,%20skipped%20" + skipped + ".";
    }

    @PostMapping("/rooms/reserve/delete")
    public String deleteReservation(
            HttpSession session,
            @RequestParam int roomId,
            @RequestParam int slotId,
            @RequestParam String date
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        LocalDate parsedDate = parseDate(date);
        if (roomId <= 0 || slotId <= 0 || parsedDate == null) {
            return "redirect:/rooms/free?message=Invalid%20reservation%20data.";
        }
        roomReservationRepository.delete(roomId, slotId, parsedDate);
        auditLogService.record(user.getUsername(), "ROOM_RESERVE_DELETE",
                "roomId=" + roomId + ", slotId=" + slotId + ", date=" + parsedDate);
        return "redirect:/rooms/free?message=Reservation%20removed.";
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private List<TimeSlotView> getSlotsForRange(int startSlotId, int duration) {
        try {
            TimeSlotView start = timeSlotRepository.findById(startSlotId);
            int endPeriod = start.getPeriod() + duration - 1;
            List<TimeSlotView> slots = timeSlotRepository.findByDayAndPeriodRange(start.getDay(), start.getPeriod(), endPeriod);
            return (slots.size() == duration) ? slots : List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<Integer> toSlotIds(List<TimeSlotView> slots) {
        List<Integer> ids = new ArrayList<>();
        for (TimeSlotView slot : slots) {
            ids.add(slot.getSlotId());
        }
        return ids;
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
