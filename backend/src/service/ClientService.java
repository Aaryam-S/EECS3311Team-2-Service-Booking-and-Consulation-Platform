package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClientService {

    private final JdbcTemplate jdbc;
    private final AtomicInteger paymentMethodIdCounter = new AtomicInteger(1000);

    public ClientService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int registerClient(String username, String password, String email, String name) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE username = ?", Integer.class, username);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("Username already exists.");
        }
        Integer id = jdbc.queryForObject(
                "INSERT INTO clients (username, password, email, name) VALUES (?, ?, ?, ?) RETURNING id",
                Integer.class, username, password, email, name);
        return id != null ? id : -1;
    }

    public Map<String, Object> loginClient(String username, String password) {
        List<Map<String, Object>> results = jdbc.queryForList(
                "SELECT * FROM clients WHERE username = ?", username);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Client not found.");
        }
        Map<String, Object> client = results.get(0);
        if (!password.equals(client.get("password"))) {
            throw new IllegalArgumentException("Invalid password.");
        }
        return toClientMap(client);
    }

    public Map<String, Object> getClientById(int clientId) {
        List<Map<String, Object>> results = jdbc.queryForList(
                "SELECT * FROM clients WHERE id = ?", clientId);
        return results.isEmpty() ? null : toClientMap(results.get(0));
    }

    public int getTotalClients() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM clients", Integer.class);
        return count != null ? count : 0;
    }

    // Payment methods
    public List<Map<String, Object>> getPaymentMethods(int clientId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM payment_methods WHERE client_id = ?", clientId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(toPaymentMethodMap(row));
        }
        return result;
    }

    public void addPaymentMethod(int clientId, Map<String, Object> method) {
        if (getClientById(clientId) == null) {
            throw new IllegalArgumentException("Client not found.");
        }
        String id = "pm_" + paymentMethodIdCounter.incrementAndGet();
        method.put("id", id);
        jdbc.update(
                "INSERT INTO payment_methods (id, client_id, type, card_number, cvv, expiry_date, email, account_number, routing_number, last_four) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                method.getOrDefault("id", id),
                clientId,
                method.get("type"),
                method.get("cardNumber"),
                method.get("cvv"),
                method.get("expiryDate"),
                method.get("email"),
                method.get("accountNumber"),
                method.get("routingNumber"),
                method.get("lastFour"));
    }

    public void removePaymentMethod(int clientId, String methodId) {
        int rows = jdbc.update(
                "DELETE FROM payment_methods WHERE id = ? AND client_id = ?", methodId, clientId);
        if (rows == 0) {
            throw new IllegalArgumentException("Payment method not found.");
        }
    }

    // Payment history (UC7)
    public List<Map<String, Object>> getPaymentHistory(int clientId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM payment_history WHERE client_id = ? ORDER BY created_at DESC", clientId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", row.get("id"));
            entry.put("bookingId", row.get("booking_id"));
            entry.put("amount", row.get("amount"));
            entry.put("methodType", row.get("method_type"));
            entry.put("transactionId", row.get("transaction_id"));
            entry.put("status", row.get("status"));
            entry.put("createdAt", row.get("created_at") != null ? row.get("created_at").toString() : null);
            result.add(entry);
        }
        return result;
    }

    public void recordPayment(int clientId, int bookingId, double amount, String methodType, String transactionId) {
        jdbc.update(
                "INSERT INTO payment_history (client_id, booking_id, amount, method_type, transaction_id, status) VALUES (?, ?, ?, ?, ?, 'Completed')",
                clientId, bookingId, amount, methodType, transactionId);
    }

    // Map DB row (snake_case) to API map (camelCase)
    private Map<String, Object> toClientMap(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.get("id"));
        m.put("username", row.get("username"));
        m.put("email", row.get("email"));
        m.put("name", row.get("name"));
        return m;
    }

    private Map<String, Object> toPaymentMethodMap(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.get("id"));
        m.put("type", row.get("type"));
        m.put("cardNumber", row.get("card_number"));
        m.put("cvv", row.get("cvv"));
        m.put("expiryDate", row.get("expiry_date"));
        m.put("email", row.get("email"));
        m.put("accountNumber", row.get("account_number"));
        m.put("routingNumber", row.get("routing_number"));
        m.put("lastFour", row.get("last_four"));
        return m;
    }
}
