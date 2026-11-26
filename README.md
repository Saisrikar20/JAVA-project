# Smart Hostel Management System (SHMS)
**Integrated OOP & DBMS Mini-Project (CLI Only)**

---

## 📌 Project Overview
* **Project Title:** SmartHostel: An Integrated Object-Oriented Hostel Administration and Room Allocation System with Relational Database Management
* **Team Size:** 4 Members
* **Architecture:** 4 Distinct Modules (1 File Per Member) & 8 Relational Tables
* **Total Size:** ~850 Lines of Clean, Human-Readable Code (No bloat, completely believable for viva)

---

## 🚀 How to Run the Project

### Option A: From Terminal
1. **Compile:**
   ```bash
   javac -d bin -cp "lib/*" src/main/java/com/hostel/*.java
   ```
2. **Run:**
   ```bash
   java -cp "bin;lib/*" com.hostel.Main
   ```

### Option B: From IntelliJ IDEA / VS Code
1. Open `g:\java project` in your IDE.
2. Ensure the JAR files inside the `lib/` folder are added as libraries (Project Structure $\to$ Libraries $\to$ Add `lib/`).
3. Open `src/main/java/com/hostel/Main.java` and click the green **Run ▶** button.

### 3. Demo Login Credentials (Password for all accounts is `123`)
* **Admin:** `admin` / `123`
* **Warden:** `warden` / `123`
* **Students:** `rahul` / `123` or `ananya` / `123`

---

## 🗄️ The 8 Relational Database Tables

| Table # | Table Name | Purpose / Columns | Owned By |
| :---: | :--- | :--- | :---: |
| **1** | `users` | User credentials, roles (ADMIN, WARDEN, STUDENT), full name | **Member 1** |
| **2** | `students` | University reg no, gender, department, phone | **Member 1** |
| **3** | `hostel_blocks` | Block ID, block name, gender type (BOYS/GIRLS) | **Member 2** |
| **4** | `rooms` | Block ID, room no, room type (STANDARD/DELUXE/AC), capacity, beds occupied, base rent | **Member 2** |
| **5** | `wardens` | Warden ID, assigned block, qualification | **Member 3** |
| **6** | `allocations` | Student ID, room ID, start date, status (ACTIVE/VACATED) | **Member 3** |
| **7** | `fee_payments` | Student ID, amount, fee type, payment date | **Member 4** |
| **8** | `complaints` | Student ID, category, title, status (PENDING/RESOLVED), resolution notes | **Member 4** |

---

## 👥 4-Member Responsibility & Viva Guide

### 👤 Member 1: User Identity & Student Module ([`StudentModule.java`](src/main/java/com/hostel/StudentModule.java) - 147 lines)
* **Tables Owned:** `users` (Table 1) and `students` (Table 2)
* **OOP Concepts to Explain:**
  * **Inheritance:** `class Student extends User` (`User` has common fields like `userId`, `username`, `password`, while `Student` adds `regNo`, `department`, `gender`).
  * **Encapsulation:** Private attributes with getter methods.
* **DBMS Concepts to Explain:**
  * Multi-table `INSERT` using `PreparedStatement` to maintain referential integrity.
  * Student login and lookup queries using `WHERE username = ? AND password = ?`.

---

### 👤 Member 2: Infrastructure & Tariffs ([`RoomModule.java`](src/main/java/com/hostel/RoomModule.java) - 152 lines)
* **Tables Owned:** `hostel_blocks` (Table 3) and `rooms` (Table 4)
* **OOP Concepts to Explain:**
  * **Abstraction & Polymorphism:** Abstract class `Room` with polymorphic method `calculateRent()`.
    * `StandardRoom` returns base rent.
    * `DeluxeRoom` overrides to return `baseRent * 1.15` (15% surcharge).
    * `ACRoom` overrides to return `baseRent * 1.35` (35% power tariff).
  * **Method Overloading (Compile-Time Polymorphism):**
    * `searchRooms(int minCapacity)` vs `searchRooms(String roomType)`.
* **DBMS Concepts to Explain:**
  * Room capacity constraints (`CHECK (beds_occupied <= capacity)`).
  * Queries filtering available vacant rooms.

