package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConsultantService {

    private final JdbcTemplate jdbc;

    public ConsultantService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int registerConsultant(String username, String password, String email, String name, String specialty) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM consultants WHERE username = ?", Integer.class, username);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("Username already exists.");
        }
        Integer id = jdbc.queryForObject(
                "INSERT INTO consultants (username, password, email, name, specialty, approved, status) VALUES (?, ?, ?, ?, ?, false, 'Pending') RETURNING id",
                Integer.class, username, password, email, name, specialty);
        return id != null ? id : -1;
    }

    public Map<String, Object> loginConsultant(String username, String password) {
        List<Map<String, Object>> results = jdbc.queryForList(
                "SELECT * FROM consultants WHERE username = ?", username);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Consultant not found.");
        }
        Map<String, Object> consultant = results.get(0);
        if (!password.equals(consultant.get("password"))) {
            throw new IllegalArgumentException("Invalid password.");
        }
        Boolean approved = (Boolean) consultant.get("approved");
        if (approved == null || !approved) {
            throw new IllegalArgumentException("Consultant account not yet approved by admin.");
        }
        return toConsultantMap(consultant);
    }

    public Map<String, Object> getConsultantById(int consultantId) {
        List<Map<String, Object>> results = jdbc.queryForList(
                "SELECT * FROM consultants WHERE id = ?", consultantId);
        return results.isEmpty() ? null : toConsultantMap(results.get(0));
    }

    public Integer findConsultantIdByName(String name) {
        List<Map<String, Object>> results = jdbc.queryForList(
                "SELECT id FROM consultants WHERE name = ? AND approved = true LIMIT 1", name);
        if (results.isEmpty()) return null;
        return (Integer) results.get(0).get("id");
    }

    public List<Map<String, Object>> getPendingConsultants() {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM consultants WHERE approved = false");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) result.add(toConsultantMap(row));
        return result;
    }

    public List<Map<String, Object>> getApprovedConsultants() {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM consultants WHERE approved = true");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) result.add(toConsultantMap(row));
        return result;
    }

    public void approveConsultant(int consultantId) {
        jdbc.update("UPDATE consultants SET approved = true, status = 'Approved' WHERE id = ?", consultantId);
    }

    public void rejectConsultant(int consultantId) {
        jdbc.update("DELETE FROM consultants WHERE id = ?", consultantId);
    }

    private Map<String, Object> toConsultantMap(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.get("id"));
        m.put("username", row.get("username"));
        m.put("email", row.get("email"));
        m.put("name", row.get("name"));
        m.put("specialty", row.get("specialty"));
        m.put("approved", row.get("approved"));
        m.put("status", row.get("status"));
        return m;
    }
}
