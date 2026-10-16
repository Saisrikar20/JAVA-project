package com.hostel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * =====================================================================
 * MEMBER 1 MODULE: User Identity & Student Management
 * Assigned Tables: 1. users | 2. students
 * OOP Concepts:    Encapsulation (Private fields) & Inheritance (User -> Student)
 * DBMS Concepts:   CRUD Operations using PreparedStatement
 * =====================================================================
 */

// --- OOP INHERITANCE: Base Class ---
class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private String fullName;

    public User(int userId, String username, String password, String role, String fullName) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    // Encapsulation: Getters and Setters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
}

// --- OOP INHERITANCE: Derived Subclass ---
class Student extends User {
    private String regNo;
    private String gender;
    private String department;
    private String phone;

    public Student(int userId, String username, String password, String fullName,
                   String regNo, String gender, String department, String phone) {
        super(userId, username, password, "STUDENT", fullName);
        this.regNo = regNo;
        this.gender = gender;
        this.department = department;
        this.phone = phone;
    }

    public String getRegNo() { return regNo; }
    public String getGender() { return gender; }
    public String getDepartment() { return department; }
    public String getPhone() { return phone; }
}

public class StudentModule {
    private final Connection conn;

    public StudentModule() {
        this.conn = Database.getConnection();
    }

    // DBMS: Authenticate user by username and password (PreparedStatement)
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("username"),
                                    rs.getString("password"), rs.getString("role"),
                                    rs.getString("full_name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // DBMS: Fetch Student details (JOIN across users and students tables)
    public Student getStudent(int userId) {
        String sql = "SELECT u.*, s.reg_no, s.gender, s.department, s.phone " +
                     "FROM users u JOIN students s ON u.user_id = s.student_id WHERE u.user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(rs.getInt("user_id"), rs.getString("username"),
                                       rs.getString("password"), rs.getString("full_name"),
                                       rs.getString("reg_no"), rs.getString("gender"),
                                       rs.getString("department"), rs.getString("phone"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student: " + e.getMessage());
        }
        return null;
    }

    // DBMS: Register a new student into tables 1 and 2
    public boolean registerStudent(String username, String password, String fullName,
                                   String regNo, String gender, String dept, String phone) {
        String userSql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, 'STUDENT', ?)";
        String studentSql = "INSERT INTO students (student_id, reg_no, gender, department, phone) VALUES (?, ?, ?, ?, ?)";
        try {
            conn.setAutoCommit(false);
            int newUserId;
            try (PreparedStatement ps1 = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps1.setString(1, username);
                ps1.setString(2, password);
                ps1.setString(3, fullName);
                ps1.executeUpdate();
                var keys = ps1.getGeneratedKeys();
                keys.next();
                newUserId = keys.getInt(1);
            }

            try (PreparedStatement ps2 = conn.prepareStatement(studentSql)) {
                ps2.setInt(1, newUserId);
                ps2.setString(2, regNo);
                ps2.setString(3, gender);
                ps2.setString(4, dept);
                ps2.setString(5, phone);
                ps2.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            System.err.println("Error registering student: " + e.getMessage());
            return false;
        }
    }

    // DBMS: View all registered students
    public void displayAllStudents() {
        String sql = "SELECT u.user_id, s.reg_no, u.full_name, s.department, s.phone " +
                     "FROM users u JOIN students s ON u.user_id = s.student_id";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            System.out.printf("%-6s %-12s %-20s %-10s %-12s\n", "ID", "Reg No", "Name", "Dept", "Phone");
            System.out.println("------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-6d %-12s %-20s %-10s %-12s\n",
                        rs.getInt("user_id"), rs.getString("reg_no"),
                        rs.getString("full_name"), rs.getString("department"), rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.err.println("Error displaying students: " + e.getMessage());
        }
    }
}
