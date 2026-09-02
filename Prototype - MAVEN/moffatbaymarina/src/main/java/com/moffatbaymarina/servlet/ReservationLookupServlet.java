package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.dao.BoatDAO;
import com.moffatbaymarina.dao.CustomerDAO;
import com.moffatbaymarina.dao.ReservationDAO;
import com.moffatbaymarina.dao.SlipDAO;
import com.moffatbaymarina.dao.SlipTypeDAO;
import com.moffatbaymarina.model.Boat;
import com.moffatbaymarina.model.Customer;
import com.moffatbaymarina.model.Reservation;
import com.moffatbaymarina.model.Slip;
import com.moffatbaymarina.model.SlipType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/reservation-lookup")
public class ReservationLookupServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareJson(response);
        Long reservationId = parseLong(firstNonBlank(
                request.getParameter("reservationId"), request.getParameter("id")));
        String email = clean(request.getParameter("email"));
        Long sessionCustomerId = authenticatedCustomerId(request);

        try {
            ReservationDAO reservationDAO = new ReservationDAO();
            List<Reservation> reservations;
            if (reservationId != null || !email.isBlank()) {
                reservations = reservationDAO.search(reservationId, email);
            } else if (sessionCustomerId != null) {
                reservations = reservationDAO.findByCustomerId(sessionCustomerId);
            } else {
                writeJson(response, 422,
                        error("Enter a reservation ID or email address."));
                return;
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (Reservation reservation : reservations) results.add(details(reservation));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("count", results.size());
            body.put("reservations", results);
            writeJson(response, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log("Reservation lookup error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("Reservation lookup could not be completed."));
        }
    }

    private Map<String, Object> details(Reservation reservation) throws SQLException {
        Customer customer = new CustomerDAO().findById(reservation.getCustomerId());
        Boat boat = new BoatDAO().findById(reservation.getBoatId());
        Slip slip = new SlipDAO().findById(reservation.getSlipId());
        SlipType slipType = slip == null ? null
                : new SlipTypeDAO().findById(slip.getSlipTypeId());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reservationId", reservation.getReservationId());
        map.put("email", customer == null ? null : customer.getEmail());
        map.put("customerName", customer == null ? null
                : customer.getFirstName() + " " + customer.getLastName());
        map.put("boatName", boat == null ? null : boat.getBoatName());
        map.put("boatLengthFt", boat == null ? null : boat.getBoatLengthFt());
        map.put("slipNumber", slip == null ? null : slip.getSlipNumber());
        map.put("slipSizeFt", slipType == null ? null : slipType.getSizeFt());
        map.put("checkInDate", reservation.getCheckInDate() == null ? null
                : reservation.getCheckInDate().toString());
        map.put("expectedTerm", reservation.getExpectedTerm());
        map.put("monthlyCost", reservation.getMonthlyCost());
        map.put("status", reservation.getStatus());
        map.put("createdAt", reservation.getCreatedAt() == null ? null
                : reservation.getCreatedAt().toString());
        map.put("cancelledAt", reservation.getCancelledAt() == null ? null
                : reservation.getCancelledAt().toString());
        return map;
    }

    private Long authenticatedCustomerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("customerId");
        return value instanceof Number number ? number.longValue() : null;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Long.valueOf(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }

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
