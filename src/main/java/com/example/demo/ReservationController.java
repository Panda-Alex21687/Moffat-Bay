package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Controller
public class ReservationController {

    private final DataSource dataSource;

    public ReservationController(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    @PostMapping("/reserve-slip")
    public String reserveSlip(
            @RequestParam("slip") String slipNumber,
            @RequestParam("arrival") String arrivalDate,
            @RequestParam("departure") String departureDate,
            HttpSession session) {

        Object customerIdObject = session.getAttribute("customer_id");

        if (customerIdObject == null) {
            return "redirect:/login.html";
        }

        int customerId = (Integer) customerIdObject;

        try {
            java.time.LocalDate arrival =
                    java.time.LocalDate.parse(arrivalDate);

            java.time.LocalDate departure =
                    java.time.LocalDate.parse(departureDate);

            if (!arrival.isBefore(departure)) {
                return "redirect:/reserve-slip.html?slip="
                        + slipNumber + "&error=dates";
            }

            String slipSql = """
                    SELECT s.slip_id,
                           s.status,
                           st.size_ft,
                           st.rate_per_foot,
                           st.electric_fee
                    FROM slips s
                    JOIN slip_types st
                      ON s.slip_type_id = st.slip_type_id
                    WHERE s.slip_number = ?
                    """;

            int slipId;
            String status;
            double sizeFt;
            double ratePerFoot;
            double electricFee;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(slipSql)) {

                statement.setString(1, slipNumber);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (!resultSet.next()) {
                        return "redirect:/reserve-slip.html?slip="
                                + slipNumber + "&error=invalid";
                    }

                    slipId = resultSet.getInt("slip_id");
                    status = resultSet.getString("status");
                    sizeFt = resultSet.getDouble("size_ft");
                    ratePerFoot =
                            resultSet.getDouble("rate_per_foot");
                    electricFee =
                            resultSet.getDouble("electric_fee");
                }
            }

            if (!"AVAILABLE".equalsIgnoreCase(status)) {
                return "redirect:/reserve-slip.html?slip="
                        + slipNumber + "&error=unavailable";
            }

            int boatId;

            String boatSql = """
                    SELECT boat_id
                    FROM boats
                    WHERE customer_id = ?
                    ORDER BY boat_id
                    LIMIT 1
                    """;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(boatSql)) {

                statement.setInt(1, customerId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (!resultSet.next()) {
                        return "redirect:/reserve-slip.html?slip="
                                + slipNumber + "&error=noboat";
                    }

                    boatId = resultSet.getInt("boat_id");
                }
            }

            double monthlyCost =
                    (sizeFt * ratePerFoot) + electricFee;

            String expectedTerm = "Monthly";

            String reservationSql = """
                    INSERT INTO reservations
                    (customer_id, boat_id, slip_id, check_in_date,
                     expected_term, monthly_cost, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(reservationSql)) {

                statement.setInt(1, customerId);
                statement.setInt(2, boatId);
                statement.setInt(3, slipId);
                statement.setDate(
                        4,
                        java.sql.Date.valueOf(arrival)
                );
                statement.setString(5, expectedTerm);
                statement.setDouble(6, monthlyCost);
                statement.setString(7, "ACTIVE");

                statement.executeUpdate();
            }

            String updateSlipSql = """
                    UPDATE slips
                    SET status = 'RESERVED'
                    WHERE slip_id = ?
                    AND status = 'AVAILABLE'
                    """;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(updateSlipSql)) {

                statement.setInt(1, slipId);
                statement.executeUpdate();
            }

            return "redirect:/reservation-confirmation.html?slip="
                    + slipNumber;

        } catch (Exception e) {
            e.printStackTrace();

            return "redirect:/reserve-slip.html?slip="
                    + slipNumber + "&error=server";
        }
    }
}