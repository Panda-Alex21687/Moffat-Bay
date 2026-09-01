package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.dao.BoatDAO;
import com.moffatbaymarina.dao.SlipTypeDAO;
import com.moffatbaymarina.dao.WaitlistDAO;
import com.moffatbaymarina.model.Boat;
import com.moffatbaymarina.model.SlipType;
import com.moffatbaymarina.model.WaitlistEntry;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/waitlist")
public class WaitlistServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareJson(response);
        Long customerId = authenticatedCustomerId(request);
        if (customerId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    error("Log in to view your waitlist entries."));
            return;
        }

        try {
            WaitlistDAO waitlistDAO = new WaitlistDAO();
            BoatDAO boatDAO = new BoatDAO();
            SlipTypeDAO slipTypeDAO = new SlipTypeDAO();
            List<Map<String, Object>> results = new ArrayList<>();
            for (WaitlistEntry entry : waitlistDAO.findByCustomerId(customerId)) {
                Boat boat = boatDAO.findById(entry.getBoatId());
                SlipType slipType = slipTypeDAO.findById(entry.getSlipTypeId());
                Map<String, Object> row = waitlistMap(entry, boat, slipType);
                row.put("position", waitlistDAO.getPosition(entry));
                results.add(row);
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("entries", results);
            writeJson(response, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log("Waitlist lookup error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("Waitlist information could not be loaded."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareJson(response);
        Long customerId = authenticatedCustomerId(request);
        if (customerId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    error("Log in before joining the waitlist."));
            return;
        }

        JsonNode data = readJson(request, response);
        if (data == null) return;
        Long boatId = longValue(data, "boatId");
        String boatName = text(data, "boatName");
        Long slipTypeId = longValue(data, "slipTypeId");
        BigDecimal boatLength = decimal(data, "boatLength");

        BoatDAO boatDAO = new BoatDAO();
        SlipTypeDAO slipTypeDAO = new SlipTypeDAO();
        WaitlistDAO waitlistDAO = new WaitlistDAO();

        try {
            Boat boat;
            if (boatId != null) boat = boatDAO.findForCustomer(boatId, customerId);
            else if (!boatName.isBlank()) boat = boatDAO.findByCustomerAndName(customerId, boatName);
            else {
                writeJson(response, 422, error("Select a boat before joining the waitlist."));
                return;
            }
            if (boat == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND, error("Boat not found."));
                return;
            }

            SlipType slipType;
            if (slipTypeId != null) slipType = slipTypeDAO.findById(slipTypeId);
            else {
                BigDecimal length = boatLength != null ? boatLength : boat.getBoatLengthFt();
                slipType = slipTypeDAO.findRequiredForBoatLength(length);
            }
            if (slipType == null) {
                writeJson(response, 422, error("No matching slip type was found."));
                return;
            }

            if (waitlistDAO.hasActiveEntry(customerId, boat.getBoatId(), slipType.getSlipTypeId())) {
                writeJson(response, HttpServletResponse.SC_CONFLICT,
                        error("An active waitlist entry already exists for this boat and slip size."));
                return;
            }

            WaitlistEntry entry = new WaitlistEntry();
            entry.setCustomerId(customerId);
            entry.setBoatId(boat.getBoatId());
            entry.setSlipTypeId(slipType.getSlipTypeId());
            entry.setStatus("WAITING");
            waitlistDAO.insert(entry);

            WaitlistEntry saved = waitlistDAO.findByCustomerId(customerId).stream()
                    .filter(item -> item.getWaitlistId() == entry.getWaitlistId())
                    .findFirst().orElse(entry);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("message", "You have been added to the waitlist.");
            body.put("entry", waitlistMap(saved, boat, slipType));
            body.put("position", waitlistDAO.getPosition(saved));
            writeJson(response, HttpServletResponse.SC_CREATED, body);
        } catch (SQLException e) {
            log("Waitlist creation error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("The waitlist entry could not be created."));
        }
    }

    private Map<String, Object> waitlistMap(WaitlistEntry entry, Boat boat, SlipType slipType) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("waitlistId", entry.getWaitlistId());
        map.put("customerId", entry.getCustomerId());
        map.put("boatId", entry.getBoatId());
        map.put("boatName", boat == null ? null : boat.getBoatName());
        map.put("boatLengthFt", boat == null ? null : boat.getBoatLengthFt());
        map.put("slipTypeId", entry.getSlipTypeId());
        map.put("slipSizeFt", slipType == null ? null : slipType.getSizeFt());
        map.put("joinedAt", entry.getJoinedAt() == null ? null : entry.getJoinedAt().toString());
        map.put("status", entry.getStatus());
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

    private BigDecimal decimal(JsonNode data, String field) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull()) return null;
        try { return new BigDecimal(node.asText().trim()); }
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
