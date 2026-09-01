package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moffatbaymarina.dao.SlipDAO;
import com.moffatbaymarina.dao.SlipTypeDAO;
import com.moffatbaymarina.model.SlipType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/availability")
public class AvailabilityServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareJson(response);
        BigDecimal boatLength = parseDecimal(request.getParameter("boatLength"));
        SlipTypeDAO slipTypeDAO = new SlipTypeDAO();
        SlipDAO slipDAO = new SlipDAO();

        try {
            List<Map<String, Object>> availability = new ArrayList<>();
            for (SlipType slipType : slipTypeDAO.findAll()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("slipTypeId", slipType.getSlipTypeId());
                row.put("sizeFt", slipType.getSizeFt());
                row.put("totalCapacity", slipType.getTotalCapacity());
                row.put("ratePerFoot", slipType.getRatePerFoot());
                row.put("electricFee", slipType.getElectricFee());
                row.put("availableCount", slipDAO.countAvailable(slipType.getSlipTypeId()));
                availability.add(row);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("slipTypes", availability);

            if (boatLength != null && boatLength.compareTo(BigDecimal.ZERO) > 0) {
                SlipType required = slipTypeDAO.findRequiredForBoatLength(boatLength);
                if (required == null) {
                    body.put("requiredSlipType", null);
                    body.put("message", "No slip type can accommodate this boat.");
                } else {
                    BigDecimal estimatedCost = boatLength.multiply(required.getRatePerFoot())
                            .add(required.getElectricFee())
                            .setScale(2, RoundingMode.HALF_UP);
                    Map<String, Object> requiredMap = new LinkedHashMap<>();
                    requiredMap.put("slipTypeId", required.getSlipTypeId());
                    requiredMap.put("sizeFt", required.getSizeFt());
                    requiredMap.put("availableCount",
                            slipDAO.countAvailable(required.getSlipTypeId()));
                    requiredMap.put("estimatedMonthlyCost", estimatedCost);
                    body.put("requiredSlipType", requiredMap);
                }
            }

            writeJson(response, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log("Availability lookup error", e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    error("Slip availability could not be loaded."));
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try { return new BigDecimal(value.trim()); }
        catch (NumberFormatException e) { return null; }
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
