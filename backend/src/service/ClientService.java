package service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClientService {

    @Autowired
    private JdbcTemplate jdbc;

    // Kept for controllers that still call getInstance() — delegates to Spring bean
    private static ClientService instance;

    @Autowired
    private void selfRegister() {
        instance = this;
    }

    public static ClientService getInstance() {
        return instance;
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    public int registerClient(String username, String password, String email, String name) {
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM clients WHERE username = ?", username);
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        String hashed = sha256(password);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "INSERT INTO clients (username, password, email, name) VALUES (?, ?, ?, ?) RETURNING id",
                username, hashed, email, name);
        return ((Number) rows.get(0).get("id")).intValue();
    }

    public Map<String, Object> loginClient(String username, String password) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, username, email, name, password FROM clients WHERE username = ?", username);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Client not found.");
        }
        Map<String, Object> row = rows.get(0);
        String stored = (String) row.get("password");
        if (!sha256(password).equals(stored)) {
            throw new IllegalArgumentException("Invalid password.");
        }
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.remove("password");
        return result;
    }

    // -------------------------------------------------------------------------
    // Lookup
    // -------------------------------------------------------------------------

    public Map<String, Object> getClientById(int clientId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, username, email, name FROM clients WHERE id = ?", clientId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int getTotalClients() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM clients", Integer.class);
        return count == null ? 0 : count;
    }

    // -------------------------------------------------------------------------
    // Payment Methods
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getPaymentMethods(int clientId) {
        List<Map<String, Object>> dbResults = jdbc.queryForList(
                "SELECT id, type, last_four, email, card_number, expiry_date FROM payment_methods WHERE client_id = ?",
                clientId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : dbResults) {
            Map<String, Object> mapped = new LinkedHashMap<>(row);
            if (row.containsKey("last_four")) {
                mapped.put("lastFour", row.get("last_four"));
            }
            result.add(mapped);
        }
        return result;
    }

    public void addPaymentMethod(int clientId, Map<String, Object> pm) {
        jdbc.update(
                "INSERT INTO payment_methods (id, client_id, type, card_number, cvv, expiry_date, last_four, email, account_number, routing_number) VALUES (?,?,?,?,?,?,?,?,?,?)",
                pm.get("id"), clientId, pm.get("type"),
                pm.getOrDefault("cardNumber", null),
                pm.getOrDefault("cvv", null),
                pm.getOrDefault("expiryDate", null),
                pm.getOrDefault("lastFour", null),
                pm.getOrDefault("email", null),
                pm.getOrDefault("accountNumber", null),
                pm.getOrDefault("routingNumber", null));
    }

    public void removePaymentMethod(int clientId, String methodId) {
        jdbc.update("DELETE FROM payment_methods WHERE id = ? AND client_id = ?", methodId, clientId);
    }

    // -------------------------------------------------------------------------
    // Payment History (UC7)
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getPaymentHistory(int clientId) {
        return jdbc.queryForList(
                "SELECT id, booking_id, amount, payment_method, type, created_at FROM payment_history WHERE client_id = ? ORDER BY created_at DESC",
                clientId);
    }

    public void recordPayment(int clientId, int bookingId, double amount, String paymentMethod, String type) {
        jdbc.update(
                "INSERT INTO payment_history (client_id, booking_id, amount, payment_method, type) VALUES (?,?,?,?,?)",
                clientId, bookingId, amount, paymentMethod, type);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
