package com.erp.web.scheduler;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConflictChecker {
    public boolean canAssign(List<TimetableEntryRecord> current, TimetableEntryRecord candidate) {
        for (TimetableEntryRecord existing : current) {
            if (existing.slotId != candidate.slotId) {
                continue;
            }
            if (existing.facultyId == candidate.facultyId) {
                return false;
            }
            if (existing.roomId == candidate.roomId) {
                return false;
            }
            if (existing.sectionId == candidate.sectionId) {
                return false;
            }
        }
        return true;
    }

    public int countConflicts(List<TimetableEntryRecord> entries) {
        int conflicts = 0;
        for (int i = 0; i < entries.size(); i++) {
            TimetableEntryRecord a = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                TimetableEntryRecord b = entries.get(j);
                if (a.slotId != b.slotId) {
                    continue;
                }
                if (a.facultyId == b.facultyId) {
                    conflicts++;
                }
                if (a.roomId == b.roomId) {
                    conflicts++;
                }
                if (a.sectionId == b.sectionId) {
                    conflicts++;
                }
            }
        }
        return conflicts;
    }

    public boolean isFacultyAvailable(
            int facultyId,
            int slotId,
            Map<Integer, Set<Integer>> availabilityByFaculty
    ) {
        Set<Integer> slots = availabilityByFaculty.get(facultyId);
        if (slots == null || slots.isEmpty()) {
            return true;
        }
        return slots.contains(slotId);
    }

    public boolean isFacultyOverloaded(
            int facultyId,
            Map<Integer, Integer> facultyLoad,
            Map<Integer, FacultyRecord> facultyById
    ) {
        FacultyRecord faculty = facultyById.get(facultyId);
        if (faculty == null) {
            return true;
        }
        int currentLoad = facultyLoad.getOrDefault(facultyId, 0);
        return currentLoad >= faculty.maxHours;
    }

    public boolean isRoomSuitable(int roomCapacity, int sectionStrength) {
        return roomCapacity >= sectionStrength;
    }
}
