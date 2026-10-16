-- =====================================================================
-- SMART HOSTEL MANAGEMENT SYSTEM (SHMS)
-- DBMS SCRIPT 03: Realistic Sample Seed Data
-- Passwords: All default accounts use password: 'password123'
-- SHA-256 hash of 'password123' = 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'
-- =====================================================================

USE hostel_db;

-- 1. Insert Hostel Blocks
INSERT INTO hostel_blocks (block_id, block_name, gender_type, total_floors) VALUES
(1, 'Kaveri Boys Hostel', 'BOYS', 4),
(2, 'Ganga Girls Hostel', 'GIRLS', 4);

-- 2. Insert Core Users
INSERT INTO users (user_id, username, password_hash, role, full_name, email, phone, status) VALUES
(1, 'admin', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN', 'Chief Warden / Administrator', 'admin@hostel.edu', '9876543210', 'ACTIVE'),
(2, 'warden_rajesh', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'WARDEN', 'Dr. Rajesh Sharma', 'rajesh.warden@hostel.edu', '9876543211', 'ACTIVE'),
(3, 'warden_priya', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'WARDEN', 'Prof. Priya Menon', 'priya.warden@hostel.edu', '9876543212', 'ACTIVE'),
(4, 'rahul_21cs', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 'Rahul Verma', 'rahul.v@student.edu', '9876543220', 'ACTIVE'),
(5, 'ananya_21ec', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 'Ananya Iyer', 'ananya.i@student.edu', '9876543221', 'ACTIVE'),
(6, 'karthik_22me', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 'Karthik Sundaram', 'karthik.s@student.edu', '9876543222', 'ACTIVE'),
(7, 'sneha_22it', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 'Sneha Patel', 'sneha.p@student.edu', '9876543223', 'ACTIVE');

-- 3. Insert Wardens Details
INSERT INTO wardens (warden_id, block_id, qualification, joining_date) VALUES
(2, 1, 'M.Tech, Ph.D in Computer Science', '2021-06-15'),
(3, 2, 'M.Sc, M.Phil in Electronics', '2022-01-10');

-- 4. Insert Students Details
INSERT INTO students (student_id, reg_no, gender, department, year_of_study, guardian_name, guardian_phone, address) VALUES
(4, '21CS045', 'MALE', 'Computer Science & Engineering', 3, 'Suresh Verma', '9123456780', '124 MG Road, Bangalore'),
(5, '21EC012', 'FEMALE', 'Electronics & Comm.', 3, 'Raman Iyer', '9123456781', '45 Gandhi Nagar, Chennai'),
(6, '22ME089', 'MALE', 'Mechanical Engineering', 2, 'Sundaram Pillai', '9123456782', '89 Anna Salai, Madurai'),
(7, '22IT033', 'FEMALE', 'Information Technology', 2, 'Dinesh Patel', '9123456783', '12 Ring Road, Ahmedabad');

-- 5. Insert Rooms
-- Kaveri Boys Hostel (Block 1)
INSERT INTO rooms (room_id, block_id, room_number, room_type, capacity, beds_occupied, base_rent, status) VALUES
(1, 1, 'K-101', 'STANDARD', 2, 1, 4500.00, 'AVAILABLE'),
(2, 1, 'K-102', 'DELUXE',   2, 1, 6000.00, 'AVAILABLE'),
(3, 1, 'K-103', 'AC',       1, 0, 8500.00, 'AVAILABLE'),
(4, 1, 'K-104', 'STANDARD', 3, 0, 4000.00, 'AVAILABLE'),
-- Ganga Girls Hostel (Block 2)
(5, 2, 'G-201', 'STANDARD', 2, 1, 4500.00, 'AVAILABLE'),
(6, 2, 'G-202', 'DELUXE',   2, 1, 6000.00, 'AVAILABLE'),
(7, 2, 'G-203', 'AC',       1, 0, 8500.00, 'AVAILABLE');

-- 6. Insert Active Allocations
INSERT INTO allocations (allocation_id, student_id, room_id, start_date, status, allocated_by) VALUES
(1, 4, 1, '2026-07-01', 'ACTIVE', 1), -- Rahul in K-101
(2, 6, 2, '2026-07-01', 'ACTIVE', 2), -- Karthik in K-102
(3, 5, 5, '2026-07-01', 'ACTIVE', 1), -- Ananya in G-201
(4, 7, 6, '2026-07-01', 'ACTIVE', 3); -- Sneha in G-202

-- 7. Insert Fee Payments
INSERT INTO fee_payments (payment_id, student_id, amount, fee_type, payment_date, payment_method, transaction_ref, status, remarks) VALUES
(1, 4, 27000.00, 'HOSTEL_FEE', '2026-07-02 10:15:00', 'UPI', 'TXN_UPI_9823412', 'SUCCESS', 'Semester 5 Hostel Rent Paid'),
(2, 4, 15000.00, 'MESS_FEE',   '2026-07-02 10:20:00', 'UPI', 'TXN_UPI_9823415', 'SUCCESS', 'Semester 5 Mess Charges Paid'),
(3, 5, 27000.00, 'HOSTEL_FEE', '2026-07-03 14:30:00', 'BANK_TRANSFER', 'TXN_NEFT_564123', 'SUCCESS', 'Semester 5 Hostel Rent Paid'),
(4, 6, 36000.00, 'HOSTEL_FEE', '2026-07-04 11:00:00', 'ONLINE', 'TXN_CARD_776129', 'SUCCESS', 'Deluxe Room Advance Paid');

-- 8. Insert Complaints
INSERT INTO complaints (complaint_id, student_id, room_id, category, title, description, status, filed_date, resolved_date, resolved_by, resolution_notes) VALUES
(1, 4, 1, 'PLUMBING', 'Tap leaking in bathroom', 'Washroom faucet is dripping constantly in K-101.', 'RESOLVED', '2026-08-01 09:30:00', '2026-08-02 16:00:00', 2, 'Plumber replaced washer valve.'),
(2, 6, 2, 'ELECTRICAL', 'Study lamp socket faulty', 'Power socket near desk 2 sparks when plugged in.', 'IN_PROGRESS', '2026-09-10 18:20:00', NULL, NULL, NULL),
(3, 5, 5, 'CLEANLINESS', 'Corridor dustbin overflow', 'Dustbin near room G-201 needs regular emptying.', 'PENDING', '2026-10-12 11:45:00', NULL, NULL, NULL);

-- 9. Insert Visitor Logs
INSERT INTO visitor_logs (log_id, student_id, visitor_name, relation, phone, id_proof_type, check_in, check_out, purpose) VALUES
(1, 4, 'Suresh Verma', 'Father', '9123456780', 'Aadhaar Card', '2026-09-15 10:00:00', '2026-09-15 12:30:00', 'Semester visit & fees receipt collection'),
(2, 5, 'Raman Iyer', 'Father', '9123456781', 'Voter ID', '2026-09-20 14:00:00', '2026-09-20 16:00:00', 'Family visit');
