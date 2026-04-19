package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.classroom.RoomRepository;
import com.erp.web.department.DepartmentRepository;
import com.erp.web.faculty.FacultyRepository;
import com.erp.web.section.SectionRepository;
import com.erp.web.section.SectionAdminRepository;
import com.erp.web.subject.SubjectRepository;
import com.erp.web.audit.AuditLogService;
import com.erp.web.classroom.RoomAvailabilityRepository;
import com.erp.web.classroom.RoomReservationRepository;
import com.erp.web.timetable.ClassCancellationRepository;
import com.erp.web.timetable.TimetableRepository;
import com.erp.web.timetable.TimetableRowView;
import com.erp.web.timetable.TimetableService;
import com.erp.web.timeslot.TimeSlotRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Controller
public class TimetableController {
    private final TimetableRepository timetableRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;
    private final TimetableService timetableService;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final SectionAdminRepository sectionAdminRepository;
    private final ClassCancellationRepository classCancellationRepository;
    private final RoomReservationRepository roomReservationRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final AuditLogService auditLogService;

    public TimetableController(
            TimetableRepository timetableRepository,
            DepartmentRepository departmentRepository,
            SectionRepository sectionRepository,
            TimetableService timetableService,
            SubjectRepository subjectRepository,
            FacultyRepository facultyRepository,
            RoomRepository roomRepository,
            TimeSlotRepository timeSlotRepository,
            SectionAdminRepository sectionAdminRepository,
            ClassCancellationRepository classCancellationRepository,
            RoomReservationRepository roomReservationRepository,
            RoomAvailabilityRepository roomAvailabilityRepository,
            AuditLogService auditLogService
    ) {
        this.timetableRepository = timetableRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
        this.timetableService = timetableService;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.sectionAdminRepository = sectionAdminRepository;
        this.classCancellationRepository = classCancellationRepository;
        this.roomReservationRepository = roomReservationRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/timetable")
    public String timetable(
            HttpSession session,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String semesterNo,
            @RequestParam(required = false) String previewSectionId,
            @RequestParam(required = false) String message,
            Model model
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);

        String effectiveSemester = (semester == null || semester.isBlank()) ? "2026S1" : semester.trim();
        Integer effectiveDepartment = parseOptionalInt(departmentId);
        if (!user.isAdmin()) {
            effectiveDepartment = user.getDepartmentId();
        }
        Integer parsedSemesterNo = parseOptionalInt(semesterNo);
        int effectiveSemesterNo = (parsedSemesterNo == null || parsedSemesterNo <= 0) ? 4 : parsedSemesterNo;

        var departments = departmentRepository.findAll();
        if (effectiveDepartment == null && user.isAdmin() && !departments.isEmpty()) {
            effectiveDepartment = departments.get(0).getDepartmentId();
        }
        model.addAttribute("departments", departments);
        model.addAttribute("semester", effectiveSemester);
        model.addAttribute("departmentId", effectiveDepartment);
        model.addAttribute("semesterNo", effectiveSemesterNo);
        model.addAttribute("message", message);

        if (effectiveDepartment == null || effectiveDepartment <= 0) {
            model.addAttribute("rows", java.util.List.of());
            model.addAttribute("sections", java.util.List.of());
            model.addAttribute("message", "No department assigned.");
            return "timetable";
        }

        List<com.erp.web.section.SectionView> sections = sectionRepository.findByDepartmentAndSemester(effectiveDepartment, effectiveSemesterNo);
        model.addAttribute("sections", sections);

        model.addAttribute("subjects", subjectRepository.findByDepartment(effectiveDepartment));
        model.addAttribute("faculty", facultyRepository.findByDepartment(effectiveDepartment));
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("timeslots", timeSlotRepository.findAll());
        model.addAttribute("adminSections", (effectiveDepartment == null)
                ? java.util.List.of()
                : sectionAdminRepository.findByDepartmentAndSemester(effectiveDepartment, effectiveSemesterNo));

        List<TimetableRowView> rows = timetableRepository.findBySemesterAndDepartment(effectiveSemester, effectiveDepartment);
        model.addAttribute("rows", rows);

        Integer selectedPreviewSection = parseOptionalInt(previewSectionId);
        if (selectedPreviewSection == null || selectedPreviewSection <= 0) {
            selectedPreviewSection = sections.isEmpty() ? null : sections.get(0).getSectionId();
        }
        model.addAttribute("previewSectionId", selectedPreviewSection);

        String previewSectionName = null;
        if (selectedPreviewSection != null) {
            for (var section : sections) {
                if (section.getSectionId() == selectedPreviewSection) {
                    previewSectionName = section.getSectionName();
                    break;
                }
            }
        }
        model.addAttribute("previewSectionName", previewSectionName);
        String previewDepartmentName = rows.isEmpty() ? null : rows.get(0).getDepartmentName();
        model.addAttribute("previewDepartmentName", previewDepartmentName);

        PreviewTable previewTable = buildPreview(rows, selectedPreviewSection);
        model.addAttribute("previewPeriods", previewTable.periods);
        model.addAttribute("previewRows", previewTable.rows);
        model.addAttribute("previewLegend", previewTable.legend);
        return "timetable";
    }

