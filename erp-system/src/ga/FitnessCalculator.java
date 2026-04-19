package ga;

import model.*;

import java.util.*;

public class FitnessCalculator {

    public static int calculate(Schedule schedule) {
        int conflicts = 0;

        Map<String, Set<String>> teacherTimeMap = new HashMap<>();
        Map<String, Set<String>> roomTimeMap = new HashMap<>();

        for (Map.Entry<TimeSlot, Assignment> entry : schedule.getAssignments().entrySet()) {

            TimeSlot slot = entry.getKey();
            Assignment assignment = entry.getValue();

            String timeKey = slot.toString();

            // Teacher conflict
            teacherTimeMap.putIfAbsent(assignment.getTeacher().getName(), new HashSet<>());
            if (!teacherTimeMap.get(assignment.getTeacher().getName()).add(timeKey)) {
                conflicts++;
            }

            // Room conflict
            roomTimeMap.putIfAbsent(assignment.getRoom().getRoomNumber(), new HashSet<>());
            if (!roomTimeMap.get(assignment.getRoom().getRoomNumber()).add(timeKey)) {
                conflicts++;
            }
        }

        return conflicts; // lower is better
    }
}
