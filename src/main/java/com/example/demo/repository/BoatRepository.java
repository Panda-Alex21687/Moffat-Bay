package com.example.demo.repository;

import com.example.demo.model.Boat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BoatRepository {

    private final JdbcTemplate jdbcTemplate;

    public BoatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public long save(Boat boat) {
        String sql = """
            INSERT INTO boats
            (customer_id, boat_name, boat_length_ft, boat_type, registration_number)
            VALUES (?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
            sql,
            boat.getCustomerId(),
            boat.getBoatName(),
            boat.getBoatLengthFt(),
            boat.getBoatType(),
            boat.getRegistrationNumber()
        );

        Long boatId = jdbcTemplate.queryForObject(
            "SELECT LAST_INSERT_ID()",
            Long.class
        );

        return boatId;
    }
}