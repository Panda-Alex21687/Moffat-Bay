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
    
    public Boat findForCustomer(long boatId, long customerId) {

        String sql = """
                SELECT boat_id, customer_id, boat_name, boat_length_ft,
                       boat_type, registration_number, created_at
                FROM boats
                WHERE boat_id = ?
                  AND customer_id = ?
                LIMIT 1
                """;

        java.util.List<Boat> boats = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Boat boat = new Boat();

                    boat.setBoatId(rs.getLong("boat_id"));
                    boat.setCustomerId(rs.getLong("customer_id"));
                    boat.setBoatName(rs.getString("boat_name"));
                    boat.setBoatLengthFt(rs.getBigDecimal("boat_length_ft"));
                    boat.setBoatType(rs.getString("boat_type"));
                    boat.setRegistrationNumber(
                            rs.getString("registration_number")
                    );

                    java.sql.Timestamp created =
                            rs.getTimestamp("created_at");

                    if (created != null) {
                        boat.setCreatedAt(created.toLocalDateTime());
                    }

                    return boat;
                },
                boatId,
                customerId
        );

        return boats.isEmpty() ? null : boats.get(0);
    }
    
    public Boat findByCustomerAndName(long customerId, String boatName) {

        String sql = """
                SELECT boat_id, customer_id, boat_name, boat_length_ft,
                       boat_type, registration_number, created_at
                FROM boats
                WHERE customer_id = ?
                  AND LOWER(boat_name) = LOWER(?)
                LIMIT 1
                """;

        java.util.List<Boat> boats = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Boat boat = new Boat();

                    boat.setBoatId(rs.getLong("boat_id"));
                    boat.setCustomerId(rs.getLong("customer_id"));
                    boat.setBoatName(rs.getString("boat_name"));
                    boat.setBoatLengthFt(rs.getBigDecimal("boat_length_ft"));
                    boat.setBoatType(rs.getString("boat_type"));
                    boat.setRegistrationNumber(
                            rs.getString("registration_number")
                    );

                    java.sql.Timestamp created =
                            rs.getTimestamp("created_at");

                    if (created != null) {
                        boat.setCreatedAt(created.toLocalDateTime());
                    }

                    return boat;
                },
                customerId,
                boatName
        );

        return boats.isEmpty() ? null : boats.get(0);
    }
    
    public Boat findFirstByCustomerId(long customerId) {

        String sql = """
                SELECT boat_id, customer_id, boat_name, boat_length_ft,
                       boat_type, registration_number, created_at
                FROM boats
                WHERE customer_id = ?
                ORDER BY boat_id
                LIMIT 1
                """;

        java.util.List<Boat> boats = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Boat boat = new Boat();

                    boat.setBoatId(rs.getLong("boat_id"));
                    boat.setCustomerId(rs.getLong("customer_id"));
                    boat.setBoatName(rs.getString("boat_name"));
                    boat.setBoatLengthFt(rs.getBigDecimal("boat_length_ft"));
                    boat.setBoatType(rs.getString("boat_type"));
                    boat.setRegistrationNumber(
                            rs.getString("registration_number")
                    );

                    java.sql.Timestamp created =
                            rs.getTimestamp("created_at");

                    if (created != null) {
                        boat.setCreatedAt(created.toLocalDateTime());
                    }

                    return boat;
                },
                customerId
        );

        return boats.isEmpty() ? null : boats.get(0);
    }
}