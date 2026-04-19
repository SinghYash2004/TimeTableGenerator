import ai.RiskAnalyzer;
import config.DBConnection;
import dao.ClassroomDAO;
import dao.DepartmentDAO;
import dao.FacultyDAO;
import dao.SectionDAO;
import dao.SubjectDAO;
import dao.TimeSlotDAO;
import dao.TimetableDAO;
import finance.BudgetManager;
import model.Classroom;
import model.Department;
import model.Faculty;
import model.Section;
import model.Subject;
import model.TimeSlot;
import model.TimetableEntry;
import reporting.ReportGenerator;
import scheduling.ConflictChecker;
import scheduling.GraphColoringScheduler;
import scheduling.GreedyScheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_FACULTY = "FACULTY";

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final FacultyDAO FACULTY_DAO = new FacultyDAO();
    private static final SectionDAO SECTION_DAO = new SectionDAO();
    private static final SubjectDAO SUBJECT_DAO = new SubjectDAO();
    private static final ClassroomDAO CLASSROOM_DAO = new ClassroomDAO();
    private static final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();
    private static final TimeSlotDAO TIMESLOT_DAO = new TimeSlotDAO();
    private static final TimetableDAO TIMETABLE_DAO = new TimetableDAO();
    private static final GreedyScheduler GREEDY_SCHEDULER = new GreedyScheduler();
    private static final GraphColoringScheduler GRAPH_SCHEDULER = new GraphColoringScheduler();
    private static final ConflictChecker CONFLICT_CHECKER = new ConflictChecker();
    private static final RiskAnalyzer RISK_ANALYZER = new RiskAnalyzer();
    private static final BudgetManager BUDGET_MANAGER = new BudgetManager();
    private static final ReportGenerator REPORT_GENERATOR = new ReportGenerator();

    private static Session CURRENT_SESSION;

    public static void main(String[] args) {
        try {
            ensureSchemaCompatibility();
        } catch (Exception e) {
            System.out.println("Startup schema check failed: " + e.getMessage());
        }

        CURRENT_SESSION = login();
        if (CURRENT_SESSION == null) {
            System.out.println("Authentication failed. Exiting...");
            return;
        }

        while (true) {
            printMainMenu();
            String choice = SCANNER.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        requireAdmin();
                        seedSampleData();
                        break;
                    case "2":
                        requireAdmin();
                        generateAndStore("GREEDY");
                        break;
                    case "3":
                        requireAdmin();
                        generateAndStore("GRAPH");
                        break;
                    case "4":
                        requireAdmin();
                        compareAlgorithms();
                        break;
                    case "5":
                        showStoredTimetable();
                        break;
                    case "6":
                        requireAdmin();
                        manageMasterData();
                        break;
                    case "7":
                        exportStoredTimetableCsv();
                        break;
                    case "0":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Data constraint error: check references and duplicate values.");
            } catch (SecurityException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private static void ensureSchemaCompatibility() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            String createFacultyAvailability = "CREATE TABLE IF NOT EXISTS faculty_availability (" +
                    "faculty_id INT NOT NULL," +
                    "slot_id INT NOT NULL," +
                    "available TINYINT(1) NOT NULL DEFAULT 1," +
                    "PRIMARY KEY (faculty_id, slot_id)," +
                    "FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)," +
                    "FOREIGN KEY (slot_id) REFERENCES timeslot(slot_id)" +
                    ")";
            try (PreparedStatement ps = conn.prepareStatement(createFacultyAvailability)) {
                ps.executeUpdate();
            }

            ensureColumnExists(
                    conn,
                    "timetable",
                    "semester",
                    "ALTER TABLE timetable ADD COLUMN semester VARCHAR(20) NOT NULL DEFAULT '2026S1'"
            );
            ensureColumnExists(
                    conn,
                    "classroom",
                    "room_code",
                    "ALTER TABLE classroom ADD COLUMN room_code VARCHAR(30)"
            );
            ensureColumnExists(
                    conn,
                    "timetable",
                    "section_id",
                    "ALTER TABLE timetable ADD COLUMN section_id INT NULL"
            );

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE classroom SET room_code = CONCAT('R', room_id) WHERE room_code IS NULL OR room_code = ''"
            )) {
                ps.executeUpdate();
            }

            String createSection = "CREATE TABLE IF NOT EXISTS section (" +
                    "section_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "section_name VARCHAR(20) NOT NULL," +
                    "semester_no INT NOT NULL," +
                    "strength INT NOT NULL," +
                    "department_id INT NOT NULL," +
                    "UNIQUE KEY uk_section_name_sem_dept (section_name, semester_no, department_id)," +
                    "FOREIGN KEY (department_id) REFERENCES department(department_id)" +
                    ")";
            try (PreparedStatement ps = conn.prepareStatement(createSection)) {
                ps.executeUpdate();
            }

            String createSectionIndex = "CREATE INDEX idx_section_slot ON timetable(section_id, slot_id, semester)";
            try (PreparedStatement ps = conn.prepareStatement(createSectionIndex)) {
                ps.executeUpdate();
            } catch (Exception ignored) {
                // index may already exist
            }

            try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE timetable DROP INDEX uk_subject_slot_sem")) {
                ps.executeUpdate();
            } catch (Exception ignored) {
                // old index may not exist
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "ALTER TABLE timetable ADD UNIQUE KEY uk_subject_section_slot_sem (subject_id, section_id, slot_id, semester)"
            )) {
                ps.executeUpdate();
            } catch (Exception ignored) {
                // already exists
            }

            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(100) NOT NULL UNIQUE," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "role VARCHAR(20) NOT NULL," +
                    "faculty_id INT NULL," +
                    "FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)" +
                    ")";
            try (PreparedStatement ps = conn.prepareStatement(createUsers)) {
                ps.executeUpdate();
            }

            String seedAdmin = "INSERT IGNORE INTO users(username, password_hash, role, faculty_id) " +
                    "VALUES('admin', SHA2('admin123', 256), 'ADMIN', NULL)";
            try (PreparedStatement ps = conn.prepareStatement(seedAdmin)) {
                ps.executeUpdate();
            }
        }
    }

    private static void ensureColumnExists(Connection conn, String tableName, String columnName, String alterSql) throws Exception {
        String sql = "SELECT 1 FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(alterSql)) {
            ps.executeUpdate();
        }
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("===== Intelligent Academic ERP (CLI) ===== (" + CURRENT_SESSION.role + ")");
        if (isAdmin()) {
            System.out.println("1. Seed sample data");
            System.out.println("2. Generate timetable (Greedy)");
            System.out.println("3. Generate timetable (Graph Coloring)");
            System.out.println("4. Compare algorithms");
            System.out.println("6. Manage master data (CRUD)");
        }
        System.out.println("5. View stored timetable");
        System.out.println("7. Export timetable CSV");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    private static void seedSampleData() throws Exception {
        DEPARTMENT_DAO.save(new Department(0, "Computer Science", 30000.0));
        Department dept = DEPARTMENT_DAO.getByName("Computer Science");
        if (dept == null) {
            System.out.println("Failed to load department after seeding.");
            return;
        }
        int departmentId = dept.getDepartmentId();

        FACULTY_DAO.save(new Faculty(0, "A. Singh", departmentId, 10, 450));
        FACULTY_DAO.save(new Faculty(0, "M. Patel", departmentId, 10, 425));
        FACULTY_DAO.save(new Faculty(0, "R. Das", departmentId, 8, 400));

        SUBJECT_DAO.save(new Subject(0, "Data Structures", 4, departmentId));
        SUBJECT_DAO.save(new Subject(0, "Operating Systems", 3, departmentId));
        SUBJECT_DAO.save(new Subject(0, "DBMS", 3, departmentId));

        CLASSROOM_DAO.save(new Classroom(0, "R101", 60, 150));
        CLASSROOM_DAO.save(new Classroom(0, "R102", 60, 145));
        CLASSROOM_DAO.save(new Classroom(0, "LAB1", 40, 220));

        // Default 6 sections for semester 4, as commonly used in CSE examples.
        for (String s : new String[]{"A", "B", "C", "D", "E", "F"}) {
            SECTION_DAO.save(new Section(0, s, 4, 60, departmentId));
        }

        TIMESLOT_DAO.upsertDefaultWeekSlots();
        FACULTY_DAO.seedFullAvailability();
        seedFacultyUsers();
        System.out.println("Sample data seeded.");
    }

    private static void seedFacultyUsers() throws Exception {
        List<Faculty> facultyList = FACULTY_DAO.getAll();
        String sql = "INSERT IGNORE INTO users(username, password_hash, role, faculty_id) VALUES(?, SHA2(?, 256), 'FACULTY', ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Faculty faculty : facultyList) {
                String username = faculty.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
                if (username.isEmpty()) {
                    username = "faculty" + faculty.getFacultyId();
                }
                ps.setString(1, username);
                ps.setString(2, "faculty123");
                ps.setInt(3, faculty.getFacultyId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void generateAndStore(String algorithm) throws Exception {
        String semester = askSemester();
        int semesterNo = askSemesterNumber();
        int departmentId = askDepartmentId();
        if (departmentId <= 0) {
            System.out.println("Invalid department. Add a department first.");
            return;
        }

        List<Faculty> faculties = FACULTY_DAO.getByDepartment(departmentId);
        List<Subject> subjects = SUBJECT_DAO.getByDepartment(departmentId);
        List<Section> sections = askSectionsForGeneration(departmentId, semesterNo);
        List<Classroom> classrooms = CLASSROOM_DAO.getAll();
        List<TimeSlot> slots = TIMESLOT_DAO.getAll();
        Map<Integer, Set<Integer>> availability = FACULTY_DAO.getAvailabilityMap();

        if (faculties.isEmpty() || subjects.isEmpty() || sections.isEmpty() || classrooms.isEmpty() || slots.isEmpty()) {
            System.out.println("Insufficient master data. Seed or add data first.");
            return;
        }

        Department dept = DEPARTMENT_DAO.getById(departmentId);
        double budget = dept == null ? 0.0 : dept.getBudgetLimit();

        long start = System.currentTimeMillis();
        List<TimetableEntry> entries = "GRAPH".equals(algorithm)
                ? GRAPH_SCHEDULER.generate(departmentId, semester, faculties, subjects, sections, classrooms, slots, availability, budget)
                : GREEDY_SCHEDULER.generate(departmentId, semester, faculties, subjects, sections, classrooms, slots, availability, budget);
        long runtime = System.currentTimeMillis() - start;

        TIMETABLE_DAO.clearBySemesterAndDepartment(semester, departmentId);
        TIMETABLE_DAO.saveAll(entries);

        int requiredHours = subjects.stream().mapToInt(Subject::getWeeklyHours).sum() * sections.size();
        int conflicts = CONFLICT_CHECKER.countConflicts(entries);
        double totalCost = BUDGET_MANAGER.calculateTotalCost(entries, faculties, classrooms);
        boolean withinBudget = BUDGET_MANAGER.isWithinBudget(totalCost, budget);

        REPORT_GENERATOR.printAlgorithmPerformance(algorithm, runtime, entries.size(), conflicts, requiredHours);
        REPORT_GENERATOR.printBudgetSummary(totalCost, budget, withinBudget);
        REPORT_GENERATOR.printTimetable(entries, faculties, subjects, sections, classrooms, slots);
        printRiskReport(entries, faculties, slots, classrooms);
    }

    private static void compareAlgorithms() throws Exception {
        String semester = askSemester();
        int semesterNo = askSemesterNumber();
        int departmentId = askDepartmentId();
        if (departmentId <= 0) {
            System.out.println("Invalid department.");
            return;
        }

        List<Faculty> faculties = FACULTY_DAO.getByDepartment(departmentId);
        List<Subject> subjects = SUBJECT_DAO.getByDepartment(departmentId);
        List<Section> sections = askSectionsForGeneration(departmentId, semesterNo);
        List<Classroom> classrooms = CLASSROOM_DAO.getAll();
        List<TimeSlot> slots = TIMESLOT_DAO.getAll();
        Map<Integer, Set<Integer>> availability = FACULTY_DAO.getAvailabilityMap();

        Department dept = DEPARTMENT_DAO.getById(departmentId);
        double budget = dept == null ? 0.0 : dept.getBudgetLimit();

        long gStart = System.currentTimeMillis();
        List<TimetableEntry> greedy = GREEDY_SCHEDULER.generate(
                departmentId, semester, faculties, subjects, sections, classrooms, slots, availability, budget
        );
        long greedyTime = System.currentTimeMillis() - gStart;

        long cStart = System.currentTimeMillis();
        List<TimetableEntry> colored = GRAPH_SCHEDULER.generate(
                departmentId, semester, faculties, subjects, sections, classrooms, slots, availability, budget
        );
        long colorTime = System.currentTimeMillis() - cStart;

        int requiredHours = subjects.stream().mapToInt(Subject::getWeeklyHours).sum() * sections.size();
        REPORT_GENERATOR.printAlgorithmPerformance("GREEDY", greedyTime, greedy.size(), CONFLICT_CHECKER.countConflicts(greedy), requiredHours);
        REPORT_GENERATOR.printAlgorithmPerformance("GRAPH", colorTime, colored.size(), CONFLICT_CHECKER.countConflicts(colored), requiredHours);
    }

    private static void showStoredTimetable() throws Exception {
        String semester = askSemester();
        int departmentId = askDepartmentId();
        if (departmentId <= 0) {
            System.out.println("Invalid department.");
            return;
        }

        List<TimetableEntry> entries = TIMETABLE_DAO.getBySemesterAndDepartment(semester, departmentId);
        if (entries.isEmpty()) {
            System.out.println("No timetable entries found.");
            return;
        }

        List<Faculty> faculties = FACULTY_DAO.getByDepartment(departmentId);
        List<Subject> subjects = SUBJECT_DAO.getByDepartment(departmentId);
        List<Section> sections = SECTION_DAO.getAll();
        List<Classroom> classrooms = CLASSROOM_DAO.getAll();
        List<TimeSlot> slots = TIMESLOT_DAO.getAll();
        REPORT_GENERATOR.printTimetable(entries, faculties, subjects, sections, classrooms, slots);
    }

    private static void exportStoredTimetableCsv() throws Exception {
        String semester = askSemester();
        int departmentId = askDepartmentId();
        if (departmentId <= 0) {
            System.out.println("Invalid department.");
            return;
        }

        List<TimetableEntry> entries = TIMETABLE_DAO.getBySemesterAndDepartment(semester, departmentId);
        if (entries.isEmpty()) {
            System.out.println("No timetable entries found.");
            return;
        }

        List<Faculty> faculties = FACULTY_DAO.getByDepartment(departmentId);
        List<Subject> subjects = SUBJECT_DAO.getByDepartment(departmentId);
        List<Section> sections = SECTION_DAO.getAll();
        List<Classroom> classrooms = CLASSROOM_DAO.getAll();
        List<TimeSlot> slots = TIMESLOT_DAO.getAll();
        String path = REPORT_GENERATOR.exportTimetableCsv(entries, faculties, subjects, sections, classrooms, slots, semester, departmentId);
        System.out.println("CSV exported to: " + path);
    }

    private static void manageMasterData() throws Exception {
        while (true) {
            System.out.println();
            System.out.println("----- Master Data -----");
            System.out.println("1. Department CRUD");
            System.out.println("2. Faculty CRUD");
            System.out.println("3. Subject CRUD");
            System.out.println("4. Classroom CRUD");
            System.out.println("5. TimeSlot CRUD");
            System.out.println("6. Section CRUD");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String c = SCANNER.nextLine().trim();
            switch (c) {
                case "1":
                    manageDepartments();
                    break;
                case "2":
                    manageFaculty();
                    break;
                case "3":
                    manageSubjects();
                    break;
                case "4":
                    manageClassrooms();
                    break;
                case "5":
                    manageTimeSlots();
                    break;
                case "6":
                    manageSections();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void manageDepartments() throws Exception {
        while (true) {
            System.out.println();
            System.out.println("Departments:");
            for (Department d : DEPARTMENT_DAO.getAll()) {
                System.out.println(d);
            }
            System.out.println("1. Add  2. Update  3. Delete  0. Back");
            System.out.print("Choice: ");
            String c = SCANNER.nextLine().trim();
            if ("0".equals(c)) {
                return;
            }
            if ("1".equals(c)) {
                String name = readNonEmpty("Name: ");
                double budget = readPositiveDouble("Budget limit: ");
                DEPARTMENT_DAO.save(new Department(0, name, budget));
            } else if ("2".equals(c)) {
                int id = readInt("Department ID: ");
                String name = readNonEmpty("New name: ");
                double budget = readPositiveDouble("New budget: ");
                DEPARTMENT_DAO.update(id, name, budget);
            } else if ("3".equals(c)) {
                int id = readInt("Department ID to delete: ");
                DEPARTMENT_DAO.delete(id);
            }
        }
    }

    private static void manageFaculty() throws Exception {
        while (true) {
            System.out.println();
            System.out.println("Faculty:");
            for (Faculty f : FACULTY_DAO.getAll()) {
                System.out.println(f);
            }
            System.out.println("1. Add  2. Update  3. Delete  0. Back");
            System.out.print("Choice: ");
            String c = SCANNER.nextLine().trim();
            if ("0".equals(c)) {
                return;
            }
            if ("1".equals(c)) {
                String name = readNonEmpty("Name: ");
                int departmentId = readInt("Department ID: ");
                int maxHours = readPositiveInt("Max hours/week: ");
                double cost = readPositiveDouble("Cost/hour: ");
                FACULTY_DAO.save(new Faculty(0, name, departmentId, maxHours, cost));
                FACULTY_DAO.seedFullAvailability();
                seedFacultyUsers();
            } else if ("2".equals(c)) {
                int id = readInt("Faculty ID: ");
                String name = readNonEmpty("New name: ");
                int departmentId = readInt("New Department ID: ");
                int maxHours = readPositiveInt("New Max hours/week: ");
                double cost = readPositiveDouble("New Cost/hour: ");
                FACULTY_DAO.update(id, name, departmentId, maxHours, cost);
            } else if ("3".equals(c)) {
                int id = readInt("Faculty ID to delete: ");
                FACULTY_DAO.delete(id);
            }
        }
    }

    private static void manageSubjects() throws Exception {
        while (true) {
            System.out.println();
            System.out.println("Subjects:");
            for (Subject s : SUBJECT_DAO.getAll()) {
                System.out.println(s);
            }
            System.out.println("1. Add  2. Update  3. Delete  0. Back");
            System.out.print("Choice: ");
            String c = SCANNER.nextLine().trim();
            if ("0".equals(c)) {
                return;
            }
            if ("1".equals(c)) {
                String name = readNonEmpty("Name: ");
                int weeklyHours = readPositiveInt("Weekly hours: ");
                int departmentId = readInt("Department ID: ");
                SUBJECT_DAO.save(new Subject(0, name, weeklyHours, departmentId));
            } else if ("2".equals(c)) {
                int id = readInt("Subject ID: ");
                String name = readNonEmpty("New name: ");
                int weeklyHours = readPositiveInt("New weekly hours: ");
                int departmentId = readInt("New department ID: ");
                SUBJECT_DAO.update(id, name, weeklyHours, departmentId);
            } else if ("3".equals(c)) {
                int id = readInt("Subject ID to delete: ");
                SUBJECT_DAO.delete(id);
            }
        }
    }

    private static void manageClassrooms() throws Exception {
        while (true) {
            System.out.println();
            System.out.println("Classrooms:");
            for (Classroom r : CLASSROOM_DAO.getAll()) {
                System.out.println(r);
            }
            System.out.println("1. Add  2. Update  3. Delete  0. Back");
            System.out.print("Choice: ");
            String c = SCANNER.nextLine().trim();
            if ("0".equals(c)) {
                return;
            }
            if ("1".equals(c)) {
                String roomCode = readNonEmpty("Room code: ");
                int capacity = readPositiveInt("Capacity: ");
                double cost = readPositiveDouble("Cost/hour: ");
                CLASSROOM_DAO.save(new Classroom(0, roomCode, capacity, cost));
            } else if ("2".equals(c)) {
                int id = readInt("Room ID: ");
                String roomCode = readNonEmpty("New room code: ");
                int capacity = readPositiveInt("New capacity: ");
                double cost = readPositiveDouble("New cost/hour: ");
                CLASSROOM_DAO.update(id, roomCode, capacity, cost);
            } else if ("3".equals(c)) {
                int id = readInt("Room ID to delete: ");
                CLASSROOM_DAO.delete(id);
            }
        }
    }

    private static void manageTimeSlots() throws Exception {
        while (true) {
            System.out.println();
            System.out.println("TimeSlots:");
            for (TimeSlot slot : TIMESLOT_DAO.getAll()) {
                System.out.println(slot.getSlotId() + " | " + slot);
            }
            System.out.println("1. Add  2. Update  3. Delete  0. Back");
            System.out.print("Choice: ");
            String c = SCANNER.nextLine().trim();
            if ("0".equals(c)) {
                return;
            }
            if ("1".equals(c)) {
                String day = readNonEmpty("Day (Mon/Tue/...): ");
                int period = readPositiveInt("Period (int): ");
                TIMESLOT_DAO.save(new TimeSlot(0, day, period));
                FACULTY_DAO.seedFullAvailability();
            } else if ("2".equals(c)) {
                int id = readInt("Slot ID: ");
                String day = readNonEmpty("New day: ");
                int period = readPositiveInt("New period: ");
                TIMESLOT_DAO.update(id, day, period);
            } else if ("3".equals(c)) {
                int id = readInt("Slot ID to delete: ");
                TIMESLOT_DAO.delete(id);
            }
        }
    }

    private static void manageSections() throws Exception {
        while (true) {
            System.out.println();
            System.out.println("Sections:");
            for (Section section : SECTION_DAO.getAll()) {
                System.out.println(section);
            }
            System.out.println("1. Add  2. Update  3. Delete  0. Back");
            System.out.print("Choice: ");
            String c = SCANNER.nextLine().trim();
            if ("0".equals(c)) {
                return;
            }
            if ("1".equals(c)) {
                String sectionName = readNonEmpty("Section name (A/B/...): ");
                int semesterNo = readPositiveInt("Semester no: ");
                int strength = readPositiveInt("Strength: ");
                int departmentId = readInt("Department ID: ");
                SECTION_DAO.save(new Section(0, sectionName, semesterNo, strength, departmentId));
            } else if ("2".equals(c)) {
                int id = readInt("Section ID: ");
                String sectionName = readNonEmpty("New section name: ");
                int semesterNo = readPositiveInt("New semester no: ");
                int strength = readPositiveInt("New strength: ");
                int departmentId = readInt("New department ID: ");
                SECTION_DAO.update(id, sectionName, semesterNo, strength, departmentId);
            } else if ("3".equals(c)) {
                int id = readInt("Section ID to delete: ");
                SECTION_DAO.delete(id);
            }
        }
    }

    private static int askDepartmentId() throws Exception {
        if (!isAdmin() && CURRENT_SESSION.facultyId != null) {
            Integer dept = getFacultyDepartmentId(CURRENT_SESSION.facultyId);
            if (dept != null) {
                return dept;
            }
        }

        List<Department> departments = DEPARTMENT_DAO.getAll();
        if (departments.isEmpty()) {
            return -1;
        }
        System.out.println("Departments:");
        for (Department d : departments) {
            System.out.println(d.getDepartmentId() + " - " + d.getName());
        }
        int chosen = readInt("Department ID: ");
        for (Department d : departments) {
            if (d.getDepartmentId() == chosen) {
                return chosen;
            }
        }
        return -1;
    }

    private static int askSemesterNumber() {
        return readPositiveInt("Semester number (e.g. 4): ");
    }

    private static List<Section> askSectionsForGeneration(int departmentId, int semesterNo) throws Exception {
        List<Section> sections = SECTION_DAO.getByDepartmentAndSemester(departmentId, semesterNo);
        if (sections.isEmpty()) {
            return sections;
        }
        System.out.println("Sections for dept " + departmentId + ", sem " + semesterNo + ":");
        for (Section section : sections) {
            System.out.println(section.getSectionId() + " - " + section.getSectionName());
        }
        System.out.print("Use all sections? (Y/N): ");
        String useAll = SCANNER.nextLine().trim();
        if (useAll.isEmpty() || useAll.equalsIgnoreCase("Y")) {
            return sections;
        }
        System.out.print("Enter section IDs comma-separated: ");
        String raw = SCANNER.nextLine().trim();
        if (raw.isEmpty()) {
            return sections;
        }
        List<Integer> ids = parseIdList(raw);
        List<Section> selected = new ArrayList<>();
        for (Section s : sections) {
            if (ids.contains(s.getSectionId())) {
                selected.add(s);
            }
        }
        return selected.isEmpty() ? sections : selected;
    }

    private static Integer getFacultyDepartmentId(int facultyId) throws Exception {
        String sql = "SELECT department_id FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("department_id");
                }
            }
        }
        return null;
    }

    private static void printRiskReport(
            List<TimetableEntry> entries,
            List<Faculty> faculties,
            List<TimeSlot> slots,
            List<Classroom> classrooms
    ) {
        Map<Integer, String> facultyRisk = RISK_ANALYZER.facultyLoadRisk(entries, faculties);
        int overloaded = 0;
        for (String risk : facultyRisk.values()) {
            if ("Overload".equals(risk)) {
                overloaded++;
            }
        }
        double overloadPercent = faculties.isEmpty() ? 0.0 : (overloaded * 100.0 / faculties.size());
        double roomUnderutilization = RISK_ANALYZER.roomUnderutilizationRisk(entries, slots.size(), classrooms.size()) * 100.0;
        REPORT_GENERATOR.printRiskSummary(overloadPercent, roomUnderutilization);
    }

    private static Session login() {
        System.out.println("Login required. Default admin credentials: admin / admin123");
        for (int attempt = 0; attempt < 3; attempt++) {
            String username = readNonEmpty("Username: ");
            String password = readNonEmpty("Password: ");
            try {
                Session session = authenticate(username, password);
                if (session != null) {
                    System.out.println("Logged in as " + session.role);
                    return session;
                }
                System.out.println("Invalid credentials.");
            } catch (Exception e) {
                System.out.println("Login error: " + e.getMessage());
            }
        }
        return null;
    }

    private static Session authenticate(String username, String password) throws Exception {
        String sql = "SELECT role, faculty_id FROM users WHERE username = ? AND password_hash = SHA2(?, 256)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    Integer facultyId = (Integer) rs.getObject("faculty_id");
                    return new Session(role, facultyId);
                }
            }
        }
        return null;
    }

    private static void requireAdmin() {
        if (!isAdmin()) {
            throw new SecurityException("Access denied: admin-only operation.");
        }
    }

    private static boolean isAdmin() {
        return CURRENT_SESSION != null && ROLE_ADMIN.equalsIgnoreCase(CURRENT_SESSION.role);
    }

    private static String askSemester() {
        System.out.print("Semester code (e.g. 2026S1): ");
        String semester = SCANNER.nextLine().trim();
        return semester.isEmpty() ? "2026S1" : semester;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ignored) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException ignored) {
                System.out.println("Enter a valid decimal number.");
            }
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Value must be greater than 0.");
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            double value = readDouble(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Value must be greater than 0.");
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Value cannot be empty.");
        }
    }

    private static List<Integer> parseIdList(String raw) {
        List<Integer> ids = new ArrayList<>();
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                ids.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ignored) {
                // skip invalid token
            }
        }
        return ids;
    }

    private static final class Session {
        private final String role;
        private final Integer facultyId;

        private Session(String role, Integer facultyId) {
            this.role = role == null ? ROLE_FACULTY : role.toUpperCase();
            this.facultyId = facultyId;
        }
    }
}
