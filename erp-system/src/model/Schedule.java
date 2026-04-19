package model;

import java.util.*;

public class Schedule {

    private Map<TimeSlot, Assignment> assignments = new HashMap<>();

    public void assign(TimeSlot slot, Assignment assignment) {
        assignments.put(slot, assignment);
    }

    public Map<TimeSlot, Assignment> getAssignments() {
        return assignments;
    }
}
