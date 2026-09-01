package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.config.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Creates a customer account and the customer's boat in one transaction.
 * Endpoint: POST /register
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final Pattern ZIP_PATTERN = Pattern.compile(
            "^\\d{5}(-\\d{4})?$");

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        prepareJson(response);

        JsonNode data;
        try {
            data = JSON.readTree(request.getInputStream());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    error("Invalid JSON request body."));
            return;
        }

        try {
            String firstName = requiredText(data, "firstName", "First name");
            String lastName = requiredText(data, "lastName", "Last name");
            String phone = requiredText(data, "phone", "Phone number");
            String street = requiredText(data, "street", "Street address");
            String city = requiredText(data, "city", "City");
            String state = requiredText(data, "state", "State").toUpperCase();
            String zip = requiredText(data, "zip", "ZIP code");
            String email = requiredText(data, "email", "Email address")
                    .toLowerCase();
            String password = requiredText(data, "password", "Password");
            String boatName = requiredText(data, "boatName", "Boat name");
            double boatLength = requiredDouble(data, "boatLength", "Boat length");
            String boatType = optionalText(data, "boatType");
            String registrationNumber = optionalText(data, "registrationNumber");

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                writeJson(response, 422,
                        error("Enter a valid email address."));
                return;
            }

            if (!ZIP_PATTERN.matcher(zip).matches()) {
                writeJson(response, 422,
                        error("Enter a valid ZIP code."));
                return;
            }

            if (state.length() != 2) {
                writeJson(response, 422,
                        error("Use the two-letter state abbreviation."));
                return;
            }

            if (boatLength <= 0 || boatLength > 200) {
                writeJson(response, 422,
                        error("Boat length must be between 1 and 200 feet."));
                return;
            }

            if (!validPassword(password)) {
                writeJson(response, 422, error(
                        "Password must be at least 8 characters and include " +
                                "uppercase, lowercase, a number, and a special character."));
                return;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {

                if (emailExists(connection, email)) {
                    writeJson(response, HttpServletResponse.SC_CONFLICT,
                            error("An account already exists for this email address. Please sign in instead."));
                    return;
                }

                connection.setAutoCommit(false);

                try {
                    long customerId = insertCustomer(
                            connection,
                            firstName,
                            lastName,
                            phone,
                            street,
                            city,
                            state,
                            zip,
                            email,
                            BCrypt.hashpw(password, BCrypt.gensalt(12)));

                    long boatId = insertBoat(
                            connection,
                            customerId,
                            boatName,
                            boatLength,
                            boatType,
                            registrationNumber);

                    connection.commit();

                    HttpSession session = request.getSession(true);
                    request.changeSessionId();
                    session.setAttribute("customerId", customerId);
                    session.setAttribute("email", email);
                    session.setAttribute("firstName", firstName);
                    session.setAttribute("boatId", boatId);

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("ok", true);
                    result.put("message", "Account and boat information saved.");
                    result.put("customerId", customerId);
                    result.put("boatId", boatId);
                    result.put("redirect", "verification.html");

                    writeJson(response, HttpServletResponse.SC_CREATED, result);

                } catch (SQLException | RuntimeException e) {
                    rollbackQuietly(connection);
                    throw e;
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException ignored) {
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            writeJson(response, 422, error(e.getMessage()));
        } catch (SQLException e) {
            log("Registration database error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The account could not be created."));
        } catch (RuntimeException e) {
            log("Registration server error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The account could not be created."));
        }
    }

    private boolean emailExists(Connection connection, String email)
            throws SQLException {

        String sql = "SELECT customer_id FROM customers WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private long insertCustomer(Connection connection,
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

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Customer ID was not generated.");
                }
                return keys.getLong(1);
            }
        }
    }

    private long insertBoat(Connection connection,
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

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Boat ID was not generated.");
                }
                return keys.getLong(1);
            }
        }
    }

    private static boolean validPassword(String password) {
        return password.length() >= 8
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    }

    private static String requiredText(JsonNode data,
            String field,
            String label) {
        JsonNode node = data.get(field);
        String value = node == null || node.isNull() ? "" : node.asText().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value;
    }

    private static double requiredDouble(JsonNode data,
            String field,
            String label) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException(label + " is required.");
        }

        try {
            return node.asDouble();
        } catch (Exception e) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }

    private static String optionalText(JsonNode data, String field) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }

    private static void setNullableString(PreparedStatement statement,
            int index,
            String value)
            throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, java.sql.Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", false);
        result.put("message", message);
        return result;
    }

    private static void prepareJson(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private static void writeJson(HttpServletResponse response,
            int status,
            Object body)
            throws IOException {
        response.setStatus(status);
        JSON.writeValue(response.getWriter(), body);
    }
}
