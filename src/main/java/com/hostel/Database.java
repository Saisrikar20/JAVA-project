package com.hostel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Shared Database Manager
 * Sets up and manages the 8 core relational tables:
 * 1. users          2. students        3. hostel_blocks   4. rooms
 * 5. wardens        6. allocations     7. fee_payments    8. complaints
 */
public class Database {
    private static Connection connection;

    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:hostel.db");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                createTables();
            } catch (SQLException e) {
                System.err.println("Database Connection Error: " + e.getMessage());
            }
        }
        return connection;
    }

    private static void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Table 1: users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, role TEXT NOT NULL, full_name TEXT NOT NULL);");

            // Table 2: students
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "student_id INTEGER PRIMARY KEY, reg_no TEXT UNIQUE NOT NULL, gender TEXT NOT NULL, " +
                    "department TEXT NOT NULL, phone TEXT NOT NULL, " +
                    "FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE);");

            // Table 3: hostel_blocks
            stmt.execute("CREATE TABLE IF NOT EXISTS hostel_blocks (" +
                    "block_id INTEGER PRIMARY KEY AUTOINCREMENT, block_name TEXT UNIQUE NOT NULL, gender_type TEXT NOT NULL);");

            // Table 4: rooms
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_id INTEGER PRIMARY KEY AUTOINCREMENT, block_id INTEGER NOT NULL, room_no TEXT NOT NULL, " +
                    "room_type TEXT NOT NULL, capacity INTEGER NOT NULL, beds_occupied INTEGER DEFAULT 0, " +
                    "base_rent REAL NOT NULL, UNIQUE(block_id, room_no), " +
                    "FOREIGN KEY (block_id) REFERENCES hostel_blocks(block_id));");

            // Table 5: wardens
            stmt.execute("CREATE TABLE IF NOT EXISTS wardens (" +
                    "warden_id INTEGER PRIMARY KEY, block_id INTEGER, qualification TEXT, " +
                    "FOREIGN KEY (warden_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (block_id) REFERENCES hostel_blocks(block_id));");

            // Table 6: allocations
            stmt.execute("CREATE TABLE IF NOT EXISTS allocations (" +
                    "alloc_id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, " +
                    "room_id INTEGER NOT NULL, start_date TEXT NOT NULL, status TEXT DEFAULT 'ACTIVE', " +
                    "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                    "FOREIGN KEY (room_id) REFERENCES rooms(room_id));");

            // Table 7: fee_payments
            stmt.execute("CREATE TABLE IF NOT EXISTS fee_payments (" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, fee_type TEXT NOT NULL, payment_date TEXT NOT NULL, " +
                    "FOREIGN KEY (student_id) REFERENCES students(student_id));");

            // Table 8: complaints
            stmt.execute("CREATE TABLE IF NOT EXISTS complaints (" +
                    "complaint_id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, " +
                    "category TEXT NOT NULL, title TEXT NOT NULL, status TEXT DEFAULT 'PENDING', " +
                    "resolution_notes TEXT, FOREIGN KEY (student_id) REFERENCES students(student_id));");

            // Seed initial mock data if users table is empty
            var rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM users;");
            if (rs.next() && rs.getInt("total") == 0) {
                seedData(stmt);
            }
        } catch (SQLException e) {
            System.err.println("Table Creation Error: " + e.getMessage());
        }
    }

    private static void seedData(Statement s) throws SQLException {
        // Blocks
        s.execute("INSERT INTO hostel_blocks VALUES (1, 'Kaveri Boys Block', 'BOYS');");
        s.execute("INSERT INTO hostel_blocks VALUES (2, 'Ganga Girls Block', 'GIRLS');");

        // Users: Admin (pass: 123), Warden (pass: 123), Student 1 & 2 (pass: 123)
        s.execute("INSERT INTO users VALUES (1, 'admin', '123', 'ADMIN', 'Dr. Ramanathan');");
        s.execute("INSERT INTO users VALUES (2, 'warden', '123', 'WARDEN', 'Prof. Rajesh Sharma');");
        s.execute("INSERT INTO users VALUES (3, 'rahul', '123', 'STUDENT', 'Rahul Verma');");
        s.execute("INSERT INTO users VALUES (4, 'ananya', '123', 'STUDENT', 'Ananya Iyer');");

        // Wardens & Students
        s.execute("INSERT INTO wardens VALUES (2, 1, 'M.Tech, Ph.D');");
        s.execute("INSERT INTO students VALUES (3, '21CS045', 'MALE', 'CSE', '9876543210');");
        s.execute("INSERT INTO students VALUES (4, '21EC012', 'FEMALE', 'ECE', '9876543211');");

        // Rooms
        s.execute("INSERT INTO rooms VALUES (1, 1, 'K-101', 'STANDARD', 2, 1, 4500.0);");
        s.execute("INSERT INTO rooms VALUES (2, 1, 'K-102', 'DELUXE',   2, 0, 6000.0);");
        s.execute("INSERT INTO rooms VALUES (3, 1, 'K-103', 'AC',       1, 0, 8500.0);");
        s.execute("INSERT INTO rooms VALUES (4, 2, 'G-201', 'STANDARD', 2, 1, 4500.0);");

        // Allocations
        s.execute("INSERT INTO allocations VALUES (1, 3, 1, '2026-07-01', 'ACTIVE');");
        s.execute("INSERT INTO allocations VALUES (2, 4, 4, '2026-07-01', 'ACTIVE');");

        // Fee Payments & Complaints
        s.execute("INSERT INTO fee_payments VALUES (1, 3, 27000.0, 'HOSTEL_FEE', '2026-07-02');");
        s.execute("INSERT INTO complaints VALUES (1, 3, 'PLUMBING', 'Tap leaking in bathroom', 'RESOLVED', 'Plumber fixed washer');");
    }
}
