package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.config.DatabaseConnection;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Authenticates a registered customer.
 * Endpoint: POST /login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        prepareJson(response);

        JsonNode data;
        try {
            data = JSON.readTree(request.getInputStream());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    error("Invalid JSON request body."));
            return;
        }

        String email = text(data, "email").toLowerCase();
        String password = text(data, "password");

        if (email.isBlank() || password.isBlank()) {
            writeJson(response, 422,
                    error("Email and password are required."));
            return;
        }

        String sql = """
                SELECT customer_id, first_name, email, password_hash
                FROM customers
                WHERE email = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    invalidLogin(response);
                    return;
                }

                String storedHash = result.getString("password_hash");
                boolean passwordMatches;

                try {
                    String verificationHash = storedHash;

                    if (verificationHash != null && verificationHash.startsWith("$2y$")) {
                        verificationHash = "$2a$" + verificationHash.substring(4);
                    }

                    passwordMatches = verificationHash != null
                            && BCrypt.checkpw(password, verificationHash);
                } catch (IllegalArgumentException e) {
                    passwordMatches = false;
                }

                if (!passwordMatches) {
                    invalidLogin(response);
                    return;
                }

                long customerId = result.getLong("customer_id");
                String firstName = result.getString("first_name");
                String storedEmail = result.getString("email");

                HttpSession session = request.getSession(true);
                request.changeSessionId();
                session.setAttribute("customerId", customerId);
                session.setAttribute("email", storedEmail);
                session.setAttribute("firstName", firstName);

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("ok", true);
                body.put("message", "Login successful.");
                body.put("customerId", customerId);
                body.put("firstName", firstName);
                body.put("email", storedEmail);
                body.put("redirect", "post_login.html");

                writeJson(response, HttpServletResponse.SC_OK, body);
            }

        } catch (SQLException e) {
            log("Login database error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("Unable to sign in right now."));
        }
    }

    private static void invalidLogin(HttpServletResponse response)
            throws IOException {
        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                error("Invalid email address or password."));
    }

    private static String text(JsonNode data, String field) {
        JsonNode node = data == null ? null : data.get(field);
        return node == null || node.isNull() ? "" : node.asText().trim();
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("message", message);
        return body;
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