    @PostMapping("/timetable/add")
    public String addEntry(
            HttpSession session,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam int facultyId,
            @RequestParam int subjectId,
            @RequestParam int roomId,
            @RequestParam int slotId,
            @RequestParam(required = false) Integer sectionId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (semester == null || semester.isBlank() || departmentId <= 0 || facultyId <= 0 || subjectId <= 0 || roomId <= 0 || slotId <= 0) {
            return "redirect:/timetable?message=" + encode("Invalid timetable entry data.");
        }
        try {
            timetableRepository.insert(semester.trim(), facultyId, subjectId, roomId, slotId, departmentId, sectionId);
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Entry conflicts with existing data.");
        }
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Entry added.");
    }

    @PostMapping("/timetable/update")
    public String updateEntry(
            HttpSession session,
            @RequestParam int entryId,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam int facultyId,
            @RequestParam int subjectId,
            @RequestParam int roomId,
            @RequestParam int slotId,
            @RequestParam(required = false) Integer sectionId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (entryId <= 0 || semester == null || semester.isBlank() || departmentId <= 0 || facultyId <= 0 || subjectId <= 0 || roomId <= 0 || slotId <= 0) {
            return "redirect:/timetable?message=" + encode("Invalid timetable entry data.");
        }
        try {
            timetableRepository.update(entryId, semester.trim(), facultyId, subjectId, roomId, slotId, departmentId, sectionId);
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Update conflicts with existing data.");
        }
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Entry updated.");
    }

    @PostMapping("/timetable/delete")
    public String deleteEntry(
            HttpSession session,
            @RequestParam int entryId,
            @RequestParam String semester,
            @RequestParam int departmentId
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (entryId <= 0) {
            return "redirect:/timetable?message=" + encode("Invalid entry.");
        }
        timetableRepository.delete(entryId);
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Entry deleted.");
    }

