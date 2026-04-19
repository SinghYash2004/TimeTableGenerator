package reporting;

import model.Classroom;
import model.Faculty;
import model.Section;
import model.Subject;
import model.TimeSlot;
import model.TimetableEntry;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    public void printTimetable(
            List<TimetableEntry> entries,
            List<Faculty> faculties,
            List<Subject> subjects,
            List<Section> sections,
            List<Classroom> classrooms,
            List<TimeSlot> slots
    ) {
        Map<Integer, Faculty> facultyById = new HashMap<>();
        for (Faculty faculty : faculties) {
            facultyById.put(faculty.getFacultyId(), faculty);
        }

        Map<Integer, Subject> subjectById = new HashMap<>();
        for (Subject subject : subjects) {
            subjectById.put(subject.getSubjectId(), subject);
        }

        Map<Integer, Classroom> classroomById = new HashMap<>();
        for (Classroom room : classrooms) {
            classroomById.put(room.getRoomId(), room);
        }

        Map<Integer, Section> sectionById = new HashMap<>();
        for (Section section : sections) {
            sectionById.put(section.getSectionId(), section);
        }

        Map<Integer, TimeSlot> slotById = new HashMap<>();
        for (TimeSlot slot : slots) {
            slotById.put(slot.getSlotId(), slot);
        }

        System.out.println("----- TIMETABLE -----");
        for (TimetableEntry entry : entries) {
            Faculty faculty = facultyById.get(entry.getFacultyId());
            Subject subject = subjectById.get(entry.getSubjectId());
            Classroom room = classroomById.get(entry.getRoomId());
            Section section = sectionById.get(entry.getSectionId());
            TimeSlot slot = slotById.get(entry.getSlotId());

            System.out.println(
                    (slot != null ? slot : ("slot#" + entry.getSlotId())) + " | " +
                            (section != null ? section.getSectionName() : ("section#" + entry.getSectionId())) + " | " +
                            (subject != null ? subject.getName() : ("subject#" + entry.getSubjectId())) + " | " +
                            (faculty != null ? faculty.getName() : ("faculty#" + entry.getFacultyId())) + " | " +
                            (room != null ? room.getRoomCode() : ("room#" + entry.getRoomId()))
            );
        }
    }

    public void printAlgorithmPerformance(String name, long runtimeMs, int generatedEntries, int conflicts, int requiredHours) {
        System.out.println("[" + name + "] runtime=" + runtimeMs + "ms, entries=" + generatedEntries + ", requiredHours=" + requiredHours + ", conflicts=" + conflicts);
    }

    public void printBudgetSummary(double totalCost, double budgetLimit, boolean withinBudget) {
        System.out.println("Budget: used=" + totalCost + ", limit=" + budgetLimit + ", withinBudget=" + withinBudget);
    }

    public void printRiskSummary(double overloadPercent, double underutilizationPercent) {
        System.out.println("Risk summary: overloadRisk=" + overloadPercent + "%, roomUnderutilizationRisk=" + underutilizationPercent + "%");
    }

    public String exportTimetableCsv(
            List<TimetableEntry> entries,
            List<Faculty> faculties,
            List<Subject> subjects,
            List<Section> sections,
            List<Classroom> classrooms,
            List<TimeSlot> slots,
            String semester,
            int departmentId
    ) throws Exception {
        Map<Integer, Faculty> facultyById = new HashMap<>();
        for (Faculty faculty : faculties) {
            facultyById.put(faculty.getFacultyId(), faculty);
        }

        Map<Integer, Subject> subjectById = new HashMap<>();
        for (Subject subject : subjects) {
            subjectById.put(subject.getSubjectId(), subject);
        }

        Map<Integer, Classroom> classroomById = new HashMap<>();
        for (Classroom room : classrooms) {
            classroomById.put(room.getRoomId(), room);
        }

        Map<Integer, Section> sectionById = new HashMap<>();
        for (Section section : sections) {
            sectionById.put(section.getSectionId(), section);
        }

        Map<Integer, TimeSlot> slotById = new HashMap<>();
        for (TimeSlot slot : slots) {
            slotById.put(slot.getSlotId(), slot);
        }

        File reportDir = new File("erp-system/reports");
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        String filePath = "erp-system/reports/timetable_" + semester + "_dept" + departmentId + ".csv";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("semester,department_id,section,slot,subject,faculty,room\n");
            for (TimetableEntry entry : entries) {
                Faculty faculty = facultyById.get(entry.getFacultyId());
                Subject subject = subjectById.get(entry.getSubjectId());
                Classroom room = classroomById.get(entry.getRoomId());
                Section section = sectionById.get(entry.getSectionId());
                TimeSlot slot = slotById.get(entry.getSlotId());

                writer.write(safe(entry.getSemester()) + ",");
                writer.write(entry.getDepartmentId() + ",");
                writer.write(safe(section != null ? section.getSectionName() : ("section#" + entry.getSectionId())) + ",");
                writer.write(safe(slot != null ? slot.toString() : ("slot#" + entry.getSlotId())) + ",");
                writer.write(safe(subject != null ? subject.getName() : ("subject#" + entry.getSubjectId())) + ",");
                writer.write(safe(faculty != null ? faculty.getName() : ("faculty#" + entry.getFacultyId())) + ",");
                writer.write(safe(room != null ? room.getRoomCode() : ("room#" + entry.getRoomId())));
                writer.write("\n");
            }
        }
        return filePath;
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replace("\"", "\"\"");
        return "\"" + cleaned + "\"";
    }
}
