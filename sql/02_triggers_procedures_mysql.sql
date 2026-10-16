-- =====================================================================
-- SMART HOSTEL MANAGEMENT SYSTEM (SHMS)
-- DBMS SCRIPT 02: Triggers, Stored Procedures & Analytical Views
-- =====================================================================

USE hostel_db;

DELIMITER $$

-- ---------------------------------------------------------------------
-- TRIGGER 1: trg_after_allocation_insert
-- Automatically increments room's occupied bed count when a new allocation is made.
-- If beds_occupied reaches capacity, updates status to 'OCCUPIED'.
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_after_allocation_insert$$
CREATE TRIGGER trg_after_allocation_insert
AFTER INSERT ON allocations
FOR EACH ROW
BEGIN
    IF NEW.status = 'ACTIVE' THEN
        UPDATE rooms
        SET beds_occupied = beds_occupied + 1,
            status = CASE 
                        WHEN beds_occupied + 1 >= capacity THEN 'OCCUPIED'
                        ELSE 'AVAILABLE'
                     END
        WHERE room_id = NEW.room_id;
    END IF;
END$$

-- ---------------------------------------------------------------------
-- TRIGGER 2: trg_after_allocation_update
-- Automatically decrements room's occupied bed count when an allocation is vacated.
-- Sets room status back to 'AVAILABLE'.
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_after_allocation_update$$
CREATE TRIGGER trg_after_allocation_update
AFTER UPDATE ON allocations
FOR EACH ROW
BEGIN
    IF OLD.status = 'ACTIVE' AND NEW.status = 'VACATED' THEN
        UPDATE rooms
        SET beds_occupied = GREATEST(0, beds_occupied - 1),
            status = 'AVAILABLE'
        WHERE room_id = NEW.room_id;
    END IF;
END$$

-- ---------------------------------------------------------------------
-- STORED PROCEDURE: sp_AllocateStudentRoom
-- Encapsulates business validation, concurrency check, and allocation inside a transaction.
-- Returns result code: 0 = Success, 1 = Student already allocated, 2 = Room full, 3 = Error
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_AllocateStudentRoom$$
CREATE PROCEDURE sp_AllocateStudentRoom(
    IN p_student_id INT,
    IN p_room_id INT,
    IN p_allocated_by INT,
    OUT p_result_code INT,
    OUT p_message VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_active_allocations INT DEFAULT 0;
    DECLARE v_capacity INT DEFAULT 0;
    DECLARE v_occupied INT DEFAULT 0;
    DECLARE v_room_status VARCHAR(20);

    -- Error handler for database transaction
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = 3;
        SET p_message = 'Database error occurred during room allocation.';
    END;

    START TRANSACTION;

    -- 1. Check if student already has an active allocation
    SELECT COUNT(*) INTO v_active_allocations
    FROM allocations
    WHERE student_id = p_student_id AND status = 'ACTIVE';

    IF v_active_allocations > 0 THEN
        ROLLBACK;
        SET p_result_code = 1;
        SET p_message = 'Student already has an active room allocation.';
        LEAVE proc_label;
    END IF;

    -- 2. Lock room row and check vacancy
    SELECT capacity, beds_occupied, status INTO v_capacity, v_occupied, v_room_status
    FROM rooms
    WHERE room_id = p_room_id
    FOR UPDATE;

    IF v_room_status = 'MAINTENANCE' OR v_occupied >= v_capacity THEN
        ROLLBACK;
        SET p_result_code = 2;
        SET p_message = 'Room has no available beds or is under maintenance.';
        LEAVE proc_label;
    END IF;

    -- 3. Insert allocation record (Trigger will update bed occupancy)
    INSERT INTO allocations (student_id, room_id, start_date, status, allocated_by)
    VALUES (p_student_id, p_room_id, CURDATE(), 'ACTIVE', p_allocated_by);

    COMMIT;
    SET p_result_code = 0;
    SET p_message = 'Room successfully allocated to student.';
END$$

DELIMITER ;

-- =====================================================================
-- ANALYTICAL VIEWS
-- =====================================================================

-- VIEW 1: Complete Student Occupancy & Room Details
CREATE OR REPLACE VIEW vw_student_hostel_details AS
SELECT 
    s.student_id,
    s.reg_no,
    u.full_name,
    u.email,
    u.phone,
    s.department,
    s.year_of_study,
    b.block_name,
    r.room_number,
    r.room_type,
    r.base_rent,
    a.allocation_id,
    a.start_date,
    a.status AS allocation_status
FROM students s
JOIN users u ON s.student_id = u.user_id
LEFT JOIN allocations a ON s.student_id = a.student_id AND a.status = 'ACTIVE'
LEFT JOIN rooms r ON a.room_id = r.room_id
LEFT JOIN hostel_blocks b ON r.block_id = b.block_id;

-- VIEW 2: Room Occupancy Statistics per Block
CREATE OR REPLACE VIEW vw_room_occupancy_summary AS
SELECT 
    b.block_name,
    b.gender_type,
    COUNT(r.room_id) AS total_rooms,
    SUM(r.capacity) AS total_bed_capacity,
    SUM(r.beds_occupied) AS total_beds_occupied,
    SUM(r.capacity - r.beds_occupied) AS total_beds_available,
    ROUND((SUM(r.beds_occupied) * 100.0) / NULLIF(SUM(r.capacity), 0), 2) AS occupancy_percentage
FROM hostel_blocks b
LEFT JOIN rooms r ON b.block_id = r.block_id
GROUP BY b.block_id, b.block_name, b.gender_type;

-- VIEW 3: Pending Complaints Overview
CREATE OR REPLACE VIEW vw_pending_complaints_by_block AS
SELECT 
    c.complaint_id,
    c.category,
    c.title,
    c.filed_date,
    c.status,
    u.full_name AS student_name,
    s.reg_no,
    r.room_number,
    b.block_name
FROM complaints c
JOIN students s ON c.student_id = s.student_id
JOIN users u ON s.student_id = u.user_id
JOIN rooms r ON c.room_id = r.room_id
JOIN hostel_blocks b ON r.block_id = b.block_id
WHERE c.status IN ('PENDING', 'IN_PROGRESS');
