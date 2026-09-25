package com.example.demo;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class LookupReservationController {

    private final JdbcTemplate jdbcTemplate;

    public LookupReservationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/lookup-reservation")
    public String lookupReservation(
            @RequestParam("reservationId") int reservationId,
            @RequestParam("email") String email,
            HttpSession session) {

        String sql = """
            SELECT
                r.reservation_id,
                r.check_in_date,
                r.expected_term,
                r.monthly_cost,
                r.status,
                s.slip_number,
                c.first_name,
                c.last_name,
                c.email
            FROM reservations r
            JOIN customers c ON r.customer_id = c.customer_id
            JOIN slips s ON r.slip_id = s.slip_id
            WHERE r.reservation_id = ?
              AND LOWER(c.email) = LOWER(?)
            """;

        try {
            Map<String, Object> reservation =
                    jdbcTemplate.queryForMap(sql, reservationId, email);

            session.setAttribute("lookupReservation", reservation);

        } catch (Exception e) {
            session.setAttribute(
                    "lookupReservationError",
                    "No reservation was found with the information provided.");

            session.removeAttribute("lookupReservation");
        }

        return "redirect:/reservation-lookup-result.html";
    }

    @GetMapping("/lookup-reservation-result")
    @ResponseBody
    public ResponseEntity<?> getLookupReservationResult(HttpSession session) {

        Object error = session.getAttribute("lookupReservationError");

        if (error != null) {
            session.removeAttribute("lookupReservationError");
            return ResponseEntity.ok(
                    Map.of("error", error.toString())
            );
        }

        Object reservation = session.getAttribute("lookupReservation");

        if (reservation == null) {
            return ResponseEntity.ok(
                    Map.of(
                        "error",
                        "No reservation information is currently available."
                    )
            );
        }

        session.removeAttribute("lookupReservation");

        return ResponseEntity.ok(reservation);
    }
}