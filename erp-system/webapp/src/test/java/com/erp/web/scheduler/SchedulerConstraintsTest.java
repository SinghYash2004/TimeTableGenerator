package com.erp.web.scheduler;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerConstraintsTest {

    private static final int DEPARTMENT_ID = 10;

    private final GreedyScheduler greedyScheduler = new GreedyScheduler();
    private final GraphColoringScheduler graphScheduler = new GraphColoringScheduler();
    private final ConflictChecker conflictChecker = new ConflictChecker();

    @Test
    void bothSchedulersPreventCollisionsAndRespectMaxLoad() {
        List<FacultyRecord> faculties = List.of(
                new FacultyRecord(1, DEPARTMENT_ID, 2, 100),
                new FacultyRecord(2, DEPARTMENT_ID, 2, 120)
        );
        List<SubjectRecord> subjects = List.of(
                new SubjectRecord(1001, 3)
        );
        List<SectionRecord> sections = List.of(
                new SectionRecord(501, 20)
        );
        List<ClassroomRecord> rooms = List.of(
                new ClassroomRecord(301, 20, 0.0),
                new ClassroomRecord(302, 20, 0.0)
        );
        List<TimeSlotRecord> slots = List.of(
                new TimeSlotRecord(1),
                new TimeSlotRecord(2),
                new TimeSlotRecord(3)
        );
        Map<Integer, Set<Integer>> availability = Map.of();

        for (List<TimetableEntryRecord> entries : runForBoth(faculties, subjects, sections, rooms, slots, availability)) {
            assertEquals(0, conflictChecker.countConflicts(entries));
            assertTrue(entries.size() <= 3);
            assertFacultyLoadWithinLimit(entries, faculties);
        }
    }

    @Test
    void bothSchedulersRespectFacultyAvailability() {
        List<FacultyRecord> faculties = List.of(
                new FacultyRecord(1, DEPARTMENT_ID, 3, 100)
        );
        List<SubjectRecord> subjects = List.of(
                new SubjectRecord(2001, 1)
        );
        List<SectionRecord> sections = List.of(
                new SectionRecord(601, 15)
        );
        List<ClassroomRecord> rooms = List.of(
                new ClassroomRecord(401, 15, 0.0)
        );
        List<TimeSlotRecord> slots = List.of(
                new TimeSlotRecord(1),
                new TimeSlotRecord(2)
        );
        Map<Integer, Set<Integer>> availability = Map.of(1, Set.of(2));

        for (List<TimetableEntryRecord> entries : runForBoth(faculties, subjects, sections, rooms, slots, availability)) {
            assertEquals(1, entries.size());
            assertEquals(2, entries.get(0).slotId);
            assertEquals(0, conflictChecker.countConflicts(entries));
        }
    }

    @Test
    void bothSchedulersAvoidCrossDepartmentFacultyAssignments() {
        List<FacultyRecord> faculties = List.of(
                new FacultyRecord(1, DEPARTMENT_ID, 3, 100),
                new FacultyRecord(2, DEPARTMENT_ID + 1, 3, 100)
        );
        List<SubjectRecord> subjects = List.of(new SubjectRecord(3001, 1));
        List<SectionRecord> sections = List.of(new SectionRecord(701, 20));
        List<ClassroomRecord> rooms = List.of(new ClassroomRecord(501, 20, 0.0));
        List<TimeSlotRecord> slots = List.of(new TimeSlotRecord(1));

        for (List<TimetableEntryRecord> entries : runForBoth(faculties, subjects, sections, rooms, slots, Map.of())) {
            assertEquals(1, entries.size());
            assertEquals(1, entries.get(0).facultyId);
        }
    }

    @Test
    void bothSchedulersReturnEmptyWhenNoSlotsAvailable() {
        List<FacultyRecord> faculties = List.of(new FacultyRecord(1, DEPARTMENT_ID, 5, 100));
        List<SubjectRecord> subjects = List.of(new SubjectRecord(4001, 2));
        List<SectionRecord> sections = List.of(new SectionRecord(801, 20));
        List<ClassroomRecord> rooms = List.of(new ClassroomRecord(601, 20, 0.0));

        for (List<TimetableEntryRecord> entries : runForBoth(faculties, subjects, sections, rooms, List.of(), Map.of())) {
            assertTrue(entries.isEmpty());
        }
    }

    @Test
    void bothSchedulersProducePartialScheduleWhenAvailabilityIsOverConstrained() {
        List<FacultyRecord> faculties = List.of(new FacultyRecord(1, DEPARTMENT_ID, 10, 100));
        List<SubjectRecord> subjects = List.of(new SubjectRecord(5001, 3));
        List<SectionRecord> sections = List.of(new SectionRecord(901, 20));
        List<ClassroomRecord> rooms = List.of(new ClassroomRecord(701, 20, 0.0));
        List<TimeSlotRecord> slots = List.of(new TimeSlotRecord(1), new TimeSlotRecord(2), new TimeSlotRecord(3));
        Map<Integer, Set<Integer>> availability = Map.of(1, Set.of(1));

        for (List<TimetableEntryRecord> entries : runForBoth(faculties, subjects, sections, rooms, slots, availability)) {
            // Only one hour can be placed because section/faculty/room cannot repeat in the same slot.
            assertEquals(1, entries.size());
            assertEquals(1, entries.get(0).slotId);
        }
    }

    @Test
    void bothSchedulersEnforceUniqueFacultyRoomAndSectionPerSlot() {
        List<FacultyRecord> faculties = List.of(
                new FacultyRecord(1, DEPARTMENT_ID, 4, 100),
                new FacultyRecord(2, DEPARTMENT_ID, 4, 120)
        );
        List<SubjectRecord> subjects = List.of(
                new SubjectRecord(6001, 2),
                new SubjectRecord(6002, 2)
        );
        List<SectionRecord> sections = List.of(
                new SectionRecord(1001, 25),
                new SectionRecord(1002, 25)
        );
        List<ClassroomRecord> rooms = List.of(
                new ClassroomRecord(801, 25, 0.0),
                new ClassroomRecord(802, 25, 0.0)
        );
        List<TimeSlotRecord> slots = List.of(
                new TimeSlotRecord(1),
                new TimeSlotRecord(2),
                new TimeSlotRecord(3)
        );

        for (List<TimetableEntryRecord> entries : runForBoth(faculties, subjects, sections, rooms, slots, Map.of())) {
            assertEquals(0, conflictChecker.countConflicts(entries));
            assertUniqueBySlot(entries, "faculty");
            assertUniqueBySlot(entries, "room");
            assertUniqueBySlot(entries, "section");
            assertFacultyLoadWithinLimit(entries, faculties);
        }
    }

    private void assertFacultyLoadWithinLimit(List<TimetableEntryRecord> entries, List<FacultyRecord> faculties) {
        Map<Integer, Integer> assignedHours = new HashMap<>();
        for (TimetableEntryRecord entry : entries) {
            assignedHours.put(entry.facultyId, assignedHours.getOrDefault(entry.facultyId, 0) + 1);
        }
        for (FacultyRecord faculty : faculties) {
            assertTrue(assignedHours.getOrDefault(faculty.id, 0) <= faculty.maxHours);
        }
    }

    private void assertUniqueBySlot(List<TimetableEntryRecord> entries, String keyType) {
        Set<String> seen = new java.util.HashSet<>();
        for (TimetableEntryRecord entry : entries) {
            String key;
            if ("faculty".equals(keyType)) {
                key = entry.slotId + ":f:" + entry.facultyId;
            } else if ("room".equals(keyType)) {
                key = entry.slotId + ":r:" + entry.roomId;
            } else {
                key = entry.slotId + ":s:" + entry.sectionId;
            }
            assertFalse(seen.contains(key), "Duplicate assignment detected for " + keyType + " key " + key);
            seen.add(key);
        }
    }

    private List<List<TimetableEntryRecord>> runForBoth(
            List<FacultyRecord> faculties,
            List<SubjectRecord> subjects,
            List<SectionRecord> sections,
            List<ClassroomRecord> rooms,
            List<TimeSlotRecord> slots,
            Map<Integer, Set<Integer>> availability
    ) {
        return List.of(
                greedyScheduler.generate(DEPARTMENT_ID, "SEM-1", faculties, subjects, sections, rooms, slots, availability, 0.0),
                graphScheduler.generate(DEPARTMENT_ID, "SEM-1", faculties, subjects, sections, rooms, slots, availability, 0.0)
        );
    }
}
