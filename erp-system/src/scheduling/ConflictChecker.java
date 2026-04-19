package scheduling;

import model.Faculty;
import model.TimetableEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConflictChecker {

    public boolean canAssign(List<TimetableEntry> current, TimetableEntry candidate) {
        for (TimetableEntry existing : current) {
            if (existing.getSlotId() != candidate.getSlotId()) {
                continue;
            }
            if (existing.getFacultyId() == candidate.getFacultyId()) {
                return false;
            }
            if (existing.getRoomId() == candidate.getRoomId()) {
                return false;
            }
            if (existing.getSectionId() == candidate.getSectionId()) {
                return false;
            }
        }
        return true;
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

    public boolean isFacultyOverloaded(int facultyId, Map<Integer, Integer> facultyLoad, Map<Integer, Faculty> facultyById) {
        Faculty faculty = facultyById.get(facultyId);
        if (faculty == null) {
            return true;
        }
        int currentLoad = facultyLoad.getOrDefault(facultyId, 0);
        return currentLoad >= faculty.getMaxHoursPerWeek();
    }

    public boolean isRoomSuitable(int roomCapacity, int sectionStrength) {
        return roomCapacity >= sectionStrength;
    }

    public int countConflicts(List<TimetableEntry> entries) {
        int conflicts = 0;
        for (int i = 0; i < entries.size(); i++) {
            TimetableEntry a = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                TimetableEntry b = entries.get(j);
                if (a.getSlotId() != b.getSlotId()) {
                    continue;
                }
                if (a.getFacultyId() == b.getFacultyId()) {
                    conflicts++;
                }
                if (a.getRoomId() == b.getRoomId()) {
                    conflicts++;
                }
                if (a.getSectionId() == b.getSectionId()) {
                    conflicts++;
                }
            }
        }
        return conflicts;
    }
}
