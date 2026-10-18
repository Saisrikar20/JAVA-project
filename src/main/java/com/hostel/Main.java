package com.hostel;

import java.util.Scanner;

/**
 * Smart Hostel Management System - CLI Application Entry Point
 * Demonstrates the collective integration of 4 Modules and 8 Database Tables.
 */
public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final StudentModule studentMod = new StudentModule();
    private static final RoomModule roomMod = new RoomModule();
    private static final AllocationModule allocMod = new AllocationModule();
    private static final BillingAndComplaintModule billMod = new BillingAndComplaintModule();

    public static void main(String[] args) {
        // Initialize the 8 relational tables
        Database.getConnection();

        // Start built-in Web Server on port 8080
        WebServer.start(8080);

        while (true) {
            System.out.println("\n========================================================");
            System.out.println("       SMART HOSTEL MANAGEMENT SYSTEM");
            System.out.println("   Integrated OOP & DBMS Project (4 Members, 8 Tables)");
            System.out.println("========================================================");
            System.out.println(" 🌐 Web Application: http://localhost:8080 (Open in Browser)");
            System.out.println(" 💻 Terminal CLI Options:");
            System.out.println("   1. Login to Terminal CLI (Admin / Warden / Student)");
            System.out.println("   2. View Project Viva Guide (Concepts & 8 Tables Architecture)");
            System.out.println("   3. Exit");
            System.out.print("Select an option (1-3): ");

            int choice = readInt(1, 3);
            if (choice == 1) {
                handleLogin();
            } else if (choice == 2) {
                showVivaGuide();
            } else {
                System.out.println("Exiting application. Goodbye!");
                WebServer.stop();
                break;
            }
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- LOGIN PORTAL (Default password for all accounts: 123) ---");
        System.out.print("Enter Username (e.g. admin, warden, rahul, ananya): ");
        String username = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        User user = studentMod.login(username, password);
        if (user == null) {
            System.out.println("Invalid credentials. Try again.");
            return;
        }

        System.out.println("\nWelcome, " + user.getFullName() + "! Role: " + user.getRole());
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            adminMenu();
        } else if ("WARDEN".equalsIgnoreCase(user.getRole())) {
            wardenMenu();
        } else {
            studentMenu(user.getUserId());
        }
    }

    private static void adminMenu() {
        while (true) {
            System.out.println("\n--- ADMINISTRATOR MENU ---");
            System.out.println("1. View All Students (Member 1)");
            System.out.println("2. Register New Student (Member 1)");
            System.out.println("3. View All Rooms & Tariffs (Member 2)");
            System.out.println("4. Add New Room (Member 2)");
            System.out.println("5. View Active Allocations (Member 3)");
            System.out.println("6. View Fee Audit & Collections (Member 4)");
            System.out.println("7. View All Complaints (Member 4)");
            System.out.println("8. Logout");
            System.out.print("Choice: ");
            int c = readInt(1, 8);
            if (c == 1) studentMod.displayAllStudents();
            else if (c == 2) {
                System.out.print("Username: "); String u = sc.nextLine();
                System.out.print("Password: "); String p = sc.nextLine();
                System.out.print("Full Name: "); String fn = sc.nextLine();
                System.out.print("Reg No: "); String reg = sc.nextLine();
                System.out.print("Gender (MALE/FEMALE): "); String g = sc.nextLine();
                System.out.print("Department: "); String d = sc.nextLine();
                System.out.print("Phone: "); String ph = sc.nextLine();
                if (studentMod.registerStudent(u, p, fn, reg, g, d, ph)) {
                    System.out.println("Student registered successfully!");
                }
            } else if (c == 3) roomMod.displayAllRooms();
            else if (c == 4) {
                System.out.print("Block ID (1-Kaveri, 2-Ganga): "); int bId = readInt(1, 10);
                System.out.print("Room No: "); String rNo = sc.nextLine();
                System.out.print("Type (STANDARD / DELUXE / AC): "); String type = sc.nextLine();
                System.out.print("Capacity: "); int cap = readInt(1, 6);
                System.out.print("Base Rent: "); double rent = Double.parseDouble(sc.nextLine());
                if (roomMod.addRoom(bId, rNo, type, cap, rent)) {
                    System.out.println("Room added successfully!");
                }
            } else if (c == 5) allocMod.displayAllocations();
            else if (c == 6) billMod.displayFinancialAudit();
            else if (c == 7) billMod.displayComplaints();
            else break;
        }
    }

    private static void wardenMenu() {
        while (true) {
            System.out.println("\n--- WARDEN MENU ---");
            System.out.println("1. View Rooms & Vacancies (Member 2)");
            System.out.println("2. Search Rooms - Overloading Demo (Member 2)");
            System.out.println("3. Allocate Room to Student - ACID Transaction (Member 3)");
            System.out.println("4. Vacate Student from Room - ACID Transaction (Member 3)");
            System.out.println("5. View All Active Allocations (Member 3)");
            System.out.println("6. Resolve Complaint (Member 4)");
            System.out.println("7. Logout");
            System.out.print("Choice: ");
            int c = readInt(1, 7);
            if (c == 1) roomMod.displayAllRooms();
            else if (c == 2) {
                System.out.println("Search by: 1. Min Capacity (int) | 2. Room Type (String)");
                int mode = readInt(1, 2);
                if (mode == 1) {
                    System.out.print("Enter capacity: ");
                    roomMod.searchRooms(readInt(1, 10));
                } else {
                    System.out.print("Enter type (STANDARD/DELUXE/AC): ");
                    roomMod.searchRooms(sc.nextLine());
                }
            } else if (c == 3) {
                System.out.print("Enter Student ID: "); int sId = readInt(1, 1000);
                System.out.print("Enter Room ID: "); int rId = readInt(1, 1000);
                if (allocMod.allocateRoom(sId, rId)) System.out.println("Room allocated successfully!");
            } else if (c == 4) {
                System.out.print("Enter Student ID to vacate: "); int sId = readInt(1, 1000);
                if (allocMod.vacateRoom(sId)) System.out.println("Student vacated successfully!");
            } else if (c == 5) allocMod.displayAllocations();
            else if (c == 6) {
                System.out.print("Enter Complaint ID: "); int cId = readInt(1, 1000);
                System.out.print("Enter Resolution Remarks: "); String notes = sc.nextLine();
                if (billMod.resolveComplaint(cId, notes)) System.out.println("Complaint resolved!");
            } else break;
        }
    }

    private static void studentMenu(int studentId) {
        Student s = studentMod.getStudent(studentId);
        while (true) {
            System.out.println("\n--- STUDENT PORTAL (" + s.getFullName() + ") ---");
            System.out.println("1. View Profile & Reg No (Member 1)");
            System.out.println("2. View Semester Fee Bill & Dues (Member 4 + Member 2 Polymorphism)");
            System.out.println("3. Pay Fee (Member 4)");
            System.out.println("4. Lodge Complaint (Member 4)");
            System.out.println("5. Logout");
            System.out.print("Choice: ");
            int c = readInt(1, 5);
            if (c == 1) {
                System.out.printf("Name: %s | Reg: %s | Dept: %s | Phone: %s\n",
                        s.getFullName(), s.getRegNo(), s.getDepartment(), s.getPhone());
            } else if (c == 2) {
                double total = billMod.calculateTotalSemesterBill(studentId);
                double paid = billMod.getTotalPaid(studentId);
                System.out.printf("Total Semester Fee: ₹%.2f | Amount Paid: ₹%.2f | Outstanding Due: ₹%.2f\n",
                        total, paid, Math.max(0, total - paid));
            } else if (c == 3) {
                System.out.print("Enter Amount to Pay: "); double amt = Double.parseDouble(sc.nextLine());
                if (billMod.recordPayment(studentId, amt, "HOSTEL_FEE")) {
                    System.out.println("Payment recorded successfully!");
                }
            } else if (c == 4) {
                System.out.print("Category (PLUMBING/ELECTRICAL/CLEANLINESS): "); String cat = sc.nextLine();
                System.out.print("Title / Problem: "); String title = sc.nextLine();
                if (billMod.lodgeComplaint(studentId, cat, title)) {
                    System.out.println("Complaint registered successfully!");
                }
            } else break;
        }
    }

    private static void showVivaGuide() {
        System.out.println("\n========================================================");
        System.out.println("      PROJECT VIVA GUIDE & 8 TABLES ARCHITECTURE");
        System.out.println("========================================================");
        System.out.println("Member 1 (StudentModule.java):");
        System.out.println("  • Tables: 1. users, 2. students");
        System.out.println("  • OOP: User -> Student Inheritance, Encapsulation");
        System.out.println("  • DBMS: Student Registration & Authentication with PreparedStatement\n");
        System.out.println("Member 2 (RoomModule.java):");
        System.out.println("  • Tables: 3. hostel_blocks, 4. rooms");
        System.out.println("  • OOP: Abstract Room -> Standard, Deluxe, AC (Polymorphic Rent)");
        System.out.println("         Method Overloading: searchRooms(int) vs searchRooms(String)");
        System.out.println("  • DBMS: Capacity constraints and Room inventory\n");
        System.out.println("Member 3 (AllocationModule.java):");
        System.out.println("  • Tables: 5. wardens, 6. allocations");
        System.out.println("  • OOP: Allocation business logic");
        System.out.println("  • DBMS: ACID Transactions (setAutoCommit(false), commit/rollback)\n");
        System.out.println("Member 4 (BillingAndComplaintModule.java):");
        System.out.println("  • Tables: 7. fee_payments, 8. complaints");
        System.out.println("  • OOP: Integration with polymorphic room rates");
        System.out.println("  • DBMS: Aggregations SUM(amount), complaint lifecycle tracking");
        System.out.println("========================================================");
    }

    private static int readInt(int min, int max) {
        while (true) {
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.print("Enter between " + min + " and " + max + ": ");
            } catch (Exception e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }
}
