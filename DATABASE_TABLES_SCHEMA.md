# SMARTHOSTEL: COMPLETE 8-TABLE RELATIONAL DATABASE SCHEMA
## Third Normal Form (3NF) Relational Schema Architecture & Data Dictionary

The SmartHostel relational database (`hostel.db`) is partitioned into **eight normalized tables** in Third Normal Form (3NF). It enforces strict referential integrity (`PRAGMA foreign_keys = ON;`), cascade deletions, unique invariants, and default states.

---

### PART A: MASTER ENTITY TABLES

#### 1. `users` — Master Authentication & Identity Directory
* **Classification:** Core Master Table (Authentication & Security)
* **Description:** Central security store enforcing single-identity authentication across all platform roles (`ADMIN`, `WARDEN`, `STUDENT`).

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `user_id` | `INTEGER` | **PRIMARY KEY AUTOINCREMENT** | Unique internal system identifier |
| `username` | `TEXT` | **UNIQUE, NOT NULL** | Unique login handle (e.g., `admin`, `rahul`) |
| `password` | `TEXT` | **NOT NULL** | Authentication secret / credential |
| `role` | `TEXT` | **NOT NULL** | Security scope: `'ADMIN'`, `'WARDEN'`, `'STUDENT'` |
| `full_name` | `TEXT` | **NOT NULL** | Official legal name of the user |

* **Sample Seed Records:**
  | user_id | username | password | role | full_name |
  | :--- | :--- | :--- | :--- | :--- |
  | `1` | `admin` | `123` | `ADMIN` | `Dr. Ramanathan` |
  | `2` | `warden` | `123` | `WARDEN` | `Prof. Rajesh Sharma` |
  | `3` | `rahul` | `123` | `STUDENT` | `Rahul Verma` |
  | `4` | `ananya` | `123` | `STUDENT` | `Ananya Iyer` |

---

#### 2. `students` — Academic & Demographic Profile
* **Classification:** Master Entity Table (Specialization of `users`)
* **Description:** Represents student resident profiles, linked directly to the parent `users` table via a 1:1 foreign key.

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `student_id` | `INTEGER` | **PRIMARY KEY, FK** $\to$ `users(user_id)` | Linked user account (`ON DELETE CASCADE`) |
| `reg_no` | `TEXT` | **UNIQUE, NOT NULL** | Institutional university register number (e.g., `21CS045`) |
| `gender` | `TEXT` | **NOT NULL** | Gender classification: `'MALE'`, `'FEMALE'` |
| `department` | `TEXT` | **NOT NULL** | Academic branch: `'CSE'`, `'ECE'`, `'IT'`, etc. |
| `phone` | `TEXT` | **NOT NULL** | 10-digit primary emergency contact number |

* **Sample Seed Records:**
  | student_id | reg_no | gender | department | phone |
  | :--- | :--- | :--- | :--- | :--- |
  | `3` | `21CS045` | `MALE` | `CSE` | `9876543210` |
  | `4` | `21EC012` | `FEMALE` | `ECE` | `9876543211` |

---

#### 3. `hostel_blocks` — Residential Facilities Registry
* **Classification:** Master Entity Table (Physical Infrastructure)
* **Description:** Defines physical campus dormitory buildings categorized by institutional gender residence rules.

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `block_id` | `INTEGER` | **PRIMARY KEY AUTOINCREMENT** | Unique campus building identifier |
| `block_name` | `TEXT` | **UNIQUE, NOT NULL** | Building designation (e.g., `'Kaveri Boys Block'`) |
| `gender_type` | `TEXT` | **NOT NULL** | Residence rule: `'BOYS'`, `'GIRLS'` |

* **Sample Seed Records:**
  | block_id | block_name | gender_type |
  | :--- | :--- | :--- |
  | `1` | `Kaveri Boys Block` | `BOYS` |
  | `2` | `Ganga Girls Block` | `GIRLS` |

---

#### 4. `wardens` — Staff Deployments & Block Assignments
* **Classification:** Master Entity Table (Specialization of `users`)
* **Description:** Staff supervisory records linking an authenticated user to the physical residential block they administer.

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `warden_id` | `INTEGER` | **PRIMARY KEY, FK** $\to$ `users(user_id)` | Linked staff credentials (`ON DELETE CASCADE`) |
| `block_id` | `INTEGER` | **FOREIGN KEY** $\to$ `hostel_blocks(block_id)` | Supervised residential building |
| `qualification`| `TEXT` | *Nullable* | Academic degree / professional qualifications |

* **Sample Seed Records:**
  | warden_id | block_id | qualification |
  | :--- | :--- | :--- |
  | `2` | `1` | `M.Tech, Ph.D` |

---

### PART B: TRANSACTIONAL & OPERATIONAL TABLES

#### 5. `rooms` — Physical Room Inventory & Base Tariffs
* **Classification:** Operational Inventory Table
* **Description:** Tracks individual room numbers, capacity limits, live bed occupancy counters, and base rental tariffs.

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `room_id` | `INTEGER` | **PRIMARY KEY AUTOINCREMENT** | Unique physical room identifier |
| `block_id` | `INTEGER` | **FOREIGN KEY** $\to$ `hostel_blocks(block_id)` | Building containing the room |
| `room_no` | `TEXT` | **NOT NULL** | Room label within the block (e.g., `'K-101'`) |
| `room_type` | `TEXT` | **NOT NULL** | Category: `'STANDARD'`, `'DELUXE'`, `'AC'` |
| `capacity` | `INTEGER` | **NOT NULL, > 0** | Maximum bed slots available |
| `beds_occupied`| `INTEGER` | **DEFAULT 0** | Currently occupied bed count |
| `base_rent` | `REAL` | **NOT NULL, $\ge$ 0** | Base monthly rent in ₹ (subject to polymorphic tariff) |
| *Composite* | — | **UNIQUE(`block_id`, `room_no`)** | Prevents duplicate room labels within the same block |

