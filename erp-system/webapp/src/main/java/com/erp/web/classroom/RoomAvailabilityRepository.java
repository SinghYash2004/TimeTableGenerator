package com.erp.web.classroom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class RoomAvailabilityRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoomAvailabilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoomAvailabilityView> findFreeRoomsForSlots(
            String semester,
            List<Integer> slotIds,
            String building,
            Integer minCapacity,
            String roomType,
            List<String> equipmentTags,
            LocalDate date
    ) {
        if (slotIds == null || slotIds.isEmpty()) {
            return List.of();
        }
        String slotIn = inClause(slotIds.size());
        List<Object> args = new ArrayList<>();
        StringBuilder filters = new StringBuilder();

        if (building != null && !building.isBlank()) {
            filters.append(" AND c.building = ? ");
            args.add(building.trim());
        }
        if (minCapacity != null && minCapacity > 0) {
            filters.append(" AND c.capacity >= ? ");
            args.add(minCapacity);
        }
        if (roomType != null && !roomType.isBlank()) {
            filters.append(" AND c.room_type = ? ");
            args.add(roomType.trim());
        }
        if (equipmentTags != null) {
            for (String tag : equipmentTags) {
                if (tag == null || tag.isBlank()) {
                    continue;
                }
                filters.append(" AND LOWER(COALESCE(c.equipment_tags, '')) LIKE ? ");
                args.add("%" + tag.toLowerCase(Locale.ROOT) + "%");
            }
        }

        String sql =
                "SELECT c.room_id, c.room_code, c.building, c.capacity " +
                        "FROM classroom c " +
                        "WHERE 1=1 " + filters +
                        "AND NOT EXISTS (" +
                        "   SELECT 1 FROM timetable t " +
                        "   WHERE t.room_id = c.room_id AND t.semester = ? AND t.slot_id IN " + slotIn +
                        "   AND NOT EXISTS (" +
                        "       SELECT 1 FROM class_cancellation cc WHERE cc.timetable_id = t.id AND cc.cancel_date = ? " +
                        "   )" +
                        ") " +
                        "AND NOT EXISTS (" +
                        "   SELECT 1 FROM room_reservation r " +
                        "   WHERE r.room_id = c.room_id AND r.semester = ? AND r.slot_id IN " + slotIn +
                        "   AND r.reserve_date = ? " +
                        ") " +
                        "ORDER BY c.room_code";

        args.add(semester);
        args.addAll(slotIds);
        args.add(date);
        args.add(semester);
        args.addAll(slotIds);
        args.add(date);

        return jdbcTemplate.query(
                sql,
                (rs, i) -> new RoomAvailabilityView(
                        rs.getInt("room_id"),
                        rs.getString("room_code"),
                        rs.getString("building"),
                        rs.getInt("capacity")
                ),
                args.toArray()
        );
    }

    public List<RoomStatusView> findRoomStatusesForSlots(
            String semester,
            List<Integer> slotIds,
            String building,
            Integer minCapacity,
            String roomType,
            List<String> equipmentTags,
            LocalDate date
    ) {
        if (slotIds == null || slotIds.isEmpty()) {
            return List.of();
        }
        String slotIn = inClause(slotIds.size());
        List<Object> args = new ArrayList<>();
        StringBuilder filters = new StringBuilder();

        if (building != null && !building.isBlank()) {
            filters.append(" AND c.building = ? ");
            args.add(building.trim());
        }
        if (minCapacity != null && minCapacity > 0) {
            filters.append(" AND c.capacity >= ? ");
            args.add(minCapacity);
        }
        if (roomType != null && !roomType.isBlank()) {
            filters.append(" AND c.room_type = ? ");
            args.add(roomType.trim());
        }
        if (equipmentTags != null) {
            for (String tag : equipmentTags) {
                if (tag == null || tag.isBlank()) {
                    continue;
                }
                filters.append(" AND LOWER(COALESCE(c.equipment_tags, '')) LIKE ? ");
                args.add("%" + tag.toLowerCase(Locale.ROOT) + "%");
            }
        }

        String sql =
                "SELECT c.room_id, c.room_code, c.building, c.floor_no, c.room_type, c.equipment_tags, c.capacity, " +
                        "CASE " +
                        "  WHEN EXISTS (" +
                        "    SELECT 1 FROM room_reservation r " +
                        "    WHERE r.room_id = c.room_id AND r.semester = ? AND r.slot_id IN " + slotIn +
                        "    AND r.reserve_date = ? AND r.reservation_type = 'MAINTENANCE'" +
                        "  ) THEN 'MAINTENANCE' " +
                        "  WHEN EXISTS (" +
                        "    SELECT 1 FROM room_reservation r " +
                        "    WHERE r.room_id = c.room_id AND r.semester = ? AND r.slot_id IN " + slotIn +
                        "    AND r.reserve_date = ? " +
                        "  ) THEN 'RESERVED' " +
                        "  WHEN EXISTS (" +
                        "    SELECT 1 FROM timetable t " +
                        "    WHERE t.room_id = c.room_id AND t.semester = ? AND t.slot_id IN " + slotIn +
                        "    AND NOT EXISTS (" +
                        "      SELECT 1 FROM class_cancellation cc WHERE cc.timetable_id = t.id AND cc.cancel_date = ? " +
                        "    )" +
                        "  ) THEN 'OCCUPIED' " +
                        "  ELSE 'FREE' " +
                        "END AS status " +
                        "FROM classroom c " +
                        "WHERE 1=1 " + filters +
                        "ORDER BY c.room_code";

        args.add(semester);
        args.addAll(slotIds);
        args.add(date);
        args.add(semester);
        args.addAll(slotIds);
        args.add(date);
        args.add(semester);
        args.addAll(slotIds);
        args.add(date);

        return jdbcTemplate.query(
                sql,
                (rs, i) -> new RoomStatusView(
                        rs.getInt("room_id"),
                        rs.getString("room_code"),
                        rs.getString("building"),
                        rs.getInt("floor_no"),
                        rs.getString("room_type"),
                        rs.getString("equipment_tags"),
                        rs.getInt("capacity"),
                        rs.getString("status"),
                        rs.getString("status")
                ),
                args.toArray()
        );
    }

    public boolean isRoomFreeForSlots(String semester, int roomId, List<Integer> slotIds, LocalDate date) {
        if (slotIds == null || slotIds.isEmpty()) {
            return false;
        }
        String slotIn = inClause(slotIds.size());
        List<Object> args = new ArrayList<>();
        String sql =
                "SELECT COUNT(*) FROM classroom c " +
                        "WHERE c.room_id = ? " +
                        "AND NOT EXISTS (" +
                        "   SELECT 1 FROM timetable t " +
                        "   WHERE t.room_id = c.room_id AND t.semester = ? AND t.slot_id IN " + slotIn +
                        "   AND NOT EXISTS (" +
                        "       SELECT 1 FROM class_cancellation cc WHERE cc.timetable_id = t.id AND cc.cancel_date = ? " +
                        "   )" +
                        ") " +
                        "AND NOT EXISTS (" +
                        "   SELECT 1 FROM room_reservation r " +
                        "   WHERE r.room_id = c.room_id AND r.semester = ? AND r.slot_id IN " + slotIn +
                        "   AND r.reserve_date = ? " +
                        ")";
        args.add(roomId);
        args.add(semester);
        args.addAll(slotIds);
        args.add(date);
        args.add(semester);
        args.addAll(slotIds);
        args.add(date);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args.toArray());
        return count != null && count > 0;
    }

    private String inClause(int size) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");
        return sb.toString();
    }
}
