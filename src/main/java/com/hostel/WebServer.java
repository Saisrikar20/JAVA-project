package com.hostel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Built-in Lightweight HTTP Web Server (Zero External Dependencies)
 * Serves the modern Single Page Web Application (SPA) on http://localhost:8080
 * and exposes RESTful JSON endpoints integrating all 4 team member modules and 8 tables.
 */
public class WebServer {
    private static final int DEFAULT_PORT = 8080;
    private static HttpServer server;

    private static final StudentModule studentMod = new StudentModule();
    private static final RoomModule roomMod = new RoomModule();
    private static final AllocationModule allocMod = new AllocationModule();
    private static final BillingAndComplaintModule billMod = new BillingAndComplaintModule();

    public static synchronized void start(int port) {
        if (server != null) return;
        try {
            // Initialize database
            Database.getConnection();

            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);

            // 1. Static Web UI Page
            server.createContext("/", new StaticFileHandler());

            // 2. REST API Endpoints
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/students", new StudentsHandler());
            server.createContext("/api/rooms", new RoomsHandler());
            server.createContext("/api/blocks", new BlocksHandler());
            server.createContext("/api/allocations", new AllocationsHandler());
            server.createContext("/api/billing", new BillingHandler());
            server.createContext("/api/complaints", new ComplaintsHandler());
            server.createContext("/api/viva-info", new VivaInfoHandler());

            server.setExecutor(null); // Default single-thread / lightweight executor
            server.start();
            System.out.println("[Web Server] Running at: http://localhost:" + port);
        } catch (IOException e) {
            System.err.println("[Web Server Error] Could not start server on port " + port + ": " + e.getMessage());
        }
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("[Web Server] Stopped.");
        }
    }

    public static void main(String[] args) {
        start(DEFAULT_PORT);
        System.out.println("Hostel Management Web Application is live! Open your browser to http://localhost:" + DEFAULT_PORT);
    }

    // --- HTTP HANDLERS ---

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 405, "Method Not Allowed", "text/plain");
                return;
            }
            try (InputStream in = getClass().getResourceAsStream("/web/index.html")) {
                if (in == null) {
                    sendResponse(ex, 404, "index.html not found in resources.", "text/plain");
                    return;
                }
                byte[] html = in.readAllBytes();
                ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                ex.sendResponseHeaders(200, html.length);
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(html);
                }
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 204, "", "application/json");
                return;
            }
            Map<String, String> params = parseBody(ex);
            String u = params.get("username");
            String p = params.get("password");

            User user = studentMod.login(u, p);
            if (user != null) {
                Student s = "STUDENT".equalsIgnoreCase(user.getRole()) ? studentMod.getStudent(user.getUserId()) : null;
                String json = String.format("{\"success\":true,\"userId\":%d,\"username\":\"%s\",\"role\":\"%s\",\"fullName\":\"%s\",\"regNo\":\"%s\",\"dept\":\"%s\"}",
                        user.getUserId(), user.getUsername(), user.getRole(), user.getFullName(),
                        s != null ? s.getRegNo() : "", s != null ? s.getDepartment() : "");
                sendResponse(ex, 200, json, "application/json");
            } else {
                sendResponse(ex, 401, "{\"success\":false,\"message\":\"Invalid credentials.\"}", "application/json");
            }
        }
    }

    static class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 200, studentMod.getStudentsJson(), "application/json");
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> p = parseBody(ex);
                boolean ok = studentMod.registerStudent(
                        p.get("username"), p.get("password"), p.get("fullName"),
                        p.get("regNo"), p.get("gender"), p.get("dept"), p.get("phone"));
                sendResponse(ex, ok ? 200 : 400, "{\"success\":" + ok + "}", "application/json");
            }
        }
    }

    static class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
                Integer cap = q.containsKey("capacity") ? Integer.parseInt(q.get("capacity")) : null;
                String type = q.get("type");
                sendResponse(ex, 200, roomMod.getRoomsJson(cap, type), "application/json");
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> p = parseBody(ex);
                boolean ok = roomMod.addRoom(
                        Integer.parseInt(p.get("blockId")), p.get("roomNo"),
                        p.get("type"), Integer.parseInt(p.get("capacity")),
                        Double.parseDouble(p.get("baseRent")));
                sendResponse(ex, ok ? 200 : 400, "{\"success\":" + ok + "}", "application/json");
            }
        }
    }

    static class BlocksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            sendResponse(ex, 200, roomMod.getBlocksJson(), "application/json");
        }
    }

    static class AllocationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            String path = ex.getRequestURI().getPath();
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 200, allocMod.getAllocationsJson(), "application/json");
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> p = parseBody(ex);
                if (path.endsWith("/vacate")) {
                    int sId = Integer.parseInt(p.get("studentId"));
                    boolean ok = allocMod.vacateRoom(sId);
                    sendResponse(ex, ok ? 200 : 400, "{\"success\":" + ok + "}", "application/json");
                } else {
                    int sId = Integer.parseInt(p.get("studentId"));
                    int rId = Integer.parseInt(p.get("roomId"));
                    boolean ok = allocMod.allocateRoom(sId, rId);
                    sendResponse(ex, ok ? 200 : 400, "{\"success\":" + ok + "}", "application/json");
                }
            }
        }
    }

    static class BillingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            String path = ex.getRequestURI().getPath();
            if (path.endsWith("/audit")) {
                sendResponse(ex, 200, billMod.getFinancialAuditJson(), "application/json");
                return;
            }

            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
                int sId = Integer.parseInt(q.getOrDefault("studentId", "0"));
                double bill = billMod.calculateTotalSemesterBill(sId);
                double paid = billMod.getTotalPaid(sId);
                String json = String.format("{\"totalBill\":%.2f,\"totalPaid\":%.2f,\"pending\":%.2f}",
                        bill, paid, Math.max(0, bill - paid));
                sendResponse(ex, 200, json, "application/json");
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> p = parseBody(ex);
                int sId = Integer.parseInt(p.get("studentId"));
                double amt = Double.parseDouble(p.get("amount"));
                String type = p.getOrDefault("feeType", "HOSTEL_FEE");
                boolean ok = billMod.recordPayment(sId, amt, type);
                sendResponse(ex, ok ? 200 : 400, "{\"success\":" + ok + "}", "application/json");
            }
        }
    }

    static class ComplaintsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            String path = ex.getRequestURI().getPath();
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 200, billMod.getComplaintsJson(), "application/json");
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> p = parseBody(ex);
                if (path.endsWith("/resolve")) {
                    int cId = Integer.parseInt(p.get("complaintId"));
                    String notes = p.getOrDefault("notes", "Resolved by warden.");
                    boolean ok = billMod.resolveComplaint(cId, notes);
                    sendResponse(ex, ok ? 200 : 400, "{\"success\":" + ok + "}", "application/json");
                } else {
                    int sId = Integer.parseInt(p.get("studentId"));
                    String cat = p.get("category");
                    String title = p.get("title");
                    boolean ok = billMod.lodgeComplaint(sId, cat, title);
                    sendResponse(ex, ok ? 200 : 400, "{\"success\":" + ok + "}", "application/json");
                }
            }
        }
    }

    static class VivaInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            String json = """
                {
                  "project": "Smart Hostel Management System",
                  "tables": [
                    {"table": "users", "owner": "Member 1", "purpose": "Authentication & Roles"},
                    {"table": "students", "owner": "Member 1", "purpose": "Student Profiles & Academic Info"},
                    {"table": "hostel_blocks", "owner": "Member 2", "purpose": "Residential Block Details"},
                    {"table": "rooms", "owner": "Member 2", "purpose": "Room Capacity, Occupancy & Base Rent"},
                    {"table": "wardens", "owner": "Member 3", "purpose": "Warden Staff & Assigned Blocks"},
                    {"table": "allocations", "owner": "Member 3", "purpose": "Active Room Assignments"},
                    {"table": "fee_payments", "owner": "Member 4", "purpose": "Financial Receipts & Mess Dues"},
                    {"table": "complaints", "owner": "Member 4", "purpose": "Student Grievances & Resolution Remarks"}
                  ],
                  "members": [
                    {"member": "Member 1", "file": "StudentModule.java", "oop": "Inheritance (User -> Student) & Encapsulation", "dbms": "CRUD with PreparedStatement"},
                    {"member": "Member 2", "file": "RoomModule.java", "oop": "Abstraction & Polymorphic calculateRent(), Overloading", "dbms": "Capacity constraints & Inventory"},
                    {"member": "Member 3", "file": "AllocationModule.java", "oop": "Composition & Business Rules", "dbms": "ACID Transactions (commit/rollback)"},
                    {"member": "Member 4", "file": "BillingAndComplaintModule.java", "oop": "Polymorphic Tariff Integration", "dbms": "SQL Aggregations SUM(amount)"}
                  ]
                }
                """;
            sendResponse(ex, 200, json, "application/json");
        }
    }

    // --- UTILITIES ---

    private static void sendResponse(HttpExchange ex, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static Map<String, String> parseBody(HttpExchange ex) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = ex.getRequestBody()) {
            is.transferTo(baos);
        }
        String body = baos.toString(StandardCharsets.UTF_8).trim();
        Map<String, String> map = new HashMap<>();
        if (body.startsWith("{") && body.endsWith("}")) {
            // Simple JSON parser for flat key-value pairs
            String inner = body.substring(1, body.length() - 1);
            String[] tokens = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String token : tokens) {
                String[] kv = token.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String val = kv[1].trim().replace("\"", "");
                    map.put(key, val);
                }
            }
        } else {
            // URL Encoded form data
            map = parseQuery(body);
        }
        return map;
    }
}
