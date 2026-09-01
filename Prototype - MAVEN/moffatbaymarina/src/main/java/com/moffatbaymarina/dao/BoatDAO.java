package com.moffatbaymarina.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.model.Boat;

public class BoatDAO {

    public Boat insert(Boat boat) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return insert(connection, boat);
        }
    }

    public Boat insert(Connection connection, Boat boat) throws SQLException {
        String sql = """
                INSERT INTO boats
                (customer_id, boat_name, boat_length_ft, boat_type, registration_number)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, boat.getCustomerId());
            statement.setString(2, boat.getBoatName());
            statement.setBigDecimal(3, boat.getBoatLengthFt());
            statement.setString(4, blankToNull(boat.getBoatType()));
            statement.setString(5, blankToNull(boat.getRegistrationNumber()));
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Boat insert did not create one row.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No boat_id was generated.");
                }
                boat.setBoatId(keys.getLong(1));
            }
        }
        return boat;
    }

    public Boat findById(long boatId) throws SQLException {
        String sql = "SELECT * FROM boats WHERE boat_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boatId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public Boat findForCustomer(long boatId, long customerId) throws SQLException {
        String sql = "SELECT * FROM boats WHERE boat_id = ? AND customer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boatId);
            statement.setLong(2, customerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public Boat findByCustomerAndName(long customerId, String boatName) throws SQLException {
        String sql = """
                SELECT * FROM boats
                WHERE customer_id = ? AND LOWER(boat_name) = LOWER(?)
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            statement.setString(2, boatName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public List<Boat> findByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT * FROM boats WHERE customer_id = ? ORDER BY created_at, boat_id";
        List<Boat> boats = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    boats.add(map(result));
                }
            }
        }
        return boats;
    }

    private Boat map(ResultSet result) throws SQLException {
        Boat boat = new Boat();
        boat.setBoatId(result.getLong("boat_id"));
        boat.setCustomerId(result.getLong("customer_id"));
        boat.setBoatName(result.getString("boat_name"));
        boat.setBoatLengthFt(result.getBigDecimal("boat_length_ft"));
        boat.setBoatType(result.getString("boat_type"));
        boat.setRegistrationNumber(result.getString("registration_number"));
        Timestamp created = result.getTimestamp("created_at");
        boat.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return boat;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