---

### 👤 Member 3: Room Allocations & Transactions ([`AllocationModule.java`](src/main/java/com/hostel/AllocationModule.java) - 135 lines)
* **Tables Owned:** `wardens` (Table 5) and `allocations` (Table 6)
* **OOP Concepts to Explain:**
  * **Composition:** Associating `Student` objects with `Room` objects inside `allocations`.
* **DBMS Concepts to Explain:**
  * **ACID Transaction Management:**
    ```java
    conn.setAutoCommit(false); // Begin transaction
    // 1. Verify student doesn't already have an active room
    // 2. Verify room has vacancy (beds_occupied < capacity)
    // 3. Insert into allocations table
    // 4. Increment beds_occupied in rooms table
    conn.commit(); // Commit on success, or conn.rollback() on exception
    ```
  * Vacate/checkout transaction reversing bed occupancy.

---

### 👤 Member 4: Fee Billing & Grievance Redressal ([`BillingAndComplaintModule.java`](src/main/java/com/hostel/BillingAndComplaintModule.java) - 135 lines)
* **Tables Owned:** `fee_payments` (Table 7) and `complaints` (Table 8)
* **OOP Concepts to Explain:**
  * Dynamic rate calculation: Invokes Member 2's polymorphic `room.calculateRent() * 6` to compute semester dues.
  * Complaint ticket state machine (`PENDING` $\to$ `RESOLVED`).
* **DBMS Concepts to Explain:**
  * SQL Aggregations: `SELECT SUM(amount) FROM fee_payments WHERE student_id = ?` to calculate total paid and outstanding balance.
  * Audit logging and grievance status updates with resolution remarks.

---

## 📁 Source File Tree
```
g:/java project/
├── src/main/java/com/hostel/        # Java Domain & Service Layer
│   ├── Database.java                # Shared SQLite/MySQL Connection & 8 Tables Setup
│   ├── StudentModule.java           # Member 1's Code (Tables 1 & 2)
│   ├── RoomModule.java              # Member 2's Code (Tables 3 & 4)
│   ├── AllocationModule.java        # Member 3's Code (Tables 5 & 6)
│   ├── BillingAndComplaintModule.java # Member 4's Code (Tables 7 & 8)
│   ├── WebServer.java               # Embedded HTTP Web Server & REST API
│   └── Main.java                    # Terminal CLI Coordinator Menu
├── src/main/resources/web/          # Modern Single Page Web Dashboard (index.html)
├── docs/                            # Documentation, Architecture & Viva Resources
│   ├── PRESENTATION_20_SLIDES.md    # Complete 20-Slide Deck for Viva / Defense
│   ├── SYSTEM_ARCHITECTURE.md       # Spaced Multi-Tier Corporate Architecture Diagram
│   ├── DATABASE_TABLES_SCHEMA.md    # 8-Table 3NF Data Dictionary & Relational Matrix
│   ├── PROJECT_REPORT.md            # Comprehensive Phase I Academic Project Report
│   ├── SmartHostel_Project_Report.docx # Formatted Word Document Report
│   └── SmartHostel_Project_Report.pdf  # Final Exported PDF Report
├── diagrams/                        # Architecture, ER & UML Diagram Images
├── lib/                             # JDBC SQLite & Web Driver JARs
├── sql/                             # SQL DDL Scripts
└── hostel.db                        # SQLite Database Engine
```

---

## 📚 Project Documentation & Viva Resources (in `docs/`)
* **[20-Slide Viva Deck](docs/PRESENTATION_20_SLIDES.md):** Complete slide copy, talking points, test outcomes, and theoretical inferences.
* **[System Architecture](docs/SYSTEM_ARCHITECTURE.md):** High-contrast, clean 4-tier corporate architecture flowchart in Mermaid.
* **[8-Table Relational Schema](docs/DATABASE_TABLES_SCHEMA.md):** 3NF specifications for all 8 tables, data dictionary, check constraints, and foreign key cascades.
* **[Full Phase I Project Report](docs/PROJECT_REPORT.md):** Complete bonafide certificate, PO/CO mapping, normalization proofs, and literature survey.

