package com.moffatbaymarina.dao;

import com.moffatbaymarina.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data-access methods for the customers table.
 *
 * This DAO keeps SQL out of the servlet layer. Methods that accept an
 * existing Connection are intended for transactions such as registration,
 * where a customer and boat must be inserted together.
 */
public class CustomerDAO {

    public boolean emailExists(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return emailExists(connection, email);
        }
    }

    public boolean emailExists(Connection connection, String email)
            throws SQLException {

        String sql = "SELECT customer_id FROM customers WHERE LOWER(email) = LOWER(?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public long insertCustomer(Connection connection,
            String firstName,
            String lastName,
            String phone,
            String street,
            String city,
            String state,
            String zip,
            String email,
            String passwordHash)
            throws SQLException {

        String sql = """
                INSERT INTO customers
                (first_name, last_name, phone, street, city, state, zip, email, password_hash)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, phone);
            statement.setString(4, street);
            statement.setString(5, city);
            statement.setString(6, state);
            statement.setString(7, zip);
            statement.setString(8, email);
            statement.setString(9, passwordHash);

            int changed = statement.executeUpdate();
            if (changed != 1) {
                throw new SQLException("Customer insert did not create exactly one row.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Customer ID was not generated.");
                }
                return keys.getLong(1);
            }
        }
    }

    public LoginCustomer findLoginByEmail(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return findLoginByEmail(connection, email);
        }
    }

    public LoginCustomer findLoginByEmail(Connection connection, String email)
            throws SQLException {

        String sql = """
                SELECT customer_id, first_name, email, password_hash
                FROM customers
                WHERE LOWER(email) = LOWER(?)
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return new LoginCustomer(
                        result.getLong("customer_id"),
                        result.getString("first_name"),
                        result.getString("email"),
                        result.getString("password_hash"));
            }
        }
    }

    public CustomerRecord findById(long customerId) throws SQLException {
        String sql = """
                SELECT customer_id,
                       first_name,
                       last_name,
                       phone,
                       street,
                       city,
                       state,
                       zip,
                       email
                FROM customers
                WHERE customer_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, customerId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return new CustomerRecord(
                        result.getLong("customer_id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("phone"),
                        result.getString("street"),
                        result.getString("city"),
                        result.getString("state"),
                        result.getString("zip"),
                        result.getString("email"));
            }
        }
    }

    public record LoginCustomer(long customerId,
            String firstName,
            String email,
            String passwordHash) {
    }

    public record CustomerRecord(long customerId,
            String firstName,
            String lastName,
            String phone,
            String street,
            String city,
            String state,
            String zip,
            String email) {
    }
}