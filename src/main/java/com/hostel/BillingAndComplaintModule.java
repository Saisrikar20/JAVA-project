package com.hostel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * =====================================================================
 * MEMBER 4 MODULE: Fee Billing & Grievance Redressal
 * Assigned Tables: 7. fee_payments | 8. complaints
 * OOP Concepts:    Polymorphic Rate Integration & Lifecycle Tracking
 * DBMS Concepts:   SQL Aggregations (SUM), Multi-table Tracking
 * =====================================================================
 */
public class BillingAndComplaintModule {
    private final Connection conn;
    private final RoomModule roomModule;

    public BillingAndComplaintModule() {
        this.conn = Database.getConnection();
        this.roomModule = new RoomModule();
    }

    // OOP POLYMORPHIC INTEGRATION: Computes semester bill based on allocated room
    public double calculateTotalSemesterBill(int studentId) {
        String sql = "SELECT r.* FROM rooms r JOIN allocations a ON r.room_id = a.room_id " +
                     "WHERE a.student_id = ? AND a.status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Room r = roomModule.createRoomInstance(rs.getInt("room_id"), rs.getInt("block_id"),
                            rs.getString("room_no"), rs.getString("room_type"),
                            rs.getInt("capacity"), rs.getInt("beds_occupied"), rs.getDouble("base_rent"));

                    // Polymorphic call: r.calculateRent() * 6 months + 15,000 mess charges
                    return (r.calculateRent() * 6.0) + 15000.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Bill calculation error: " + e.getMessage());
        }
        return 0.0;
    }

    // DBMS AGGREGATION: SUM(amount) for student payments
    public double getTotalPaid(int studentId) {
        String sql = "SELECT SUM(amount) AS total FROM fee_payments WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            System.err.println("Error calculating total paid: " + e.getMessage());
        }
        return 0.0;
    }

    // DBMS: Record payment transaction into Table 7
    public boolean recordPayment(int studentId, double amount, String feeType) {
        String sql = "INSERT INTO fee_payments (student_id, amount, fee_type, payment_date) VALUES (?, ?, ?, '2026-07-02')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setDouble(2, amount);
            ps.setString(3, feeType);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Payment error: " + e.getMessage());
            return false;
        }
    }

    // DBMS: File a grievance into Table 8
    public boolean lodgeComplaint(int studentId, String category, String title) {
        String sql = "INSERT INTO complaints (student_id, category, title, status) VALUES (?, ?, ?, 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, category.toUpperCase());
            ps.setString(3, title);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Complaint filing error: " + e.getMessage());
            return false;
        }
    }

    // DBMS: Update grievance status in Table 8
    public boolean resolveComplaint(int complaintId, String notes) {
        String sql = "UPDATE complaints SET status = 'RESOLVED', resolution_notes = ? WHERE complaint_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notes);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resolving complaint: " + e.getMessage());
            return false;
        }
    }

    // DBMS: Display complaints
    public void displayComplaints() {
        String sql = "SELECT c.complaint_id, u.full_name, s.reg_no, c.category, c.title, c.status, c.resolution_notes " +
                     "FROM complaints c JOIN students s ON c.student_id = s.student_id " +
                     "JOIN users u ON s.student_id = u.user_id";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            System.out.printf("%-6s %-18s %-10s %-12s %-25s %-12s\n", "ID", "Student", "Reg No", "Category", "Title", "Status");
            System.out.println("----------------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-6d %-18s %-10s %-12s %-25s %-12s\n",
                        rs.getInt("complaint_id"), rs.getString("full_name"), rs.getString("reg_no"),
                        rs.getString("category"), rs.getString("title"), rs.getString("status"));
                String notes = rs.getString("resolution_notes");
                if (notes != null && !notes.isEmpty()) {
                    System.out.println("   └─ Resolution: " + notes);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error displaying complaints: " + e.getMessage());
        }
    }

    // DBMS: Display financial audit summary
    public void displayFinancialAudit() {
        String sql = "SELECT p.payment_id, u.full_name, s.reg_no, p.fee_type, p.amount, p.payment_date " +
                     "FROM fee_payments p JOIN students s ON p.student_id = s.student_id " +
                     "JOIN users u ON s.student_id = u.user_id";
        double grandTotal = 0.0;
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            System.out.printf("%-6s %-18s %-10s %-15s %-12s %-12s\n", "ID", "Student", "Reg No", "Fee Type", "Amount", "Date");
            System.out.println("-----------------------------------------------------------------------------");
            while (rs.next()) {
                double amt = rs.getDouble("amount");
                grandTotal += amt;
                System.out.printf("%-6d %-18s %-10s %-15s ₹%-11.2f %-12s\n",
                        rs.getInt("payment_id"), rs.getString("full_name"), rs.getString("reg_no"),
                        rs.getString("fee_type"), amt, rs.getString("payment_date"));
            }
            System.out.println("-----------------------------------------------------------------------------");
            System.out.printf("TOTAL COLLECTIONS: ₹%.2f\n", grandTotal);
        } catch (SQLException e) {
            System.err.println("Error displaying audit: " + e.getMessage());
        }
    }
}
