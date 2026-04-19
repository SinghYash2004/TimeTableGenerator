package com.erp.web.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphColoringScheduler {
    private final ConflictChecker conflictChecker = new ConflictChecker();

    public List<TimetableEntryRecord> generate(
            int departmentId,
            String semester,
            List<FacultyRecord> faculties,
            List<SubjectRecord> subjects,
            List<SectionRecord> sections,
            List<ClassroomRecord> rooms,
            List<TimeSlotRecord> slots
    ) {
        return generate(
                departmentId, semester, faculties, subjects, sections, rooms, slots, Map.of(), 0.0
        );
    }

    public List<TimetableEntryRecord> generate(
            int departmentId,
            String semester,
            List<FacultyRecord> faculties,
            List<SubjectRecord> subjects,
            List<SectionRecord> sections,
            List<ClassroomRecord> rooms,
            List<TimeSlotRecord> slots,
            Map<Integer, Set<Integer>> availabilityByFaculty,
            double budgetLimit
    ) {
        List<SubjectRecord> ordered = new ArrayList<>(subjects);
        ordered.sort(Comparator.comparingInt((SubjectRecord s) -> s.weeklyHours).reversed());
        List<TimetableEntryRecord> entries = new ArrayList<>();

        Map<Integer, Integer> facultyLoad = new HashMap<>();
        Map<Integer, FacultyRecord> facultyById = new HashMap<>();
        for (FacultyRecord faculty : faculties) {
            facultyById.put(faculty.id, faculty);
        }
        Map<Integer, ClassroomRecord> roomById = new HashMap<>();
        for (ClassroomRecord room : rooms) {
            roomById.put(room.id, room);
        }
        double currentCost = 0.0;
        int slotCursor = 0;

        for (SectionRecord section : sections) {
            for (SubjectRecord subject : ordered) {
                int remaining = subject.weeklyHours;
                while (remaining > 0) {
                    TimetableEntryRecord assigned = assignOne(
                            departmentId, section.id, semester, subject, faculties, rooms, slots, slotCursor,
                            entries, facultyLoad, facultyById, roomById, availabilityByFaculty, budgetLimit, currentCost, section.strength
                    );
                    if (assigned == null) {
                        break;
                    }
                    entries.add(assigned);
                    facultyLoad.put(assigned.facultyId, facultyLoad.getOrDefault(assigned.facultyId, 0) + 1);
                    currentCost += estimateEntryCost(assigned, facultyById, roomById);
                    slotCursor = (slotCursor + 1) % Math.max(1, slots.size());
                    remaining--;
                }
            }
        }
        return entries;
    }

    private TimetableEntryRecord assignOne(
            int departmentId,
            int sectionId,
            String semester,
            SubjectRecord subject,
            List<FacultyRecord> faculties,
            List<ClassroomRecord> rooms,
            List<TimeSlotRecord> slots,
            int slotStart,
            List<TimetableEntryRecord> entries,
            Map<Integer, Integer> facultyLoad,
            Map<Integer, FacultyRecord> facultyById,
            Map<Integer, ClassroomRecord> roomById,
            Map<Integer, Set<Integer>> availabilityByFaculty,
            double budgetLimit,
            double currentCost,
            int sectionStrength
    ) {
        if (slots.isEmpty()) {
            return null;
        }
        List<FacultyRecord> sortedFaculty = new ArrayList<>(faculties);
        sortedFaculty.sort(Comparator.comparingInt(f -> facultyLoad.getOrDefault(f.id, 0)));

        for (int i = 0; i < slots.size(); i++) {
            TimeSlotRecord slot = slots.get((slotStart + i) % slots.size());
            for (ClassroomRecord room : rooms) {
                if (!conflictChecker.isRoomSuitable(room.capacity, sectionStrength)) {
                    continue;
                }
                for (FacultyRecord faculty : sortedFaculty) {
                    if (faculty.departmentId != departmentId) {
                        continue;
                    }
                    if (conflictChecker.isFacultyOverloaded(faculty.id, facultyLoad, facultyById)) {
                        continue;
                    }
                    if (!conflictChecker.isFacultyAvailable(faculty.id, slot.id, availabilityByFaculty)) {
                        continue;
                    }
                    TimetableEntryRecord candidate = new TimetableEntryRecord(
                            semester, faculty.id, subject.id, room.id, slot.id, departmentId, sectionId
                    );
                    if (!isWithinBudget(candidate, facultyById, roomById, budgetLimit, currentCost)) {
                        continue;
                    }
                    if (conflictChecker.canAssign(entries, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private double estimateEntryCost(
            TimetableEntryRecord entry,
            Map<Integer, FacultyRecord> facultyById,
            Map<Integer, ClassroomRecord> roomById
    ) {
        double cost = 0.0;
        FacultyRecord faculty = facultyById.get(entry.facultyId);
        if (faculty != null) {
            cost += faculty.costPerHour;
        }
        ClassroomRecord room = roomById.get(entry.roomId);
        if (room != null) {
            cost += room.costPerHour;
        }
        return cost;
    }

    private boolean isWithinBudget(
            TimetableEntryRecord entry,
            Map<Integer, FacultyRecord> facultyById,
            Map<Integer, ClassroomRecord> roomById,
            double budgetLimit,
            double currentCost
    ) {
        if (budgetLimit <= 0) {
            return true;
        }
        double cost = estimateEntryCost(entry, facultyById, roomById);
        return currentCost + cost <= budgetLimit;
    }
}
