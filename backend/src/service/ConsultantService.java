package service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConsultantService {

    private final JdbcTemplate jdbcTemplate;

    public ConsultantService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int registerConsultant(String username, String password, String email, String name, String specialty) {
        Integer existing = jdbcTemplate.query(
            "SELECT id FROM consultants WHERE username = ?",
            rs -> rs.next() ? rs.getInt("id") : null,
            username
        );

        if (existing != null) {
            throw new IllegalArgumentException("Username already exists.");
        }

        return jdbcTemplate.queryForObject(
            "INSERT INTO consultants (username, password, email, name, specialty, approved, status) " +
            "VALUES (?, ?, ?, ?, ?, FALSE, 'Pending') RETURNING id",
            Integer.class,
            username, password, email, name, specialty
        );
    }

    public Map<String, Object> loginConsultant(String username, String password) {
        try {
            Map<String, Object> consultant = jdbcTemplate.queryForMap(
                "SELECT id, username, password, email, name, specialty, approved, status " +
                "FROM consultants WHERE username = ?",
                username
            );

            if (!password.equals(consultant.get("password"))) {
                throw new IllegalArgumentException("Invalid password.");
            }

            Boolean approved = (Boolean) consultant.get("approved");
            if (approved == null || !approved) {
                throw new IllegalArgumentException("Consultant account not yet approved by admin.");
            }

            return Map.of(
                "id", consultant.get("id"),
                "username", consultant.get("username"),
                "email", consultant.get("email"),
                "name", consultant.get("name"),
                "specialty", consultant.get("specialty"),
                "status", consultant.get("status")
            );
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Consultant not found.");
        }
    }

    public Map<String, Object> getConsultantById(int consultantId) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT id, username, email, name, specialty, approved, status " +
                "FROM consultants WHERE id = ?",
                consultantId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> getPendingConsultants() {
        return jdbcTemplate.queryForList(
            "SELECT id, name, email, specialty, status FROM consultants WHERE approved = FALSE ORDER BY id"
        );
    }

    public List<Map<String, Object>> getApprovedConsultants() {
        return jdbcTemplate.queryForList(
            "SELECT id, name, email, specialty, status FROM consultants WHERE approved = TRUE ORDER BY id"
        );
    }

    public void approveConsultant(int consultantId) {
        int updated = jdbcTemplate.update(
            "UPDATE consultants SET approved = TRUE, status = 'Approved' WHERE id = ?",
            consultantId
        );

        if (updated == 0) {
            throw new IllegalArgumentException("Consultant not found.");
        }
    }

    public void rejectConsultant(int consultantId) {
        int deleted = jdbcTemplate.update(
            "DELETE FROM consultants WHERE id = ?",
            consultantId
        );

        if (deleted == 0) {
            throw new IllegalArgumentException("Consultant not found.");
        }
    }
}