    @PostMapping("/timetable/cancel")
    public String cancelEntry(
            HttpSession session,
            @RequestParam int entryId,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam String cancelDate,
            @RequestParam(required = false) String reason
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (entryId <= 0 || cancelDate == null || cancelDate.isBlank()) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Invalid cancellation data.");
        }
        if (!user.isAdmin()) {
            Integer facultyId = timetableRepository.findFacultyIdForEntry(entryId);
            if (facultyId == null || user.getFacultyId() == null || !facultyId.equals(user.getFacultyId())) {
                return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Not allowed to cancel this class.");
            }
        }
        LocalDate date = parseDate(cancelDate);
        if (date == null) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Invalid cancellation date.");
        }
        classCancellationRepository.upsert(entryId, date, normalizeReason(reason), user.getUsername());
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Class canceled for " + date + ".");
    }

    @PostMapping("/timetable/uncancel")
    public String uncancelEntry(
            HttpSession session,
            @RequestParam int entryId,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam String cancelDate
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (entryId <= 0 || cancelDate == null || cancelDate.isBlank()) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Invalid cancellation data.");
        }
        if (!user.isAdmin()) {
            Integer facultyId = timetableRepository.findFacultyIdForEntry(entryId);
            if (facultyId == null || user.getFacultyId() == null || !facultyId.equals(user.getFacultyId())) {
                return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Not allowed to update this class.");
            }
        }
        LocalDate date = parseDate(cancelDate);
        if (date == null) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Invalid cancellation date.");
        }
        classCancellationRepository.delete(entryId, date);
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Cancellation removed for " + date + ".");
    }

    @PostMapping("/timetable/rebook")
    public String rebookEntry(
            HttpSession session,
            @RequestParam int entryId,
            @RequestParam String semester,
            @RequestParam int departmentId,
            @RequestParam int newRoomId,
            @RequestParam int newSlotId,
            @RequestParam String date,
            @RequestParam(required = false) String reason
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (entryId <= 0 || newRoomId <= 0 || newSlotId <= 0 || semester == null || semester.isBlank()) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Invalid rebook data.");
        }
        Integer facultyId = timetableRepository.findFacultyIdForEntry(entryId);
        if (!user.isAdmin()) {
            if (facultyId == null || user.getFacultyId() == null || !facultyId.equals(user.getFacultyId())) {
                return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Not allowed to rebook this class.");
            }
        }
        LocalDate parsedDate = parseDate(date);
        if (parsedDate == null) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Invalid date for rebook.");
        }
        boolean free = roomAvailabilityRepository.isRoomFreeForSlots(semester.trim(), newRoomId, List.of(newSlotId), parsedDate);
        if (!free) {
            return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Selected room is not free.");
        }
        classCancellationRepository.upsert(entryId, parsedDate, normalizeReason(reason), user.getUsername());
        roomReservationRepository.insert(
                newRoomId,
                newSlotId,
                semester.trim(),
                parsedDate,
                facultyId,
                "RESERVE",
                normalizeReason(reason),
                user.getUsername()
        );
        auditLogService.record(user.getUsername(), "TIMETABLE_REBOOK",
                "entryId=" + entryId + ", newRoomId=" + newRoomId + ", newSlotId=" + newSlotId + ", date=" + parsedDate);
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&message=" + encode("Class rebooked for " + parsedDate + ".");
    }

    @PostMapping("/timetable/generate")
    public String generate(
            HttpSession session,
            @RequestParam String semester,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer semesterNo,
            @RequestParam(required = false) String sectionIds,
            @RequestParam(defaultValue = "GREEDY") String algorithm
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (departmentId == null || departmentId <= 0 || semesterNo == null || semesterNo <= 0) {
            return "redirect:/timetable?message=" + encode("Invalid department or semester.");
        }
        List<Integer> ids = parseIds(sectionIds);
        TimetableService.GenerationResult result = timetableService.generate(semester, departmentId, semesterNo, ids, algorithm);
        String msg = "Generated entries: " + result.entries().size() + " | Conflicts: " + result.conflicts();
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&semesterNo=" + semesterNo + "&message=" + encode(msg);
    }

    @PostMapping("/timetable/detect")
    public String detect(
            HttpSession session,
            @RequestParam String semester,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer semesterNo
    ) {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (departmentId == null || departmentId <= 0 || semesterNo == null || semesterNo <= 0) {
            return "redirect:/timetable?message=" + encode("Invalid department or semester.");
        }
        int conflicts = timetableService.detectConflicts(semester, departmentId);
        String msg = "Detected conflicts: " + conflicts;
        return "redirect:/timetable?semester=" + semester + "&departmentId=" + departmentId + "&semesterNo=" + semesterNo + "&message=" + encode(msg);
    }

    @GetMapping("/timetable/export")
    public void exportCsv(
            HttpSession session,
            @RequestParam String semester,
            @RequestParam(required = false) Integer departmentId,
            HttpServletResponse response
    ) throws Exception {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null) {
            response.sendError(401);
            return;
        }
        if (departmentId == null || departmentId <= 0) {
            response.sendError(400);
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"timetable_" + semester + "_dept" + departmentId + ".csv\"");
        var rows = timetableRepository.findBySemesterAndDepartment(semester, departmentId);
        StringBuilder sb = new StringBuilder();
        sb.append("semester,department,section,day,period,subject,faculty,room\n");
        for (var r : rows) {
            sb.append(esc(r.getSemester())).append(',')
                    .append(esc(r.getDepartmentName())).append(',')
                    .append(esc(r.getSectionName())).append(',')
                    .append(esc(r.getDay())).append(',')
                    .append(r.getPeriod()).append(',')
                    .append(esc(r.getSubjectName())).append(',')
                    .append(esc(r.getFacultyName())).append(',')
                    .append(esc(r.getRoomCode())).append('\n');
        }
        response.getWriter().write(sb.toString());
    }

    private String esc(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<Integer> parseIds(String raw) {
        List<Integer> ids = new ArrayList<>();
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

    private String encode(String text) {
        return text.replace(" ", "%20").replace("|", "%7C");
    }

    private Integer parseOptionalInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PreviewTable buildPreview(List<TimetableRowView> rows, Integer sectionId) {
        if (rows == null || rows.isEmpty() || sectionId == null) {
            return new PreviewTable(List.of(), List.of(), List.of());
        }
        List<TimetableRowView> filtered = rows.stream()
                .filter(r -> r.getSectionId() == sectionId)
                .toList();
        if (filtered.isEmpty()) {
            return new PreviewTable(List.of(), List.of(), List.of());
        }
        TreeSet<Integer> periods = new TreeSet<>();
        for (TimetableRowView row : filtered) {
            periods.add(row.getPeriod());
        }
        List<Integer> periodList = new ArrayList<>(periods);

        Map<String, Map<Integer, PreviewCell>> cellMap = new LinkedHashMap<>();
        for (TimetableRowView row : filtered) {
            cellMap.computeIfAbsent(row.getDay(), ignored -> new LinkedHashMap<>());
            Map<Integer, PreviewCell> rowMap = cellMap.get(row.getDay());
            PreviewCell cell = new PreviewCell(
                    row.getSubjectName(),
                    row.getRoomCode(),
                    row.getFacultyName(),
                    row.isFacultyConflict() || row.isRoomConflict() || row.isSectionConflict()
            );
            rowMap.put(row.getPeriod(), cell);
        }

        List<String> orderedDays = new ArrayList<>(cellMap.keySet());
        orderedDays.sort(Comparator.comparingInt(this::dayOrderIndex).thenComparing(String::compareToIgnoreCase));

        List<PreviewRow> previewRows = new ArrayList<>();
        for (String day : orderedDays) {
            List<PreviewCell> cells = new ArrayList<>();
            Map<Integer, PreviewCell> rowMap = cellMap.getOrDefault(day, Collections.emptyMap());
            for (Integer period : periodList) {
                cells.add(rowMap.getOrDefault(period, PreviewCell.empty()));
            }
            previewRows.add(new PreviewRow(day, cells));
        }
        List<LegendRow> legend = buildLegend(filtered);
        return new PreviewTable(periodList, previewRows, legend);
    }

    private List<LegendRow> buildLegend(List<TimetableRowView> rows) {
        Map<String, LegendRow> legendMap = new LinkedHashMap<>();
        for (TimetableRowView row : rows) {
            String key = row.getSubjectName() + "||" + row.getFacultyName() + "||" + row.getRoomCode();
            LegendRow existing = legendMap.get(key);
            if (existing == null) {
                legendMap.put(key, new LegendRow(row.getSubjectName(), row.getFacultyName(), row.getRoomCode(), 1));
            } else {
                existing.increment();
            }
        }
        return new ArrayList<>(legendMap.values());
    }

    private int dayOrderIndex(String day) {
        if (day == null) {
            return 99;
        }
        String key = day.trim().toLowerCase();
        return switch (key) {
            case "mon", "monday" -> 1;
            case "tue", "tues", "tuesday" -> 2;
            case "wed", "wednesday" -> 3;
            case "thu", "thur", "thurs", "thursday" -> 4;
            case "fri", "friday" -> 5;
            case "sat", "saturday" -> 6;
            case "sun", "sunday" -> 7;
            default -> 99;
        };
    }

    private static final class PreviewTable {
        private final List<Integer> periods;
        private final List<PreviewRow> rows;
        private final List<LegendRow> legend;

        private PreviewTable(List<Integer> periods, List<PreviewRow> rows, List<LegendRow> legend) {
            this.periods = periods;
            this.rows = rows;
            this.legend = legend;
        }
    }

    public static final class PreviewRow {
        private final String day;
        private final List<PreviewCell> cells;

        public PreviewRow(String day, List<PreviewCell> cells) {
            this.day = day;
            this.cells = cells;
        }

        public String getDay() {
            return day;
        }

        public List<PreviewCell> getCells() {
            return cells;
        }
    }

    public static final class PreviewCell {
        private final String subject;
        private final String room;
        private final String faculty;
        private final boolean conflict;

        private PreviewCell(String subject, String room, String faculty, boolean conflict) {
            this.subject = subject;
            this.room = room;
            this.faculty = faculty;
            this.conflict = conflict;
        }

        public static PreviewCell empty() {
            return new PreviewCell("", "", "", false);
        }

        public String getSubject() {
            return subject;
        }

        public String getRoom() {
            return room;
        }

        public String getFaculty() {
            return faculty;
        }

        public boolean isConflict() {
            return conflict;
        }
    }

    public static final class LegendRow {
        private final String subject;
        private final String faculty;
        private final String room;
        private int periodsPerWeek;

        private LegendRow(String subject, String faculty, String room, int periodsPerWeek) {
            this.subject = subject;
            this.faculty = faculty;
            this.room = room;
            this.periodsPerWeek = periodsPerWeek;
        }

        private void increment() {
            periodsPerWeek += 1;
        }

        public String getSubject() {
            return subject;
        }

        public String getFaculty() {
            return faculty;
        }

        public String getRoom() {
            return room;
        }

        public int getPeriodsPerWeek() {
            return periodsPerWeek;
        }
    }
}
