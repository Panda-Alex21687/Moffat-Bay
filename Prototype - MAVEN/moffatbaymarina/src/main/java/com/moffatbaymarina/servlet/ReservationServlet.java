package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.dao.BoatDAO;
import com.moffatbaymarina.dao.ReservationDAO;
import com.moffatbaymarina.dao.SlipDAO;
import com.moffatbaymarina.dao.SlipTypeDAO;
import com.moffatbaymarina.model.Boat;
import com.moffatbaymarina.model.Reservation;
import com.moffatbaymarina.model.Slip;
import com.moffatbaymarina.model.SlipType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/reservation")
public class ReservationServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareJson(response);
        Long customerId = authenticatedCustomerId(request);
        if (customerId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    error("Log in before creating a reservation."));
            return;
        }

        JsonNode data = readJson(request, response);
        if (data == null) return;
        Long boatId = longValue(data, "boatId");
        String boatName = text(data, "boatName");
        String checkInValue = text(data, "checkInDate");
        if (checkInValue.isBlank()) checkInValue = text(data, "checkIn");
        String expectedTerm = text(data, "expectedTerm");

        if ((boatId == null && boatName.isBlank()) || checkInValue.isBlank()
                || expectedTerm.isBlank()) {
            writeJson(response, 422,
                    error("Boat, check-in date, and expected term are required."));
            return;
        }

        LocalDate checkInDate;
        try { checkInDate = LocalDate.parse(checkInValue); }
        catch (DateTimeParseException e) {
            writeJson(response, 422, error("Enter a valid check-in date."));
            return;
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            writeJson(response, 422, error("Check-in date cannot be in the past."));
            return;
        }

        BoatDAO boatDAO = new BoatDAO();
        SlipTypeDAO slipTypeDAO = new SlipTypeDAO();
        SlipDAO slipDAO = new SlipDAO();
        ReservationDAO reservationDAO = new ReservationDAO();

        try {
            Boat boat = boatId != null
                    ? boatDAO.findForCustomer(boatId, customerId)
                    : boatDAO.findByCustomerAndName(customerId, boatName);
            if (boat == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        error("The selected boat was not found."));
                return;
            }

            SlipType slipType = slipTypeDAO.findRequiredForBoatLength(boat.getBoatLengthFt());
            if (slipType == null) {
                writeJson(response, 422, error("No slip type can accommodate this boat."));
                return;
            }

            BigDecimal monthlyCost = boat.getBoatLengthFt()
                    .multiply(slipType.getRatePerFoot())
                    .add(slipType.getElectricFee())
                    .setScale(2, RoundingMode.HALF_UP);

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    Slip slip = slipDAO.findAvailableForUpdate(
                            connection, slipType.getSlipTypeId());
                    if (slip == null) {
                        connection.rollback();
                        Map<String, Object> body = error(
                                "No matching slips are currently available.");
                        body.put("waitlistRecommended", true);
                        body.put("slipTypeId", slipType.getSlipTypeId());
                        body.put("slipSizeFt", slipType.getSizeFt());
                        writeJson(response, HttpServletResponse.SC_CONFLICT, body);
                        return;
                    }

                    Reservation reservation = new Reservation();
                    reservation.setCustomerId(customerId);
                    reservation.setBoatId(boat.getBoatId());
                    reservation.setSlipId(slip.getSlipId());
                    reservation.setCheckInDate(checkInDate);
                    reservation.setExpectedTerm(expectedTerm);
                    reservation.setMonthlyCost(monthlyCost);
                    reservation.setStatus("PENDING");
                    reservationDAO.insert(connection, reservation);
                    // Matches the supplied seed pattern: PENDING reservation -> HELD slip.
                    slipDAO.updateStatus(connection, slip.getSlipId(), "HELD");
                    connection.commit();

                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("ok", true);
                    body.put("message", "Reservation created.");
                    body.put("reservation", reservationMap(reservation, boat, slip, slipType));
                    body.put("redirect", "reservation-summary.html?id="
                            + reservation.getReservationId());
                    writeJson(response, HttpServletResponse.SC_CREATED, body);
                } catch (SQLException | RuntimeException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        } catch (SQLException | RuntimeException e) {
            log("Reservation creation error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The reservation could not be created."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareJson(response);
        Long customerId = authenticatedCustomerId(request);
        if (customerId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    error("Log in before updating a reservation."));
            return;
        }

        JsonNode data = readJson(request, response);
        if (data == null) return;
        Long reservationId = longValue(data, "reservationId");
        if (reservationId == null) reservationId = longValue(data, "id");
        String status = text(data, "status").toUpperCase();
        if (reservationId == null
                || (!status.equals("CONFIRMED") && !status.equals("CANCELLED"))) {
            writeJson(response, 422,
                    error("Reservation ID and a CONFIRMED or CANCELLED status are required."));
            return;
        }

        ReservationDAO reservationDAO = new ReservationDAO();
        SlipDAO slipDAO = new SlipDAO();
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Reservation reservation = reservationDAO.findByIdForUpdate(connection, reservationId);
                if (reservation == null || reservation.getCustomerId() != customerId) {
                    connection.rollback();
                    writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                            error("Reservation not found."));
                    return;
                }
                LocalDateTime cancelledAt = status.equals("CANCELLED")
                        ? LocalDateTime.now() : null;
                reservationDAO.updateStatus(connection, reservationId, status, cancelledAt);
                String slipStatus = status.equals("CONFIRMED") ? "RESERVED" : "AVAILABLE";
                slipDAO.updateStatus(connection, reservation.getSlipId(), slipStatus);
                connection.commit();

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("ok", true);
                body.put("message", "Reservation status updated.");
                body.put("reservationId", reservationId);
                body.put("status", status);
                body.put("slipStatus", slipStatus);
                writeJson(response, HttpServletResponse.SC_OK, body);
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException | RuntimeException e) {
            log("Reservation update error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The reservation could not be updated."));
        }
    }

    private Map<String, Object> reservationMap(Reservation reservation, Boat boat,
                                                Slip slip, SlipType slipType) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reservationId", reservation.getReservationId());
        map.put("customerId", reservation.getCustomerId());
        map.put("boatId", reservation.getBoatId());
        map.put("boatName", boat.getBoatName());
        map.put("boatLengthFt", boat.getBoatLengthFt());
        map.put("slipId", slip.getSlipId());
        map.put("slipNumber", slip.getSlipNumber());
        map.put("slipTypeId", slipType.getSlipTypeId());
        map.put("slipSizeFt", slipType.getSizeFt());
        map.put("checkInDate", reservation.getCheckInDate().toString());
        map.put("expectedTerm", reservation.getExpectedTerm());
        map.put("monthlyCost", reservation.getMonthlyCost());
        map.put("status", reservation.getStatus());
        return map;
    }

    private Long authenticatedCustomerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("customerId");
        return value instanceof Number number ? number.longValue() : null;
    }

    private JsonNode readJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try { return JSON.readTree(request.getInputStream()); }
        catch (IOException e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    error("Invalid JSON request body."));
            return null;
        }
    }

    private Long longValue(JsonNode data, String field) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull() || node.asText("").isBlank()) return null;
        try { return Long.valueOf(node.asText()); }
        catch (NumberFormatException e) { return null; }
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
