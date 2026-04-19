package com.erp.web.timetable;

import com.erp.web.scheduler.ClassroomRecord;
import com.erp.web.scheduler.ConflictChecker;
import com.erp.web.scheduler.FacultyRecord;
import com.erp.web.scheduler.GraphColoringScheduler;
import com.erp.web.scheduler.GreedyScheduler;
import com.erp.web.scheduler.SectionRecord;
import com.erp.web.scheduler.SubjectRecord;
import com.erp.web.scheduler.TimeSlotRecord;
import com.erp.web.scheduler.TimetableEntryRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TimetableService {
    private final JdbcTemplate jdbcTemplate;
    private final GreedyScheduler greedyScheduler = new GreedyScheduler();
    private final GraphColoringScheduler graphScheduler = new GraphColoringScheduler();
    private final ConflictChecker conflictChecker = new ConflictChecker();

    public TimetableService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public GenerationResult generate(String semester, int departmentId, int semesterNo, List<Integer> sectionIds, String algorithm) {
        InputData input = loadInputData(departmentId, semesterNo, sectionIds);
        if (input.faculties.isEmpty() || input.subjects.isEmpty() || input.sections.isEmpty() || input.rooms.isEmpty() || input.slots.isEmpty()) {
            return new GenerationResult(List.of(), 0);
        }

        List<TimetableEntryRecord> entries = "GRAPH".equalsIgnoreCase(algorithm)
                ? graphScheduler.generate(departmentId, semester, input.faculties, input.subjects, input.sections, input.rooms, input.slots, input.availabilityByFaculty, input.budgetLimit)
                : greedyScheduler.generate(departmentId, semester, input.faculties, input.subjects, input.sections, input.rooms, input.slots, input.availabilityByFaculty, input.budgetLimit);

        jdbcTemplate.update("DELETE FROM timetable WHERE semester = ? AND department_id = ?", semester, departmentId);
        for (TimetableEntryRecord e : entries) {
            jdbcTemplate.update(
                    "INSERT INTO timetable(semester, faculty_id, subject_id, room_id, slot_id, department_id, section_id) VALUES(?, ?, ?, ?, ?, ?, ?)",
                    e.semester, e.facultyId, e.subjectId, e.roomId, e.slotId, e.departmentId, e.sectionId
            );
        }
        int conflicts = conflictChecker.countConflicts(entries);
        return new GenerationResult(entries, conflicts);
    }

    public GenerationResult preview(String semester, int departmentId, int semesterNo, List<Integer> sectionIds, String algorithm) {
        InputData input = loadInputData(departmentId, semesterNo, sectionIds);
        if (input.faculties.isEmpty() || input.subjects.isEmpty() || input.sections.isEmpty() || input.rooms.isEmpty() || input.slots.isEmpty()) {
            return new GenerationResult(List.of(), 0);
        }

        List<TimetableEntryRecord> entries = "GRAPH".equalsIgnoreCase(algorithm)
                ? graphScheduler.generate(departmentId, semester, input.faculties, input.subjects, input.sections, input.rooms, input.slots, input.availabilityByFaculty, input.budgetLimit)
                : greedyScheduler.generate(departmentId, semester, input.faculties, input.subjects, input.sections, input.rooms, input.slots, input.availabilityByFaculty, input.budgetLimit);

        int conflicts = conflictChecker.countConflicts(entries);
        return new GenerationResult(entries, conflicts);
    }

    public ComparisonResult compareAlgorithms(String semester, int departmentId, int semesterNo, List<Integer> sectionIds) {
        InputData input = loadInputData(departmentId, semesterNo, sectionIds);
        if (input.faculties.isEmpty() || input.subjects.isEmpty() || input.sections.isEmpty() || input.rooms.isEmpty() || input.slots.isEmpty()) {
            return new ComparisonResult(
                    new AlgorithmMetrics("GREEDY", 0, 0, 0),
                    new AlgorithmMetrics("GRAPH", 0, 0, 0)
            );
        }

        long greedyStart = System.nanoTime();
        List<TimetableEntryRecord> greedyEntries = greedyScheduler.generate(
                departmentId, semester, input.faculties, input.subjects, input.sections, input.rooms, input.slots, input.availabilityByFaculty, input.budgetLimit
        );
        long greedyMs = (System.nanoTime() - greedyStart) / 1_000_000;

        long graphStart = System.nanoTime();
        List<TimetableEntryRecord> graphEntries = graphScheduler.generate(
                departmentId, semester, input.faculties, input.subjects, input.sections, input.rooms, input.slots, input.availabilityByFaculty, input.budgetLimit
        );
        long graphMs = (System.nanoTime() - graphStart) / 1_000_000;

        AlgorithmMetrics greedy = new AlgorithmMetrics(
                "GREEDY",
                greedyEntries.size(),
                conflictChecker.countConflicts(greedyEntries),
                greedyMs
        );
        AlgorithmMetrics graph = new AlgorithmMetrics(
                "GRAPH",
                graphEntries.size(),
                conflictChecker.countConflicts(graphEntries),
                graphMs
        );
        return new ComparisonResult(greedy, graph);
    }

    public int detectConflicts(String semester, int departmentId) {
        List<TimetableEntryRecord> entries = jdbcTemplate.query(
                "SELECT semester, faculty_id, subject_id, room_id, slot_id, department_id, section_id FROM timetable WHERE semester = ? AND department_id = ?",
                (rs, i) -> new TimetableEntryRecord(
                        rs.getString("semester"),
                        rs.getInt("faculty_id"),
                        rs.getInt("subject_id"),
                        rs.getInt("room_id"),
                        rs.getInt("slot_id"),
                        rs.getInt("department_id"),
                        rs.getInt("section_id")
                ),
                semester, departmentId
        );
        return conflictChecker.countConflicts(entries);
    }

    public record GenerationResult(List<TimetableEntryRecord> entries, int conflicts) {
    }

    public record AlgorithmMetrics(String algorithm, int assignedEntries, int conflicts, long runtimeMs) {
    }

    public record ComparisonResult(AlgorithmMetrics greedy, AlgorithmMetrics graph) {
    }

    private Map<Integer, Set<Integer>> loadAvailabilityByFaculty(int departmentId) {
        Map<Integer, Set<Integer>> availabilityByFaculty = new HashMap<>();
        jdbcTemplate.query(
                """
                SELECT fa.faculty_id, fa.slot_id
                FROM faculty_availability fa
                JOIN faculty f ON f.faculty_id = fa.faculty_id
                WHERE f.department_id = ? AND fa.available = 1
                """,
                rs -> {
                    int facultyId = rs.getInt("faculty_id");
                    int slotId = rs.getInt("slot_id");
                    availabilityByFaculty.computeIfAbsent(facultyId, ignored -> new HashSet<>()).add(slotId);
                },
                departmentId
        );
        return availabilityByFaculty;
    }

    private InputData loadInputData(int departmentId, int semesterNo, List<Integer> sectionIds) {
        List<FacultyRecord> faculties = jdbcTemplate.query(
                "SELECT faculty_id, department_id, max_hours_per_week, cost_per_hour FROM faculty WHERE department_id = ?",
                (rs, i) -> new FacultyRecord(
                        rs.getInt("faculty_id"),
                        rs.getInt("department_id"),
                        rs.getInt("max_hours_per_week"),
                        rs.getDouble("cost_per_hour")
                ),
                departmentId
        );
        List<SubjectRecord> subjects = jdbcTemplate.query(
                "SELECT subject_id, weekly_hours FROM subject WHERE department_id = ?",
                (rs, i) -> new SubjectRecord(rs.getInt("subject_id"), rs.getInt("weekly_hours")),
                departmentId
        );
        List<SectionRecord> sections = jdbcTemplate.query(
                "SELECT section_id, strength FROM section WHERE department_id = ? AND semester_no = ?",
                (rs, i) -> new SectionRecord(rs.getInt("section_id"), rs.getInt("strength")),
                departmentId, semesterNo
        );
        if (sectionIds != null && !sectionIds.isEmpty()) {
            sections = sections.stream().filter(s -> sectionIds.contains(s.id)).toList();
        }
        List<ClassroomRecord> rooms = jdbcTemplate.query(
                "SELECT room_id, capacity, cost_per_hour FROM classroom",
                (rs, i) -> new ClassroomRecord(
                        rs.getInt("room_id"),
                        rs.getInt("capacity"),
                        rs.getDouble("cost_per_hour")
                )
        );
        List<TimeSlotRecord> slots = jdbcTemplate.query(
                "SELECT slot_id FROM timeslot ORDER BY slot_id",
                (rs, i) -> new TimeSlotRecord(rs.getInt("slot_id"))
        );
        Map<Integer, Set<Integer>> availabilityByFaculty = loadAvailabilityByFaculty(departmentId);
        Double budgetLimit = jdbcTemplate.queryForObject(
                "SELECT budget_limit FROM department WHERE department_id = ?",
                Double.class,
                departmentId
        );
        double budget = (budgetLimit == null) ? 0.0 : budgetLimit;
        return new InputData(faculties, subjects, sections, rooms, slots, availabilityByFaculty, budget);
    }

    private record InputData(
            List<FacultyRecord> faculties,
            List<SubjectRecord> subjects,
            List<SectionRecord> sections,
            List<ClassroomRecord> rooms,
            List<TimeSlotRecord> slots,
            Map<Integer, Set<Integer>> availabilityByFaculty,
            double budgetLimit
    ) {
    }
}
