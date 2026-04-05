package service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private final JdbcTemplate jdbcTemplate;

    public ClientService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int registerClient(String username, String password, String email, String name) {
        Integer existing = jdbcTemplate.query(
            "SELECT id FROM clients WHERE username = ?",
            rs -> rs.next() ? rs.getInt("id") : null,
            username
        );

        if (existing != null) {
            throw new IllegalArgumentException("Username already exists.");
        }

        return jdbcTemplate.queryForObject(
            "INSERT INTO clients (username, password, email, name) VALUES (?, ?, ?, ?) RETURNING id",
            Integer.class,
            username, password, email, name
        );
    }

    public Map<String, Object> loginClient(String username, String password) {
        try {
            Map<String, Object> client = jdbcTemplate.queryForMap(
                "SELECT id, username, password, email, name FROM clients WHERE username = ?",
                username
            );

            if (!password.equals(client.get("password"))) {
                throw new IllegalArgumentException("Invalid password.");
            }

            return Map.of(
                "id", client.get("id"),
                "username", client.get("username"),
                "email", client.get("email"),
                "name", client.get("name")
            );
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Client not found.");
        }
    }

    public Map<String, Object> getClientById(int clientId) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT id, username, email, name FROM clients WHERE id = ?",
                clientId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> getPaymentMethods(int clientId) {
        return jdbcTemplate.queryForList(
            "SELECT id, type, card_number AS \"cardNumber\", cvv, expiry_date AS \"expiryDate\", " +
            "email, account_number AS \"accountNumber\", routing_number AS \"routingNumber\", " +
            "last_four AS \"lastFour\" " +
            "FROM payment_methods WHERE client_id = ? ORDER BY id",
            clientId
        );
    }

    public void addPaymentMethod(int clientId, Map<String, Object> paymentMethod) {
        Integer exists = jdbcTemplate.query(
            "SELECT id FROM clients WHERE id = ?",
            rs -> rs.next() ? rs.getInt("id") : null,
            clientId
        );

        if (exists == null) {
            throw new IllegalArgumentException("Client not found.");
        }

        jdbcTemplate.update(
            "INSERT INTO payment_methods " +
            "(id, client_id, type, card_number, cvv, expiry_date, email, account_number, routing_number, last_four) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            paymentMethod.get("id"),
            clientId,
            paymentMethod.get("type"),
            paymentMethod.get("cardNumber"),
            paymentMethod.get("cvv"),
            paymentMethod.get("expiryDate"),
            paymentMethod.get("email"),
            paymentMethod.get("accountNumber"),
            paymentMethod.get("routingNumber"),
            paymentMethod.get("lastFour")
        );
    }

    public void removePaymentMethod(int clientId, String methodId) {
        int deleted = jdbcTemplate.update(
            "DELETE FROM payment_methods WHERE client_id = ? AND id = ?",
            clientId, methodId
        );

        if (deleted == 0) {
            throw new IllegalArgumentException("Client or payment method not found.");
        }
    }

    public int getTotalClients() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM clients",
            Integer.class
        );
        return count == null ? 0 : count;
    }
}