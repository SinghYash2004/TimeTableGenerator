package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.audit.AuditLogService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BulkImportController {
    private final JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;

    public BulkImportController(JdbcTemplate jdbcTemplate, AuditLogService auditLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/settings/bulk/import/{type}")
    public String importCsv(
            HttpSession session,
            @PathVariable String type,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/dashboard";
        }
        if (file == null || file.isEmpty()) {
            return "redirect:/settings?message=Please%20upload%20a%20CSV%20file.";
        }
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        List<String[]> rows = parseCsv(content);
        int inserted = 0;
        int skipped = 0;

        for (String[] row : rows) {
            if (row.length == 0 || isHeader(type, row)) {
                continue;
            }
            try {
                boolean ok = switch (type.toLowerCase()) {
                    case "departments" -> importDepartment(row);
                    case "faculty" -> importFaculty(row);
                    case "rooms" -> importRoom(row);
                    case "subjects" -> importSubject(row);
                    case "sections" -> importSection(row);
                    default -> false;
                };
                if (ok) {
                    inserted++;
                } else {
                    skipped++;
                }
            } catch (Exception ignored) {
                skipped++;
            }
        }

        auditLogService.record(user.getUsername(), "BULK_IMPORT",
                "type=" + type + ", inserted=" + inserted + ", skipped=" + skipped);
        return "redirect:/settings?message=Imported%20" + inserted + "%20rows,%20skipped%20" + skipped + ".";
    }

    @GetMapping("/settings/bulk/export/{type}")
    public void exportCsv(
            HttpSession session,
            @PathVariable String type,
            HttpServletResponse response
    ) throws Exception {
        UserSession user = (UserSession) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            response.sendError(401);
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + type + ".csv\"");
        String csv = switch (type.toLowerCase()) {
            case "departments" -> exportDepartments();
            case "faculty" -> exportFaculty();
            case "rooms" -> exportRooms();
            case "subjects" -> exportSubjects();
            case "sections" -> exportSections();
            default -> null;
        };
        if (csv == null) {
            response.sendError(400);
            return;
        }
        auditLogService.record(user.getUsername(), "BULK_EXPORT", "type=" + type);
        response.getWriter().write(csv);
    }

    private List<String[]> parseCsv(String content) {
        List<String[]> rows = new ArrayList<>();
        String[] lines = content.replace("\r", "").split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String clean = stripBom(line).trim();
            if (clean.isEmpty()) {
                continue;
            }
            rows.add(clean.split(",", -1));
        }
        return rows;
    }

    private String stripBom(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }
        return line.charAt(0) == '\uFEFF' ? line.substring(1) : line;
    }

    private boolean isHeader(String type, String[] row) {
        String joined = String.join(",", row).toLowerCase();
        return switch (type.toLowerCase()) {
            case "departments" -> joined.contains("budget") && joined.contains("department");
            case "faculty" -> joined.contains("faculty") || joined.contains("max_hours");
            case "rooms" -> joined.contains("room") && joined.contains("capacity");
            case "subjects" -> joined.contains("weekly") && joined.contains("subject");
            case "sections" -> joined.contains("section") && joined.contains("semester");
            default -> false;
        };
    }

    private boolean importDepartment(String[] row) {
        if (row.length < 2) {
            return false;
        }
        String name = row[0].trim();
        double budget = Double.parseDouble(row[1].trim());
        jdbcTemplate.update(
                "INSERT INTO department(name, budget_limit) VALUES(?, ?) " +
                        "ON DUPLICATE KEY UPDATE budget_limit=VALUES(budget_limit)",
                name, budget
        );
        return true;
    }

    private boolean importFaculty(String[] row) {
        if (row.length < 4) {
            return false;
        }
        String name = row[0].trim();
        int departmentId = Integer.parseInt(row[1].trim());
        int maxHours = Integer.parseInt(row[2].trim());
        double cost = Double.parseDouble(row[3].trim());
        jdbcTemplate.update(
                "INSERT INTO faculty(name, department_id, max_hours_per_week, cost_per_hour) VALUES(?, ?, ?, ?)",
                name, departmentId, maxHours, cost
        );
        return true;
    }

    private boolean importRoom(String[] row) {
        if (row.length < 3) {
            return false;
        }
        String code = row[0].trim();
        String building = (row.length >= 4 && row[3] != null && !row[3].trim().isEmpty()) ? row[3].trim() : "MAIN";
        int floorNo = (row.length >= 5 && row[4] != null && !row[4].trim().isEmpty())
                ? Integer.parseInt(row[4].trim())
                : 0;
        String roomType = (row.length >= 6 && row[5] != null && !row[5].trim().isEmpty())
                ? row[5].trim()
                : "LECTURE";
        String equipmentTags = (row.length >= 7 && row[6] != null && !row[6].trim().isEmpty())
                ? row[6].trim()
                : null;
        int capacity = Integer.parseInt(row[1].trim());
        double cost = Double.parseDouble(row[2].trim());
        jdbcTemplate.update(
                "INSERT INTO classroom(room_code, building, floor_no, room_type, equipment_tags, capacity, cost_per_hour) VALUES(?, ?, ?, ?, ?, ?, ?)",
                code, building, floorNo, roomType, equipmentTags, capacity, cost
        );
        return true;
    }

    private boolean importSubject(String[] row) {
        if (row.length < 3) {
            return false;
        }
        String name = row[0].trim();
        int weekly = Integer.parseInt(row[1].trim());
        int departmentId = Integer.parseInt(row[2].trim());
        jdbcTemplate.update(
                "INSERT INTO subject(name, weekly_hours, department_id) VALUES(?, ?, ?)",
                name, weekly, departmentId
        );
        return true;
    }

    private boolean importSection(String[] row) {
        if (row.length < 4) {
            return false;
        }
        String name = row[0].trim();
        int semesterNo = Integer.parseInt(row[1].trim());
        int strength = Integer.parseInt(row[2].trim());
        int departmentId = Integer.parseInt(row[3].trim());
        jdbcTemplate.update(
                "INSERT INTO section(section_name, semester_no, strength, department_id) VALUES(?, ?, ?, ?)",
                name, semesterNo, strength, departmentId
        );
        return true;
    }

    private String exportDepartments() {
        StringBuilder sb = new StringBuilder("department_name,budget_limit\n");
        jdbcTemplate.query(
                "SELECT name, budget_limit FROM department ORDER BY department_id",
                rs -> {
                    sb.append(rs.getString("name")).append(',')
                            .append(rs.getDouble("budget_limit")).append('\n');
                }
        );
        return sb.toString();
    }

    private String exportFaculty() {
        StringBuilder sb = new StringBuilder("faculty_name,department_id,max_hours_per_week,cost_per_hour\n");
        jdbcTemplate.query(
                "SELECT name, department_id, max_hours_per_week, cost_per_hour FROM faculty ORDER BY faculty_id",
                rs -> {
                    sb.append(rs.getString("name")).append(',')
                            .append(rs.getInt("department_id")).append(',')
                            .append(rs.getInt("max_hours_per_week")).append(',')
                            .append(rs.getDouble("cost_per_hour")).append('\n');
                }
        );
        return sb.toString();
    }

    private String exportRooms() {
        StringBuilder sb = new StringBuilder("room_code,capacity,cost_per_hour,building,floor_no,room_type,equipment_tags\n");
        jdbcTemplate.query(
                "SELECT room_code, capacity, cost_per_hour, building, floor_no, room_type, equipment_tags FROM classroom ORDER BY room_id",
                rs -> {
                    sb.append(rs.getString("room_code")).append(',')
                            .append(rs.getInt("capacity")).append(',')
                            .append(rs.getDouble("cost_per_hour")).append(',')
                            .append(rs.getString("building")).append(',')
                            .append(rs.getInt("floor_no")).append(',')
                            .append(rs.getString("room_type")).append(',')
                            .append(rs.getString("equipment_tags") == null ? "" : rs.getString("equipment_tags"))
                            .append('\n');
                }
        );
        return sb.toString();
    }

    private String exportSubjects() {
        StringBuilder sb = new StringBuilder("subject_name,weekly_hours,department_id\n");
        jdbcTemplate.query(
                "SELECT name, weekly_hours, department_id FROM subject ORDER BY subject_id",
                rs -> {
                    sb.append(rs.getString("name")).append(',')
                            .append(rs.getInt("weekly_hours")).append(',')
                            .append(rs.getInt("department_id")).append('\n');
                }
        );
        return sb.toString();
    }

    private String exportSections() {
        StringBuilder sb = new StringBuilder("section_name,semester_no,strength,department_id\n");
        jdbcTemplate.query(
                "SELECT section_name, semester_no, strength, department_id FROM section ORDER BY section_id",
                rs -> {
                    sb.append(rs.getString("section_name")).append(',')
                            .append(rs.getInt("semester_no")).append(',')
                            .append(rs.getInt("strength")).append(',')
                            .append(rs.getInt("department_id")).append('\n');
                }
        );
        return sb.toString();
    }
}