* **Sample Seed Records:**
  | room_id | block_id | room_no | room_type | capacity | beds_occupied | base_rent |
  | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
  | `1` | `1` | `K-101` | `STANDARD` | `2` | `1` | `4500.0` |
  | `2` | `1` | `K-102` | `DELUXE` | `2` | `0` | `6000.0` |
  | `3` | `1` | `K-103` | `AC` | `1` | `0` | `8500.0` |
  | `4` | `2` | `G-201` | `STANDARD` | `2` | `1` | `4500.0` |

---

#### 6. `allocations` — Active & Historical Room Tenancy
* **Classification:** Operational Transaction Table (ACID Controlled)
* **Description:** Records room tenancy assignments. Updated atomically alongside `rooms.beds_occupied` inside explicit JDBC transactions.

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `alloc_id` | `INTEGER` | **PRIMARY KEY AUTOINCREMENT** | Unique room assignment transaction ID |
| `student_id` | `INTEGER` | **FOREIGN KEY** $\to$ `students(student_id)` | Resident student receiving the allocation |
| `room_id` | `INTEGER` | **FOREIGN KEY** $\to$ `rooms(room_id)` | Allocated room |
| `start_date` | `TEXT` | **NOT NULL** | Tenancy commencement date (`YYYY-MM-DD`) |
| `status` | `TEXT` | **DEFAULT 'ACTIVE'** | Tenancy state: `'ACTIVE'`, `'VACATED'` |

* **Sample Seed Records:**
  | alloc_id | student_id | room_id | start_date | status |
  | :--- | :--- | :--- | :--- | :--- |
  | `1` | `3` | `1` | `2026-07-01` | `ACTIVE` |
  | `2` | `4` | `4` | `2026-07-01` | `ACTIVE` |

---

#### 7. `fee_payments` — Financial Ledger & Remittance Receipts
* **Classification:** Financial Ledger Table (Append-Only)
* **Description:** Immutable record of financial transactions remitted by students for hostel accommodation or mess facilities.

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `payment_id` | `INTEGER` | **PRIMARY KEY AUTOINCREMENT** | Unique payment receipt transaction identifier |
| `student_id` | `INTEGER` | **FOREIGN KEY** $\to$ `students(student_id)` | Paying student resident |
| `amount` | `REAL` | **NOT NULL, > 0** | Monetary amount remitted in ₹ |
| `fee_type` | `TEXT` | **NOT NULL** | Remittance category: `'HOSTEL_FEE'`, `'MESS_FEE'` |
| `payment_date`| `TEXT` | **NOT NULL** | Payment timestamp (`YYYY-MM-DD`) |

* **Sample Seed Records:**
  | payment_id | student_id | amount | fee_type | payment_date |
  | :--- | :--- | :--- | :--- | :--- |
  | `1` | `3` | `27000.0` | `HOSTEL_FEE` | `2026-07-02` |

---

#### 8. `complaints` — Maintenance Tickets & Redressal Lifecycle
* **Classification:** Operational Workflow Table
* **Description:** Maintenance tickets lodged by residents, tracking lifecycle progression from ticket creation to technician sign-off.

| Column Name | SQL Data Type | Key / Constraint | Description & Validation Rules |
| :--- | :--- | :--- | :--- |
| `complaint_id`| `INTEGER` | **PRIMARY KEY AUTOINCREMENT** | Unique grievance ticket identifier |
| `student_id` | `INTEGER` | **FOREIGN KEY** $\to$ `students(student_id)` | Resident who lodged the maintenance issue |
| `category` | `TEXT` | **NOT NULL** | Trade: `'PLUMBING'`, `'ELECTRICAL'`, `'CARPENTRY'`, etc. |
| `title` | `TEXT` | **NOT NULL** | Brief problem statement / issue summary |
| `status` | `TEXT` | **DEFAULT 'PENDING'** | Ticket state: `'PENDING'`, `'RESOLVED'` |
| `resolution_notes`| `TEXT` | *Nullable* | Corrective maintenance remarks logged by staff |

* **Sample Seed Records:**
  | complaint_id | student_id | category | title | status | resolution_notes |
  | :--- | :--- | :--- | :--- | :--- | :--- |
  | `1` | `3` | `PLUMBING` | `Tap leaking in bathroom` | `RESOLVED` | `Plumber fixed washer` |

---

### 🔗 Foreign Key Referential Integrity Matrix

| Child Table | Foreign Key Column | Referenced Parent Table | Parent Primary Key | Delete Action | Business Meaning |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `students` | `student_id` | `users` | `user_id` | `ON DELETE CASCADE` | Student is a specialized sub-type of User |
| `wardens` | `warden_id` | `users` | `user_id` | `ON DELETE CASCADE` | Warden is a specialized sub-type of User |
| `wardens` | `block_id` | `hostel_blocks` | `block_id` | `RESTRICT / NO ACTION` | Warden is assigned to supervise a building block |
| `rooms` | `block_id` | `hostel_blocks` | `block_id` | `RESTRICT / NO ACTION` | A room physically exists inside a block |
| `allocations`| `student_id` | `students` | `student_id` | `RESTRICT / NO ACTION` | Allocates an active bed to a resident student |
| `allocations`| `room_id` | `rooms` | `room_id` | `RESTRICT / NO ACTION` | Connects allocation to a physical room inventory |
| `fee_payments`| `student_id` | `students` | `student_id` | `RESTRICT / NO ACTION` | Credits payment against a resident's ledger |
| `complaints` | `student_id` | `students` | `student_id` | `RESTRICT / NO ACTION` | Tracks issue raised by a resident student |
