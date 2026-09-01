package com.moffatbaymarina.servlet;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.config.DatabaseConnection;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Creates reservations and changes reservation status.
 *
 * POST /reservation -> create a reservation
 * PUT /reservation -> confirm or cancel a reservation
 */
@WebServlet("/reservation")
public class ReservationServlet extends HttpServlet {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        prepareJson(response);

        Long customerId = authenticatedCustomerId(request);
        if (customerId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    error("You must be logged in before creating a reservation."));
            return;
        }

        JsonNode data;
        try {
            data = JSON.readTree(request.getInputStream());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    error("Invalid JSON request body."));
            return;
        }

        String email = text(data, "email").toLowerCase();
        String boatName = text(data, "boatName");
        String checkInText = text(data, "checkIn");

        if (boatName.isBlank() || checkInText.isBlank()) {
            writeJson(response, 422,
                    error("Boat name and check-in date are required."));
            return;
        }

        LocalDate checkIn;
        try {
            checkIn = LocalDate.parse(checkInText);
        } catch (Exception e) {
            writeJson(response, 422,
                    error("Enter a valid check-in date."));
            return;
        }

        if (checkIn.isBefore(LocalDate.now())) {
            writeJson(response, 422,
                    error("Check-in date cannot be in the past."));
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            CustomerBoat customerBoat = findCustomerBoat(
                    connection,
                    customerId,
                    email,
                    boatName);

            if (customerBoat == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        error("The customer/boat combination could not be found."));
                return;
            }

            int slipSize = requiredSlipSize(customerBoat.boatLength());
            if (slipSize == 0) {
                writeJson(response, 422,
                        error("Moffat Bay currently supports reservations for boats up to 50 feet."));
                return;
            }

            double monthlyCost = monthlyCost(customerBoat.boatLength());

            connection.setAutoCommit(false);
            try {
                String reservationId = generateReservationId(connection);
                String slipNumber = generateSlipNumber(connection, slipSize);

                String insert = """
                        INSERT INTO reservations
                        (reservation_id, customer_id, boat_id, slip_size_ft,
                         slip_number, check_in_date, monthly_cost, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')
                        """;

                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setString(1, reservationId);
                    statement.setLong(2, customerBoat.customerId());
                    statement.setLong(3, customerBoat.boatId());
                    statement.setInt(4, slipSize);
                    statement.setString(5, slipNumber);
                    statement.setDate(6, Date.valueOf(checkIn));
                    statement.setDouble(7, monthlyCost);
                    statement.executeUpdate();
                }

                connection.commit();

                Map<String, Object> reservation = new LinkedHashMap<>();
                reservation.put("id", reservationId);
                reservation.put("email", customerBoat.email());
                reservation.put("boatName", customerBoat.boatName());
                reservation.put("boatLength", customerBoat.boatLength());
                reservation.put("slipSize", slipSize);
                reservation.put("slipNumber", slipNumber);
                reservation.put("checkIn", checkIn.toString());
                reservation.put("monthlyCost", monthlyCost);
                reservation.put("status", "Pending");

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("ok", true);
                body.put("message", "Reservation created.");
                body.put("reservation", reservation);
                body.put("redirect",
                        "reservation-summary.html?id=" + reservationId);

                writeJson(response, HttpServletResponse.SC_CREATED, body);

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }

        } catch (SQLException e) {
            log("Reservation database error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The reservation could not be created."));
        } catch (RuntimeException e) {
            log("Reservation server error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The reservation could not be created."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        prepareJson(response);

        Long customerId = authenticatedCustomerId(request);
        if (customerId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    error("You must be logged in to update a reservation."));
            return;
        }

        JsonNode data;
        try {
            data = JSON.readTree(request.getInputStream());
        } catch (Exception e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    error("Invalid JSON request body."));
            return;
        }

        String reservationId = text(data, "id");
        String status = text(data, "status");

        if (reservationId.isBlank()) {
            writeJson(response, 422,
                    error("Reservation ID is required."));
            return;
        }

        if (!status.equals("Confirmed") && !status.equals("Cancelled")) {
            writeJson(response, 422,
                    error("Status must be Confirmed or Cancelled."));
            return;
        }

        String sql;
        if (status.equals("Cancelled")) {
            sql = """
                    UPDATE reservations
                    SET status = ?, cancelled_at = CURRENT_TIMESTAMP
                    WHERE reservation_id = ? AND customer_id = ?
                    """;
        } else {
            sql = """
                    UPDATE reservations
                    SET status = ?, cancelled_at = NULL
                    WHERE reservation_id = ? AND customer_id = ?
                    """;
        }

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setString(2, reservationId);
            statement.setLong(3, customerId);

            int changed = statement.executeUpdate();

            if (changed == 0) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        error("Reservation not found."));
                return;
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("message", "Reservation status updated.");
            body.put("id", reservationId);
            body.put("status", status);

            writeJson(response, HttpServletResponse.SC_OK, body);

        } catch (SQLException e) {
            log("Reservation update error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The reservation could not be updated."));
        }
    }

    private CustomerBoat findCustomerBoat(Connection connection,
            long customerId,
            String email,
            String boatName)
            throws SQLException {

        String sql = """
                SELECT c.customer_id,
                       c.email,
                       b.boat_id,
                       b.boat_name,
                       b.boat_length_ft
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
                if (!email.isBlank() && !storedEmail.equalsIgnoreCase(email)) {
                    return null;
                }

                return new CustomerBoat(
                        result.getLong("customer_id"),
                        result.getLong("boat_id"),
                        storedEmail,
                        result.getString("boat_name"),
                        result.getDouble("boat_length_ft"));
            }
        }
    }

    private String generateReservationId(Connection connection)
            throws SQLException {

        String checkSql = "SELECT 1 FROM reservations WHERE reservation_id = ?";

        for (int attempt = 0; attempt < 20; attempt++) {
            String id = "MB-" + Year.now().getValue() + "-"
                    + (10000 + RANDOM.nextInt(90000));

            try (PreparedStatement statement = connection.prepareStatement(checkSql)) {
                statement.setString(1, id);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return id;
                    }
                }
            }
        }

        throw new SQLException("Unable to generate a unique reservation ID.");
    }

    private String generateSlipNumber(Connection connection, int slipSize)
            throws SQLException {

        String countSql = """
                SELECT COUNT(*)
                FROM reservations
                WHERE slip_size_ft = ?
                  AND status <> 'Cancelled'
                """;

        int used;
        try (PreparedStatement statement = connection.prepareStatement(countSql)) {
            statement.setInt(1, slipSize);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                used = result.getInt(1);
            }
        }

        String[] docks = { "A", "B", "C" };
        int startingSlip = 1;

        if (slipSize == 26) {
            startingSlip = 8;
        } else if (slipSize == 40) {
            startingSlip = 4;
        }

        String dock = docks[(used / 8) % docks.length];
        return dock + "-" + (startingSlip + (used % 8));
    }

    private static int requiredSlipSize(double boatLength) {
        if (boatLength <= 0) {
            return 0;
        }
        if (boatLength <= 26) {
            return 26;
        }
        if (boatLength <= 40) {
            return 40;
        }
        if (boatLength <= 50) {
            return 50;
        }
        return 0;
    }

    private static double monthlyCost(double boatLength) {
        return (boatLength * 10.0) + 10.0;
    }

    private static Long authenticatedCustomerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("customerId");
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
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

    private record CustomerBoat(long customerId,
            long boatId,
            String email,
            String boatName,
            double boatLength) {
    }
}
