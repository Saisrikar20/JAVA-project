package com.hostel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * =====================================================================
 * MEMBER 3 MODULE: Staff Operations & Room Allocations
 * Assigned Tables: 5. wardens | 6. allocations
 * OOP Concepts:    Composition & Domain Validation
 * DBMS Concepts:   ACID Transactions (Multi-table commit & rollback)
 * =====================================================================
 */
public class AllocationModule {
    private final Connection conn;

    public AllocationModule() {
        this.conn = Database.getConnection();
    }

    // DBMS ACID TRANSACTION: Atomically book a room for a student
    public boolean allocateRoom(int studentId, int roomId) {
        String checkStudent = "SELECT COUNT(*) FROM allocations WHERE student_id = ? AND status = 'ACTIVE'";
        String checkRoom = "SELECT capacity, beds_occupied FROM rooms WHERE room_id = ?";
        String insertAlloc = "INSERT INTO allocations (student_id, room_id, start_date, status) VALUES (?, ?, '2026-07-01', 'ACTIVE')";
        String updateRoom = "UPDATE rooms SET beds_occupied = beds_occupied + 1 WHERE room_id = ?";

        try {
            // STEP 1: Turn off AutoCommit to begin transaction
            conn.setAutoCommit(false);

            // Validation 1: Student not already allocated
            try (PreparedStatement ps1 = conn.prepareStatement(checkStudent)) {
                ps1.setInt(1, studentId);
                var rs1 = ps1.executeQuery();
                if (rs1.next() && rs1.getInt(1) > 0) {
                    System.out.println("Error: Student already has an active room allocation.");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            // Validation 2: Room has vacancy
            try (PreparedStatement ps2 = conn.prepareStatement(checkRoom)) {
                ps2.setInt(1, roomId);
                var rs2 = ps2.executeQuery();
                if (!rs2.next() || rs2.getInt("beds_occupied") >= rs2.getInt("capacity")) {
                    System.out.println("Error: Room is full or does not exist.");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            // Operation 1: Insert allocation record
            try (PreparedStatement ps3 = conn.prepareStatement(insertAlloc)) {
                ps3.setInt(1, studentId);
                ps3.setInt(2, roomId);
                ps3.executeUpdate();
            }

            // Operation 2: Update room vacancy count
            try (PreparedStatement ps4 = conn.prepareStatement(updateRoom)) {
                ps4.setInt(1, roomId);
                ps4.executeUpdate();
            }

            // STEP 2: Commit transaction
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try {
                // STEP 3: Rollback on any failure
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
            System.err.println("Transaction rolled back: " + e.getMessage());
            return false;
        }
    }

    // DBMS ACID TRANSACTION: Vacate student and restore bed vacancy
    public boolean vacateRoom(int studentId) {
        String findAlloc = "SELECT alloc_id, room_id FROM allocations WHERE student_id = ? AND status = 'ACTIVE'";
        String updateAlloc = "UPDATE allocations SET status = 'VACATED' WHERE alloc_id = ?";
        String updateRoom = "UPDATE rooms SET beds_occupied = MAX(0, beds_occupied - 1) WHERE room_id = ?";

        try {
            conn.setAutoCommit(false);
            int allocId = -1, roomId = -1;

            try (PreparedStatement ps = conn.prepareStatement(findAlloc)) {
                ps.setInt(1, studentId);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    allocId = rs.getInt("alloc_id");
                    roomId = rs.getInt("room_id");
                }
            }

            if (allocId == -1) {
                System.out.println("Error: No active allocation found for this student.");
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }

            try (PreparedStatement ps1 = conn.prepareStatement(updateAlloc)) {
                ps1.setInt(1, allocId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateRoom)) {
                ps2.setInt(1, roomId);
                ps2.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            System.err.println("Vacate transaction rolled back: " + e.getMessage());
            return false;
        }
    }

    // DBMS: View all active allocations
    public void displayAllocations() {
        String sql = "SELECT a.alloc_id, u.full_name, s.reg_no, r.room_no, b.block_name, a.start_date " +
                     "FROM allocations a JOIN students s ON a.student_id = s.student_id " +
                     "JOIN users u ON s.student_id = u.user_id " +
                     "JOIN rooms r ON a.room_id = r.room_id " +
                     "JOIN hostel_blocks b ON r.block_id = b.block_id " +
                     "WHERE a.status = 'ACTIVE'";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            System.out.printf("%-8s %-20s %-12s %-18s %-10s %-12s\n", "AllocID", "Student Name", "Reg No", "Block", "Room No", "Date");
            System.out.println("-----------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-8d %-20s %-12s %-18s %-10s %-12s\n",
                        rs.getInt("alloc_id"), rs.getString("full_name"), rs.getString("reg_no"),
                        rs.getString("block_name"), rs.getString("room_no"), rs.getString("start_date"));
            }
        } catch (SQLException e) {
            System.err.println("Error displaying allocations: " + e.getMessage());
        }
    }

    // DBMS: Return all active allocations as JSON for Web API
    public String getAllocationsJson() {
        StringBuilder sb = new StringBuilder("[");
        String sql = "SELECT a.alloc_id, a.student_id, a.room_id, u.full_name, s.reg_no, r.room_no, b.block_name, a.start_date " +
                     "FROM allocations a JOIN students s ON a.student_id = s.student_id " +
                     "JOIN users u ON s.student_id = u.user_id " +
                     "JOIN rooms r ON a.room_id = r.room_id " +
                     "JOIN hostel_blocks b ON r.block_id = b.block_id " +
                     "WHERE a.status = 'ACTIVE' ORDER BY a.alloc_id ASC";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append(String.format("{\"allocId\":%d,\"studentId\":%d,\"roomId\":%d,\"studentName\":\"%s\",\"regNo\":\"%s\",\"roomNo\":\"%s\",\"block\":\"%s\",\"startDate\":\"%s\"}",
                        rs.getInt("alloc_id"), rs.getInt("student_id"), rs.getInt("room_id"),
                        rs.getString("full_name"), rs.getString("reg_no"),
                        rs.getString("room_no"), rs.getString("block_name"),
                        rs.getString("start_date")));
                first = false;
            }
        } catch (SQLException e) {
            System.err.println("Error generating allocations JSON: " + e.getMessage());
        }
        sb.append("]");
        return sb.toString();
    }
}
