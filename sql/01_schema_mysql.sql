-- =====================================================================
-- SMART HOSTEL MANAGEMENT SYSTEM (SHMS)
-- DBMS SCRIPT 01: Relational Schema DDL (Normalized to 3NF)
-- Compatible with MySQL 8.0+
-- =====================================================================

DROP DATABASE IF EXISTS hostel_db;
CREATE DATABASE hostel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hostel_db;

-- 1. Hostel Blocks Table (Gender segregated residential blocks)
CREATE TABLE hostel_blocks (
    block_id INT AUTO_INCREMENT PRIMARY KEY,
    block_name VARCHAR(50) NOT NULL UNIQUE,
    gender_type ENUM('BOYS', 'GIRLS', 'COED') NOT NULL DEFAULT 'BOYS',
    total_floors INT NOT NULL CHECK (total_floors > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Core Users Table (Inheritance root in DB)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'WARDEN', 'STUDENT') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Students Table (Extends Users with 1:1 relation)
CREATE TABLE students (
    student_id INT PRIMARY KEY,
    reg_no VARCHAR(30) NOT NULL UNIQUE,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    department VARCHAR(50) NOT NULL,
    year_of_study INT NOT NULL CHECK (year_of_study BETWEEN 1 AND 5),
    guardian_name VARCHAR(100) NOT NULL,
    guardian_phone VARCHAR(15) NOT NULL,
    address TEXT,
    CONSTRAINT fk_students_user FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Wardens Table (Extends Users with 1:1 relation, linked to a Block)
CREATE TABLE wardens (
    warden_id INT PRIMARY KEY,
    block_id INT NULL,
    qualification VARCHAR(100),
    joining_date DATE NOT NULL,
    CONSTRAINT fk_wardens_user FOREIGN KEY (warden_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_wardens_block FOREIGN KEY (block_id) REFERENCES hostel_blocks(block_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. Rooms Table (Polymorphic types: STANDARD, DELUXE, AC)
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    block_id INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type ENUM('STANDARD', 'DELUXE', 'AC') NOT NULL DEFAULT 'STANDARD',
    capacity INT NOT NULL CHECK (capacity > 0),
    beds_occupied INT NOT NULL DEFAULT 0 CHECK (beds_occupied >= 0),
    base_rent DECIMAL(10, 2) NOT NULL CHECK (base_rent >= 0),
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT uq_block_room UNIQUE (block_id, room_number),
    CONSTRAINT fk_rooms_block FOREIGN KEY (block_id) REFERENCES hostel_blocks(block_id) ON DELETE RESTRICT,
    CONSTRAINT chk_beds_capacity CHECK (beds_occupied <= capacity)
) ENGINE=InnoDB;

-- 6. Room Allocations Table (M:N relationship resolved between Students & Rooms)
CREATE TABLE allocations (
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    room_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    status ENUM('ACTIVE', 'VACATED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    allocated_by INT NULL,
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alloc_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_admin FOREIGN KEY (allocated_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. Fee Payments Table (Tracks rent, deposits, and mess charges)
CREATE TABLE fee_payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    fee_type ENUM('HOSTEL_FEE', 'MESS_FEE', 'SECURITY_DEPOSIT', 'FINE') NOT NULL DEFAULT 'HOSTEL_FEE',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('ONLINE', 'CASH', 'UPI', 'BANK_TRANSFER') NOT NULL DEFAULT 'UPI',
    transaction_ref VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('SUCCESS', 'PENDING', 'FAILED') NOT NULL DEFAULT 'SUCCESS',
    remarks VARCHAR(255),
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 8. Complaints Table (Grievance redressal tracking)
CREATE TABLE complaints (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    room_id INT NOT NULL,
    category ENUM('PLUMBING', 'ELECTRICAL', 'FURNITURE', 'CLEANLINESS', 'MESS', 'OTHER') NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    filed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_date TIMESTAMP NULL,
    resolved_by INT NULL,
    resolution_notes TEXT,
    CONSTRAINT fk_comp_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_comp_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
    CONSTRAINT fk_comp_warden FOREIGN KEY (resolved_by) REFERENCES wardens(warden_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 9. Visitor Logs Table (Security and access tracking)
CREATE TABLE visitor_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    visitor_name VARCHAR(100) NOT NULL,
    relation VARCHAR(50) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    id_proof_type VARCHAR(50) NOT NULL,
    check_in TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    check_out TIMESTAMP NULL,
    purpose VARCHAR(255),
    CONSTRAINT fk_vis_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Indexes for performance optimization on frequent foreign key searches
CREATE INDEX idx_rooms_block ON rooms(block_id);
CREATE INDEX idx_alloc_student ON allocations(student_id, status);
CREATE INDEX idx_alloc_room ON allocations(room_id, status);
CREATE INDEX idx_fee_student ON fee_payments(student_id);
CREATE INDEX idx_comp_status ON complaints(status);
