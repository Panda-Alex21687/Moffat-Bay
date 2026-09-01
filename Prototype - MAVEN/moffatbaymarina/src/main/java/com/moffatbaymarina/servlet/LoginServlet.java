package com.moffatbaymarina.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.dao.CustomerDAO;
import com.moffatbaymarina.model.Customer;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareJson(response);
        String[] credentials = readCredentials(request, response);
        if (credentials == null)
            return;

        String email = credentials[0].trim().toLowerCase();
        String password = credentials[1];
        if (email.isBlank() || password.isBlank()) {
            writeJson(response, 422, error("Email and password are required."));
            return;
        }

        try {
            Customer customer = new CustomerDAO().findByEmail(email);
            if (customer == null || !passwordMatches(password, customer.getPasswordHash())) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        error("Invalid email or password."));
                return;
            }
            if (!customer.isEmailVerified()) {
                Map<String, Object> body = error("Verify your email before logging in.");
                body.put("emailVerified", false);
                body.put("redirect", "verification.html");
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, body);
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null)
                oldSession.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute("customerId", customer.getCustomerId());
            session.setAttribute("email", customer.getEmail());
            session.setAttribute("firstName", customer.getFirstName());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("message", "Login successful.");
            body.put("customerId", customer.getCustomerId());
            body.put("email", customer.getEmail());
            body.put("firstName", customer.getFirstName());
            body.put("redirect", "post_login.html");
            writeJson(response, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log("Login error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("Login could not be completed."));
        }
    }

    /**
     * New accounts use BCrypt. The supplied course seed data currently stores
     * plain-text demo values in password_hash, so a temporary fallback keeps
     * those demo accounts usable until the seed data is converted to BCrypt.
     */
    private boolean passwordMatches(String password, String storedValue) {
        if (storedValue == null || storedValue.isBlank())
            return false;
        if (storedValue.startsWith("$2a$") || storedValue.startsWith("$2b$")
                || storedValue.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(password, storedValue);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return password.equals(storedValue);
    }

    private String[] readCredentials(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            try {
                JsonNode data = JSON.readTree(request.getInputStream());
                return new String[] {
                        text(data, "email"),
                        text(data, "password")
                };
            } catch (IOException e) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        error("Invalid JSON request body."));
                return null;
            }
        }
        String email = request.getParameter("email");
        if (email == null)
            email = request.getParameter("loginEmail");
        String password = request.getParameter("password");
        if (password == null)
            password = request.getParameter("loginPassword");
        return new String[] {
                email == null ? "" : email,
                password == null ? "" : password
        };
    }

    private String text(JsonNode data, String field) {
        JsonNode node = data.get(field);
        return node == null || node.isNull() ? "" : node.asText("").trim();
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("message", message);
        return body;
    }

    private void prepareJson(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void writeJson(HttpServletResponse response, int status, Object body)
            throws IOException {
        response.setStatus(status);
        JSON.writeValue(response.getWriter(), body);
    }

}
