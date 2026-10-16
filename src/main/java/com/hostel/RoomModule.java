package com.hostel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * =====================================================================
 * MEMBER 2 MODULE: Hostel Infrastructure & Room Tariffs
 * Assigned Tables: 3. hostel_blocks | 4. rooms
 * OOP Concepts:    Abstraction (abstract Room), Polymorphism (Overriding calculateRent()),
 *                  Method Overloading (searchRooms(int) vs searchRooms(String))
 * DBMS Concepts:   Relational Integrity, Capacity Constraints
 * =====================================================================
 */

// --- OOP ABSTRACTION: Abstract Base Room ---
abstract class Room {
    private int roomId;
    private int blockId;
    private String roomNo;
    private String roomType;
    private int capacity;
    private int bedsOccupied;
    private double baseRent;

    public Room(int roomId, int blockId, String roomNo, String roomType,
                int capacity, int bedsOccupied, double baseRent) {
        this.roomId = roomId;
        this.blockId = blockId;
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.capacity = capacity;
        this.bedsOccupied = bedsOccupied;
        this.baseRent = baseRent;
    }

    // Encapsulation
    public int getRoomId() { return roomId; }
    public String getRoomNo() { return roomNo; }
    public String getRoomType() { return roomType; }
    public int getCapacity() { return capacity; }
    public int getBedsOccupied() { return bedsOccupied; }
    public double getBaseRent() { return baseRent; }
    public boolean hasVacancy() { return bedsOccupied < capacity; }

    // OOP POLYMORPHISM: Abstract method overridden by subclasses
    public abstract double calculateRent();
}

// --- OOP POLYMORPHISM: Concrete Room 1 ---
class StandardRoom extends Room {
    public StandardRoom(int id, int bId, String no, int cap, int occ, double rent) {
        super(id, bId, no, "STANDARD", cap, occ, rent);
    }
    @Override
    public double calculateRent() {
        return getBaseRent(); // Standard base rate
    }
}

// --- OOP POLYMORPHISM: Concrete Room 2 ---
class DeluxeRoom extends Room {
    public DeluxeRoom(int id, int bId, String no, int cap, int occ, double rent) {
        super(id, bId, no, "DELUXE", cap, occ, rent);
    }
    @Override
    public double calculateRent() {
        return getBaseRent() * 1.15; // 15% deluxe amenity surcharge
    }
}

// --- OOP POLYMORPHISM: Concrete Room 3 ---
class ACRoom extends Room {
    public ACRoom(int id, int bId, String no, int cap, int occ, double rent) {
        super(id, bId, no, "AC", cap, occ, rent);
    }
    @Override
    public double calculateRent() {
        return getBaseRent() * 1.35; // 35% HVAC cooling tariff
    }
}

public class RoomModule {
    private final Connection conn;

    public RoomModule() {
        this.conn = Database.getConnection();
    }

    // Factory method for creating polymorphic rooms
    public Room createRoomInstance(int id, int bId, String no, String type, int cap, int occ, double rent) {
        if ("DELUXE".equalsIgnoreCase(type)) return new DeluxeRoom(id, bId, no, cap, occ, rent);
        if ("AC".equalsIgnoreCase(type))     return new ACRoom(id, bId, no, cap, occ, rent);
        return new StandardRoom(id, bId, no, cap, occ, rent);
    }

    // DBMS: View all rooms with polymorphic monthly rent calculation
    public void displayAllRooms() {
        String sql = "SELECT r.*, b.block_name FROM rooms r JOIN hostel_blocks b ON r.block_id = b.block_id";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            System.out.printf("%-6s %-18s %-10s %-10s %-10s %-12s\n", "ID", "Block", "Room No", "Type", "Occupancy", "Rent/Mo");
            System.out.println("----------------------------------------------------------------------");
            while (rs.next()) {
                Room r = createRoomInstance(rs.getInt("room_id"), rs.getInt("block_id"),
                        rs.getString("room_no"), rs.getString("room_type"),
                        rs.getInt("capacity"), rs.getInt("beds_occupied"), rs.getDouble("base_rent"));

                System.out.printf("%-6d %-18s %-10s %-10s %-10s ₹%-11.2f\n",
                        r.getRoomId(), rs.getString("block_name"), r.getRoomNo(),
                        r.getRoomType(), (r.getBedsOccupied() + "/" + r.getCapacity()), r.calculateRent());
            }
        } catch (SQLException e) {
            System.err.println("Error displaying rooms: " + e.getMessage());
        }
    }

    // OOP METHOD OVERLOADING 1: Search rooms by minimum capacity (int)
    public void searchRooms(int minCapacity) {
        System.out.println("--- Rooms with Minimum Capacity >= " + minCapacity + " ---");
        String sql = "SELECT * FROM rooms WHERE capacity >= ?";
        searchQuery(sql, minCapacity, null);
    }

    // OOP METHOD OVERLOADING 2: Search rooms by room type (String)
    public void searchRooms(String roomType) {
        System.out.println("--- Rooms of Type: " + roomType.toUpperCase() + " ---");
        String sql = "SELECT * FROM rooms WHERE UPPER(room_type) = UPPER(?)";
        searchQuery(sql, null, roomType);
    }

    private void searchQuery(String sql, Integer intParam, String strParam) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (intParam != null) ps.setInt(1, intParam);
            if (strParam != null) ps.setString(1, strParam);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-6s %-10s %-10s %-10s %-12s\n", "ID", "Room No", "Type", "Occupancy", "Rent/Mo");
                System.out.println("---------------------------------------------------------");
                while (rs.next()) {
                    Room r = createRoomInstance(rs.getInt("room_id"), rs.getInt("block_id"),
                            rs.getString("room_no"), rs.getString("room_type"),
                            rs.getInt("capacity"), rs.getInt("beds_occupied"), rs.getDouble("base_rent"));
                    System.out.printf("%-6d %-10s %-10s %-10s ₹%-11.2f\n",
                            r.getRoomId(), r.getRoomNo(), r.getRoomType(),
                            (r.getBedsOccupied() + "/" + r.getCapacity()), r.calculateRent());
                }
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    // DBMS: Add new room to table 4
    public boolean addRoom(int blockId, String roomNo, String type, int cap, double rent) {
        String sql = "INSERT INTO rooms (block_id, room_no, room_type, capacity, base_rent) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blockId);
            ps.setString(2, roomNo);
            ps.setString(3, type.toUpperCase());
            ps.setInt(4, cap);
            ps.setDouble(5, rent);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding room: " + e.getMessage());
            return false;
        }
    }
}
