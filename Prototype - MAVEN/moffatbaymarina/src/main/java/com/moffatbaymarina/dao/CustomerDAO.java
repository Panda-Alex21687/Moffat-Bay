package com.moffatbaymarina.dao;

import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class CustomerDAO {

    public Customer insert(Customer customer) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return insert(connection, customer);
        }
    }

    public Customer insert(Connection connection, Customer customer) throws SQLException {
        String sql = """
                INSERT INTO customers
                (first_name, last_name, phone, street, city, state, zip,
                 email, password_hash, email_verified)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getPhone());
            statement.setString(4, customer.getStreet());
            statement.setString(5, customer.getCity());
            statement.setString(6, customer.getState());
            statement.setString(7, customer.getZip());
            statement.setString(8, customer.getEmail());
            statement.setString(9, customer.getPasswordHash());
            statement.setBoolean(10, customer.isEmailVerified());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Customer insert did not create one row.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No customer_id was generated.");
                }
                customer.setCustomerId(keys.getLong(1));
            }
        }
        return customer;
    }

    public boolean emailExists(String email) throws SQLException {
        return findByEmail(email) != null;
    }

    public Customer findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM customers WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public Customer findById(long customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public boolean setEmailVerified(Connection connection, long customerId, boolean verified)
            throws SQLException {
        String sql = "UPDATE customers SET email_verified = ? WHERE customer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, verified);
            statement.setLong(2, customerId);
            return statement.executeUpdate() == 1;
        }
    }

    private Customer map(ResultSet result) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(result.getLong("customer_id"));
        customer.setFirstName(result.getString("first_name"));
        customer.setLastName(result.getString("last_name"));
        customer.setPhone(result.getString("phone"));
        customer.setStreet(result.getString("street"));
        customer.setCity(result.getString("city"));
        customer.setState(result.getString("state"));
        customer.setZip(result.getString("zip"));
        customer.setEmail(result.getString("email"));
        customer.setPasswordHash(result.getString("password_hash"));
        customer.setEmailVerified(result.getBoolean("email_verified"));
        Timestamp created = result.getTimestamp("created_at");
        customer.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return customer;
    }
}
