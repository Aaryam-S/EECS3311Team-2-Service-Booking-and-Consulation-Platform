package service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConsultantService {

    @Autowired
    private JdbcTemplate jdbc;

    private static ConsultantService instance;

    @Autowired
    private void selfRegister() {
        instance = this;
    }

    public static ConsultantService getInstance() {
        return instance;
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    public int registerConsultant(String username, String password, String email, String name, String specialty) {
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM consultants WHERE username = ?", username);
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        String hashed = sha256(password);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "INSERT INTO consultants (username, password, email, name, specialty) VALUES (?,?,?,?,?) RETURNING id",
                username, hashed, email, name, specialty);
        return ((Number) rows.get(0).get("id")).intValue();
    }

    public Map<String, Object> loginConsultant(String username, String password) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, username, email, name, specialty, approved, status, password FROM consultants WHERE username = ?",
                username);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Consultant not found.");
        }
        Map<String, Object> row = rows.get(0);
        String stored = (String) row.get("password");
        if (!sha256(password).equals(stored)) {
            throw new IllegalArgumentException("Invalid password.");
        }
        Boolean approved = (Boolean) row.get("approved");
        if (approved == null || !approved) {
            throw new IllegalArgumentException("Consultant account not yet approved by admin.");
        }
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.remove("password");
        return result;
    }

    // -------------------------------------------------------------------------
    // Lookup
    // -------------------------------------------------------------------------

    public Map<String, Object> getConsultantById(int consultantId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, username, email, name, specialty, approved, status FROM consultants WHERE id = ?",
                consultantId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Integer findConsultantIdByName(String name) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id FROM consultants WHERE name = ? AND approved = TRUE LIMIT 1", name);
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).intValue();
    }

    public List<Map<String, Object>> getPendingConsultants() {
        return jdbc.queryForList(
                "SELECT id, username, email, name, specialty, approved, status FROM consultants WHERE approved = FALSE");
    }

    public List<Map<String, Object>> getApprovedConsultants() {
        return jdbc.queryForList(
                "SELECT id, username, email, name, specialty, approved, status FROM consultants WHERE approved = TRUE");
    }

    // -------------------------------------------------------------------------
    // Admin actions
    // -------------------------------------------------------------------------

    public void approveConsultant(int consultantId) {
        jdbc.update("UPDATE consultants SET approved = TRUE, status = 'Approved' WHERE id = ?", consultantId);
    }

    public void rejectConsultant(int consultantId) {
        jdbc.update("DELETE FROM consultants WHERE id = ?", consultantId);
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
