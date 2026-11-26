# SMARTHOSTEL: CLEAN ENTERPRISE SYSTEM ARCHITECTURE
## Spacious 4-Tier Architectural Topology & Subsystem Interaction Model

---

### 1. Spaced & High-Contrast System Architecture Diagram (Mermaid)

```mermaid
flowchart TD
    %% Corporate Styling: High-Contrast Obsidian, Slate, Platinum, Pure White
    classDef clientBox fill:#1E293B,stroke:#94A3B8,stroke-width:2px,color:#FFFFFF;
    classDef gatewayBox fill:#0F172A,stroke:#CBD5E1,stroke-width:2px,color:#FFFFFF;
    classDef moduleBox fill:#1E293B,stroke:#CBD5E1,stroke-width:2px,color:#FFFFFF;
    classDef persistBox fill:#0F172A,stroke:#94A3B8,stroke-width:2px,color:#FFFFFF;

    subgraph LAYER1["LAYER 1: CLIENT PRESENTATION INTERFACES"]
        direction LR
        C_ADMIN["Administrator Web Console"]
        C_WARDEN["Warden Supervisory Portal"]
        C_STUDENT["Student Resident Portal"]
        C_CLI["Administrative Terminal CLI"]
    end

    subgraph LAYER2["LAYER 2: API GATEWAY & SECURITY PERIMETER"]
        direction LR
        GATEWAY["Embedded Java HTTP Server (Port 8080) & REST API Router"]
        SECURITY["Role-Based Access Control (RBAC) & SQL Injection Defense"]
    end

    subgraph LAYER3["LAYER 3: CORE APPLICATION DOMAIN SERVICES"]
        direction LR
        M_USER["Identity & Enrollment: User-Student Inheritance Hierarchy"]
        M_ROOM["Room Inventory: Polymorphic Tariff Computation Engine"]
        M_ALLOC["Room Allocations: Atomic ACID Booking & Rollback Manager"]
        M_BILL["Finance & Grievances: Dynamic Ledger & Ticket State Machine"]
    end

    subgraph LAYER4["LAYER 4: DATA ACCESS & 3NF PERSISTENCE"]
        direction LR
        DAO["DAO Repository Layer & Parameterized PreparedStatement Engine"]
        DB[("Relational Database: hostel.db (8 Normalized Tables in 3NF)")]
    end

    %% Clean, Spacious Vertical Dataflow
    C_ADMIN --> GATEWAY
    C_WARDEN --> GATEWAY
    C_STUDENT --> GATEWAY
    C_CLI --> GATEWAY

    GATEWAY --> SECURITY
    SECURITY --> M_USER
    SECURITY --> M_ROOM
    SECURITY --> M_ALLOC
    SECURITY --> M_BILL

    M_USER --> DAO
    M_ROOM --> DAO
    M_ALLOC --> DAO
    M_BILL --> DAO

    DAO --> DB

    class C_ADMIN,C_WARDEN,C_STUDENT,C_CLI clientBox;
    class GATEWAY,SECURITY gatewayBox;
    class M_USER,M_ROOM,M_ALLOC,M_BILL moduleBox;
    class DAO,DB persistBox;
```

---

### 2. Tier Breakdown for Slide & Viva

* **Layer 1: Client Presentation Interfaces:** Tailored access points for all user roles (Administrator, Warden, and Student) across responsive Single Page Web Dashboards and a headless Terminal CLI.
* **Layer 2: API Gateway & Security Perimeter:** Centralized ingress running on an embedded Java HTTP server (Port 8080) providing REST routing, RBAC authorization, and parameterized SQL injection prevention.
* **Layer 3: Core Application Domain Services:** The core Java business tier encapsulating object-oriented design: class inheritance (`User` $\to$ `Student`), dynamic method overriding for room tariffs, atomic ACID transaction boundaries with rollback guarantees, dynamic payment ledger aggregations, and complaint lifecycles.
* **Layer 4: Data Access & 3NF Persistence:** Data Access Object (DAO) pattern using JDBC `PreparedStatement` to ensure data integrity across an 8-table Third Normal Form (3NF) relational database.
