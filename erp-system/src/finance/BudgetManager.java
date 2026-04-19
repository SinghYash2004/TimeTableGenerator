package finance;

import model.Classroom;
import model.Faculty;
import model.TimetableEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetManager {

    public double calculateTotalCost(
            List<TimetableEntry> entries,
            List<Faculty> faculties,
            List<Classroom> classrooms
    ) {
        Map<Integer, Faculty> facultyById = new HashMap<>();
        for (Faculty faculty : faculties) {
            facultyById.put(faculty.getFacultyId(), faculty);
        }

        Map<Integer, Classroom> roomById = new HashMap<>();
        for (Classroom room : classrooms) {
            roomById.put(room.getRoomId(), room);
        }

        double total = 0.0;
        for (TimetableEntry entry : entries) {
            Faculty faculty = facultyById.get(entry.getFacultyId());
            Classroom room = roomById.get(entry.getRoomId());
            if (faculty != null) {
                total += faculty.getCostPerHour();
            }
            if (room != null) {
                total += room.getCostPerHour();
            }
        }
        return total;
    }

    public boolean isWithinBudget(double totalCost, double budgetLimit) {
        return totalCost <= budgetLimit;
    }
}
