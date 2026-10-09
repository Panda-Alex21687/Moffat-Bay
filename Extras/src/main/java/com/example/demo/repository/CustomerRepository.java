package com.example.demo.repository;

import com.example.demo.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean emailExists(String email) {
        String sql = """
            SELECT COUNT(*)
            FROM customers
            WHERE LOWER(email) = LOWER(?)
            """;

        Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            email
        );

        return count != null && count > 0;
    }
    

    public long save(Customer customer) {

        String sql = """
            INSERT INTO customers
            (first_name, last_name, phone, street, city, state, zip,
             email, password_hash, email_verified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                sql,
                java.sql.Statement.RETURN_GENERATED_KEYS
            );

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

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                "Customer was inserted, but no customer ID was returned."
            );
        }

        return key.longValue();
    }
    public int markEmailVerified(long customerId) {

        String sql = """
            UPDATE customers
            SET email_verified = true
            WHERE customer_id = ?
            """;

        return jdbcTemplate.update(sql, customerId);
    }
    public Customer findByEmail(String email) {
        String sql = """
            SELECT customer_id, first_name, last_name, email,
                   password_hash, email_verified
            FROM customers
            WHERE LOWER(email) = LOWER(?)
            LIMIT 1
            """;

        java.util.List<Customer> customers = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                Customer customer = new Customer();

                customer.setCustomerId(rs.getLong("customer_id"));
                customer.setFirstName(rs.getString("first_name"));
                customer.setLastName(rs.getString("last_name"));
                customer.setEmail(rs.getString("email"));
                customer.setPasswordHash(rs.getString("password_hash"));
                customer.setEmailVerified(rs.getBoolean("email_verified"));

                return customer;
            },
            email
        );

        return customers.isEmpty() ? null : customers.get(0);
    }
}