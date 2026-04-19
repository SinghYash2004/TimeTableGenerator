package ai;

import model.Faculty;
import model.TimetableEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiskAnalyzer {

    public Map<Integer, String> facultyLoadRisk(List<TimetableEntry> entries, List<Faculty> faculties) {
        Map<Integer, Integer> loadByFaculty = new HashMap<>();
        for (TimetableEntry entry : entries) {
            loadByFaculty.put(entry.getFacultyId(), loadByFaculty.getOrDefault(entry.getFacultyId(), 0) + 1);
        }

        Map<Integer, String> risks = new HashMap<>();
        for (Faculty faculty : faculties) {
            int load = loadByFaculty.getOrDefault(faculty.getFacultyId(), 0);
            double ratio = faculty.getMaxHoursPerWeek() == 0 ? 1.0 : (double) load / faculty.getMaxHoursPerWeek();
            if (ratio > 1.0) {
                risks.put(faculty.getFacultyId(), "Overload");
            } else if (ratio < 0.5) {
                risks.put(faculty.getFacultyId(), "Underutilized");
            } else {
                risks.put(faculty.getFacultyId(), "Normal");
            }
        }
        return risks;
    }

    public double roomUnderutilizationRisk(List<TimetableEntry> entries, int totalSlots, int totalRooms) {
        if (totalSlots <= 0 || totalRooms <= 0) {
            return 0.0;
        }
        int usedRoomSlots = entries.size();
        int maxRoomSlots = totalSlots * totalRooms;
        double utilization = (double) usedRoomSlots / maxRoomSlots;
        return Math.max(0.0, 1.0 - utilization);
    }
}
