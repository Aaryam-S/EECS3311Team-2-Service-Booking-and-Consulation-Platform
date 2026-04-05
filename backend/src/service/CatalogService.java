package service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    private final JdbcTemplate jdbcTemplate;

    public CatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getAllServices() {
        return jdbcTemplate.queryForList(
            "SELECT id, name, consultant_name AS \"consultantName\", " +
            "base_price AS price, duration_minutes AS duration " +
            "FROM services ORDER BY id"
        );
    }

    public Map<String, Object> findServiceById(int id) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT id, name, consultant_id AS \"consultantId\", " +
                "consultant_name AS \"consultantName\", base_price AS price, " +
                "duration_minutes AS duration " +
                "FROM services WHERE id = ?",
                id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Map<String, Object> addService(
            String name,
            Integer consultantId,
            String consultantName,
            Integer duration,
            Double price
    ) {
        Integer id = jdbcTemplate.queryForObject(
            "INSERT INTO services (name, consultant_id, consultant_name, duration_minutes, base_price) " +
            "VALUES (?, ?, ?, ?, ?) RETURNING id",
            Integer.class,
            name,
            consultantId,
            consultantName,
            duration,
            price
        );

        return findServiceById(id);
    }
}