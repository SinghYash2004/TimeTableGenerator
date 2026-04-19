package scheduling;

import model.Classroom;
import model.Faculty;
import model.Section;
import model.Subject;
import model.TimeSlot;
import model.TimetableEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GreedyScheduler {

    private final ConflictChecker conflictChecker = new ConflictChecker();

    public List<TimetableEntry> generate(
            int departmentId,
            String semester,
            List<Faculty> faculties,
            List<Subject> subjects,
            List<Section> sections,
            List<Classroom> classrooms,
            List<TimeSlot> slots,
            Map<Integer, Set<Integer>> availabilityByFaculty,
            double budgetLimit
    ) {
        List<TimetableEntry> entries = new ArrayList<>();
        Map<Integer, Integer> facultyLoad = new HashMap<>();
        Map<Integer, Faculty> facultyById = new HashMap<>();
        for (Faculty faculty : faculties) {
            facultyById.put(faculty.getFacultyId(), faculty);
        }
        Map<Integer, Classroom> roomById = new HashMap<>();
        for (Classroom classroom : classrooms) {
            roomById.put(classroom.getRoomId(), classroom);
        }
        double currentCost = 0.0;

        for (Section section : sections) {
            for (Subject subject : subjects) {
                int remaining = subject.getWeeklyHours();
                while (remaining > 0) {
                    TimetableEntry assigned = assignOneHour(
                            departmentId, section, semester, subject, faculties, classrooms, slots,
                            entries, facultyLoad, facultyById, roomById, availabilityByFaculty, budgetLimit, currentCost
                    );
                    if (assigned == null) {
                        break;
                    }
                    entries.add(assigned);
                    facultyLoad.put(assigned.getFacultyId(), facultyLoad.getOrDefault(assigned.getFacultyId(), 0) + 1);
                    currentCost += estimateEntryCost(assigned, facultyById, roomById);
                    remaining--;
                }
            }
        }

        return entries;
    }

    private TimetableEntry assignOneHour(
            int departmentId,
            Section section,
            String semester,
            Subject subject,
            List<Faculty> faculties,
            List<Classroom> classrooms,
            List<TimeSlot> slots,
            List<TimetableEntry> currentEntries,
            Map<Integer, Integer> facultyLoad,
            Map<Integer, Faculty> facultyById,
            Map<Integer, Classroom> roomById,
            Map<Integer, Set<Integer>> availabilityByFaculty,
            double budgetLimit,
            double currentCost
    ) {
        for (TimeSlot slot : slots) {
            for (Classroom classroom : classrooms) {
                if (!conflictChecker.isRoomSuitable(classroom.getCapacity(), section.getStrength())) {
                    continue;
                }
                for (Faculty faculty : faculties) {
                    if (faculty.getDepartmentId() != departmentId) {
                        continue;
                    }
                    if (conflictChecker.isFacultyOverloaded(faculty.getFacultyId(), facultyLoad, facultyById)) {
                        continue;
                    }
                    if (!conflictChecker.isFacultyAvailable(faculty.getFacultyId(), slot.getSlotId(), availabilityByFaculty)) {
                        continue;
                    }

                    TimetableEntry candidate = new TimetableEntry(
                            0, semester, faculty.getFacultyId(), subject.getSubjectId(),
                            classroom.getRoomId(), slot.getSlotId(), departmentId, section.getSectionId()
                    );
                    if (!isWithinBudget(candidate, facultyById, roomById, budgetLimit, currentCost)) {
                        continue;
                    }
                    if (conflictChecker.canAssign(currentEntries, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private double estimateEntryCost(
            TimetableEntry entry,
            Map<Integer, Faculty> facultyById,
            Map<Integer, Classroom> roomById
    ) {
        double cost = 0.0;
        Faculty faculty = facultyById.get(entry.getFacultyId());
        if (faculty != null) {
            cost += faculty.getCostPerHour();
        }
        Classroom classroom = roomById.get(entry.getRoomId());
        if (classroom != null) {
            cost += classroom.getCostPerHour();
        }
        return cost;
    }

    private boolean isWithinBudget(
            TimetableEntry entry,
            Map<Integer, Faculty> facultyById,
            Map<Integer, Classroom> roomById,
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
