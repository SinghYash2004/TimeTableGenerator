package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.classroom.RoomAvailabilityRepository;
import com.erp.web.classroom.RoomAvailabilityView;
import com.erp.web.classroom.RoomReservationRepository;
import com.erp.web.classroom.RoomReservationView;
import com.erp.web.classroom.RoomStatusView;
import com.erp.web.classroom.RoomRepository;
import com.erp.web.timeslot.TimeSlotRepository;
import com.erp.web.timeslot.TimeSlotView;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RoomAvailabilityController {
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final RoomReservationRepository roomReservationRepository;

    public RoomAvailabilityController(
            RoomAvailabilityRepository roomAvailabilityRepository,
            RoomRepository roomRepository,
            TimeSlotRepository timeSlotRepository,
            RoomReservationRepository roomReservationRepository
    ) {
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.roomReservationRepository = roomReservationRepository;
    }

    @GetMapping("/rooms/free")
    public String freeRooms(
            HttpSession session,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer slotId,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Integer repeatWeeks,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String equipmentTags,
            @RequestParam(required = false, defaultValue = "false") boolean showBlocked,
            @RequestParam(required = false) String message,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String effectiveSemester = (semester == null || semester.isBlank()) ? "2026S1" : semester.trim();
        LocalDate parsedDate = parseDate(date);
        int effectiveDuration = (duration == null || duration <= 0) ? 1 : duration;
        int effectiveRepeatWeeks = (repeatWeeks == null || repeatWeeks <= 0) ? 1 : repeatWeeks;

        List<RoomAvailabilityView> results = List.of();
        List<RoomStatusView> blockedRooms = List.of();
        List<RoomSuggestion> suggestions = List.of();
        List<RoomReservationView> reservationHistory = roomReservationRepository.recent(20);
        String messageText = message;

        if (slotId != null && slotId > 0 && parsedDate != null) {
            List<TimeSlotView> slots = getSlotsForRange(slotId, effectiveDuration);
            if (slots.isEmpty()) {
                messageText = "Not enough consecutive slots for the selected duration.";
            } else {
                List<LocalDate> dates = buildRecurringDates(parsedDate, effectiveRepeatWeeks);
                results = findRecurringFreeRooms(
                        effectiveSemester,
                        slots,
                        building,
                        minCapacity,
                        roomType,
                        equipmentTags,
                        dates
                );
                if (results.isEmpty() && effectiveRepeatWeeks == 1) {
                    suggestions = buildSuggestions(
                            effectiveSemester,
                            slots,
                            building,
                            minCapacity,
                            roomType,
                            equipmentTags,
                            parsedDate,
                            3
                    );
                }
                if (showBlocked && effectiveRepeatWeeks == 1) {
                    blockedRooms = roomAvailabilityRepository.findRoomStatusesForSlots(
                            effectiveSemester,
                            toSlotIds(slots),
                            building,
                            minCapacity,
                            roomType,
                            parseTags(equipmentTags),
                            parsedDate
                    );
                }
            }
        } else if (slotId != null || date != null) {
            messageText = "Please select a valid time slot and date.";
        }

        model.addAttribute("user", user);
        model.addAttribute("semester", effectiveSemester);
        model.addAttribute("slotId", slotId == null ? 0 : slotId);
        model.addAttribute("building", building == null ? "" : building);
        model.addAttribute("date", parsedDate == null ? "" : parsedDate.toString());
        model.addAttribute("duration", effectiveDuration);
        model.addAttribute("repeatWeeks", effectiveRepeatWeeks);
        model.addAttribute("minCapacity", minCapacity == null ? 0 : minCapacity);
        model.addAttribute("roomType", roomType == null ? "" : roomType);
        model.addAttribute("equipmentTags", equipmentTags == null ? "" : equipmentTags);
        model.addAttribute("showBlocked", showBlocked);
        model.addAttribute("message", messageText);
        model.addAttribute("timeslots", timeSlotRepository.findAll());
        model.addAttribute("buildings", roomRepository.findBuildings());
        model.addAttribute("roomTypes", roomRepository.findRoomTypes());
        model.addAttribute("results", results);
        model.addAttribute("blockedRooms", blockedRooms);
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("reservationHistory", reservationHistory);
        return "rooms-free";
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

    private List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> tags = new ArrayList<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                tags.add(trimmed);
            }
        }
        return tags;
    }

    private List<LocalDate> buildRecurringDates(LocalDate start, int repeatWeeks) {
        List<LocalDate> dates = new ArrayList<>();
        dates.add(start);
        for (int i = 1; i < repeatWeeks; i++) {
            dates.add(start.plusWeeks(i));
        }
        return dates;
    }

    private List<RoomAvailabilityView> findRecurringFreeRooms(
            String semester,
            List<TimeSlotView> slots,
            String building,
            Integer minCapacity,
            String roomType,
            String equipmentTags,
            List<LocalDate> dates
    ) {
        List<Integer> slotIds = toSlotIds(slots);
        List<String> tags = parseTags(equipmentTags);
        Map<Integer, RoomAvailabilityView> intersection = new HashMap<>();

        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = dates.get(i);
            List<RoomAvailabilityView> freeRooms = roomAvailabilityRepository.findFreeRoomsForSlots(
                    semester,
                    slotIds,
                    building,
                    minCapacity,
                    roomType,
                    tags,
                    date
            );
            if (i == 0) {
                for (RoomAvailabilityView room : freeRooms) {
                    intersection.put(room.getRoomId(), room);
                }
            } else {
                intersection.keySet().retainAll(freeRooms.stream().map(RoomAvailabilityView::getRoomId).toList());
            }
            if (intersection.isEmpty()) {
                break;
            }
        }
        return new ArrayList<>(intersection.values());
    }

    private List<RoomSuggestion> buildSuggestions(
            String semester,
            List<TimeSlotView> baseSlots,
            String building,
            Integer minCapacity,
            String roomType,
            String equipmentTags,
            LocalDate date,
            int limit
    ) {
        List<RoomSuggestion> suggestions = new ArrayList<>();
        if (baseSlots.isEmpty()) {
            return suggestions;
        }
        TimeSlotView start = baseSlots.get(0);
        List<TimeSlotView> nextSlots = timeSlotRepository.findNextByDayPeriod(start.getDay(), start.getPeriod(), limit);
        for (TimeSlotView next : nextSlots) {
            List<TimeSlotView> range = getSlotsForRange(next.getSlotId(), baseSlots.size());
            if (range.isEmpty()) {
                continue;
            }
            List<RoomAvailabilityView> free = roomAvailabilityRepository.findFreeRoomsForSlots(
                    semester,
                    toSlotIds(range),
                    building,
                    minCapacity,
                    roomType,
                    parseTags(equipmentTags),
                    date
            );
            suggestions.add(new RoomSuggestion(next.getDay(), next.getPeriod(), free));
        }
        return suggestions;
    }

    public static final class RoomSuggestion {
        private final String day;
        private final int period;
        private final List<RoomAvailabilityView> rooms;

        public RoomSuggestion(String day, int period, List<RoomAvailabilityView> rooms) {
            this.day = day;
            this.period = period;
            this.rooms = rooms;
        }

        public String getDay() {
            return day;
        }

        public int getPeriod() {
            return period;
        }

        public List<RoomAvailabilityView> getRooms() {
            return rooms;
        }
    }
}
