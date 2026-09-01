package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Looks up reservations belonging to the logged-in customer.
 * Endpoint: GET /reservation-lookup?id=MB-2026-12345&email=name@example.com
 * Either filter may be omitted. With neither filter, returns reservation
 * history.
 */
@WebServlet("/reservation-lookup")
public class ReservationLookupServlet extends HttpServlet {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        prepareJson(response);

        Long customerId = authenticatedCustomerId(request);
        if (customerId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    error("You must be logged in to view reservations."));
            return;
        }

        String reservationId = clean(request.getParameter("id"));
        String email = clean(request.getParameter("email")).toLowerCase();

        StringBuilder sql = new StringBuilder("""
                SELECT r.reservation_id,
                       c.email,
                       b.boat_name,
                       b.boat_length_ft,
                       r.slip_size_ft,
                       r.slip_number,
                       r.check_in_date,
                       r.monthly_cost,
                       r.status,
                       r.created_at,
                       r.cancelled_at
                FROM reservations r
                JOIN customers c ON c.customer_id = r.customer_id
                JOIN boats b ON b.boat_id = r.boat_id
                WHERE r.customer_id = ?
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(customerId);

        if (!reservationId.isBlank()) {
            sql.append(" AND LOWER(r.reservation_id) = LOWER(?) ");
            parameters.add(reservationId);
        }

        if (!email.isBlank()) {
            sql.append(" AND LOWER(c.email) = LOWER(?) ");
            parameters.add(email);
        }

        sql.append(" ORDER BY r.created_at DESC ");

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                Object value = parameters.get(i);
                if (value instanceof Number number) {
                    statement.setLong(i + 1, number.longValue());
                } else {
                    statement.setString(i + 1, String.valueOf(value));
                }
            }

            List<Map<String, Object>> reservations = new ArrayList<>();

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    reservations.add(toReservation(result));
                }
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("count", reservations.size());
            body.put("reservations", reservations);

            writeJson(response, HttpServletResponse.SC_OK, body);

        } catch (SQLException e) {
            log("Reservation lookup database error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("Reservations could not be loaded."));
        }
    }

    private static Map<String, Object> toReservation(ResultSet result)
            throws SQLException {

        Map<String, Object> reservation = new LinkedHashMap<>();
        reservation.put("id", result.getString("reservation_id"));
        reservation.put("email", result.getString("email"));
        reservation.put("boatName", result.getString("boat_name"));
        reservation.put("boatLength", result.getDouble("boat_length_ft"));
        reservation.put("slipSize", result.getInt("slip_size_ft"));
        reservation.put("slipNumber", result.getString("slip_number"));
        reservation.put("checkIn", result.getDate("check_in_date").toLocalDate().toString());
        reservation.put("monthlyCost", result.getDouble("monthly_cost"));
        reservation.put("status", result.getString("status"));

        if (result.getTimestamp("created_at") != null) {
            reservation.put("createdAt",
                    result.getTimestamp("created_at").toInstant().toString());
        }

        if (result.getTimestamp("cancelled_at") != null) {
            reservation.put("cancelledAt",
                    result.getTimestamp("cancelled_at").toInstant().toString());
        }

        return reservation;
    }

    private static Long authenticatedCustomerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("customerId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
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