package com.moffatbaymarina.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.moffatbaymarina.config.DatabaseConnection;

/**
 * Data-access methods for the boats table.
 */
public class BoatDAO {

    public long insertBoat(Connection connection,
            long customerId,
            String boatName,
            double boatLength,
            String boatType,
            String registrationNumber)
            throws SQLException {

        String sql = """
                INSERT INTO boats
                (customer_id, boat_name, boat_length_ft, boat_type, registration_number)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            statement.setString(2, boatName);
            statement.setDouble(3, boatLength);
            setNullableString(statement, 4, boatType);
            setNullableString(statement, 5, registrationNumber);

            int changed = statement.executeUpdate();
            if (changed != 1) {
                throw new SQLException("Boat insert did not create exactly one row.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Boat ID was not generated.");
                }
                return keys.getLong(1);
            }
        }
    }

    /**
     * Returns a boat belonging to a customer. The email check is optional;
     * pass null or an empty string if the caller has already authenticated
     * the customer by session.
     */
    public CustomerBoat findCustomerBoat(Connection connection,
            long customerId,
            String email,
            String boatName)
            throws SQLException {

        String sql = """
                SELECT c.customer_id,
                       c.email,
                       b.boat_id,
                       b.boat_name,
                       b.boat_length_ft,
                       b.boat_type,
                       b.registration_number
                FROM customers c
                JOIN boats b ON b.customer_id = c.customer_id
                WHERE c.customer_id = ?
                  AND LOWER(b.boat_name) = LOWER(?)
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            statement.setString(2, boatName);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                String storedEmail = result.getString("email");
                if (email != null
                        && !email.isBlank()
                        && !storedEmail.equalsIgnoreCase(email.trim())) {
                    return null;
                }

                return new CustomerBoat(
                        result.getLong("customer_id"),
                        result.getLong("boat_id"),
                        storedEmail,
                        result.getString("boat_name"),
                        result.getDouble("boat_length_ft"),
                        result.getString("boat_type"),
                        result.getString("registration_number"));
            }
        }
    }

    public BoatRecord findById(long boatId) throws SQLException {
        String sql = """
                SELECT boat_id,
                       customer_id,
                       boat_name,
                       boat_length_ft,
                       boat_type,
                       registration_number
                FROM boats
                WHERE boat_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, boatId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return mapBoat(result);
            }
        }
    }

    public List<BoatRecord> findByCustomerId(long customerId)
            throws SQLException {

        String sql = """
                SELECT boat_id,
                       customer_id,
                       boat_name,
                       boat_length_ft,
                       boat_type,
                       registration_number
                FROM boats
                WHERE customer_id = ?
                ORDER BY boat_name
                """;

        List<BoatRecord> boats = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, customerId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    boats.add(mapBoat(result));
                }
            }
        }

        return boats;
    }

    private static BoatRecord mapBoat(ResultSet result) throws SQLException {
        return new BoatRecord(
                result.getLong("boat_id"),
                result.getLong("customer_id"),
                result.getString("boat_name"),
                result.getDouble("boat_length_ft"),
                result.getString("boat_type"),
                result.getString("registration_number"));
    }

    private static void setNullableString(PreparedStatement statement,
            int index,
            String value)
            throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }

    public record CustomerBoat(long customerId,
            long boatId,
            String email,
            String boatName,
            double boatLength,
            String boatType,
            String registrationNumber) {
    }

    public record BoatRecord(long boatId,
            long customerId,
            String boatName,
            double boatLength,
            String boatType,
            String registrationNumber) {
    }
}