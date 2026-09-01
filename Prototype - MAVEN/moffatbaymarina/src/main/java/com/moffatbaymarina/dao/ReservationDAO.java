package com.moffatbaymarina.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.moffatbaymarina.config.DatabaseConnection;

/**
 * Data-access methods for the reservations table.
 */
public class ReservationDAO {

    public void insertReservation(Connection connection,
            String reservationId,
            long customerId,
            long boatId,
            int slipSize,
            String slipNumber,
            LocalDate checkIn,
            double monthlyCost,
            String status)
            throws SQLException {

        String sql = """
                INSERT INTO reservations
                (reservation_id, customer_id, boat_id, slip_size_ft,
                 slip_number, check_in_date, monthly_cost, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationId);
            statement.setLong(2, customerId);
            statement.setLong(3, boatId);
            statement.setInt(4, slipSize);
            statement.setString(5, slipNumber);
            statement.setDate(6, Date.valueOf(checkIn));
            statement.setDouble(7, monthlyCost);
            statement.setString(8, status);

            int changed = statement.executeUpdate();
            if (changed != 1) {
                throw new SQLException("Reservation insert did not create exactly one row.");
            }
        }
    }

    public boolean reservationIdExists(Connection connection,
            String reservationId)
            throws SQLException {

        String sql = "SELECT 1 FROM reservations WHERE reservation_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public int countActiveBySlipSize(Connection connection, int slipSize)
            throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM reservations
                WHERE slip_size_ft = ?
                  AND status <> 'Cancelled'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, slipSize);

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    public boolean updateStatus(String reservationId,
            long customerId,
            String status)
            throws SQLException {

        if (!"Confirmed".equals(status) && !"Cancelled".equals(status)) {
            throw new IllegalArgumentException(
                    "Reservation status must be Confirmed or Cancelled.");
        }

        String sql;
        if ("Cancelled".equals(status)) {
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

            return statement.executeUpdate() == 1;
        }
    }

    public ReservationRecord findByIdForCustomer(String reservationId,
            long customerId)
            throws SQLException {

        List<ReservationRecord> matches = search(
                customerId,
                reservationId,
                null);

        return matches.isEmpty() ? null : matches.get(0);
    }

    public List<ReservationRecord> findAllForCustomer(long customerId)
            throws SQLException {
        return search(customerId, null, null);
    }

    /**
     * Searches reservation history for one authenticated customer.
     * reservationId and email are optional filters.
     */
    public List<ReservationRecord> search(long customerId,
            String reservationId,
            String email)
            throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT r.reservation_id,
                       r.customer_id,
                       r.boat_id,
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

        boolean filterId = reservationId != null && !reservationId.isBlank();
        boolean filterEmail = email != null && !email.isBlank();

        if (filterId) {
            sql.append(" AND LOWER(r.reservation_id) = LOWER(?) ");
        }

        if (filterEmail) {
            sql.append(" AND LOWER(c.email) = LOWER(?) ");
        }

        sql.append(" ORDER BY r.created_at DESC ");

        List<ReservationRecord> reservations = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int index = 1;
            statement.setLong(index++, customerId);

            if (filterId) {
                statement.setString(index++, reservationId.trim());
            }

            if (filterEmail) {
                statement.setString(index, email.trim());
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    reservations.add(mapReservation(result));
                }
            }
        }

        return reservations;
    }

    private static ReservationRecord mapReservation(ResultSet result)
            throws SQLException {

        Date checkInDate = result.getDate("check_in_date");
        Timestamp createdAt = result.getTimestamp("created_at");
        Timestamp cancelledAt = result.getTimestamp("cancelled_at");

        return new ReservationRecord(
                result.getString("reservation_id"),
                result.getLong("customer_id"),
                result.getLong("boat_id"),
                result.getString("email"),
                result.getString("boat_name"),
                result.getDouble("boat_length_ft"),
                result.getInt("slip_size_ft"),
                result.getString("slip_number"),
                checkInDate == null ? null : checkInDate.toLocalDate(),
                result.getDouble("monthly_cost"),
                result.getString("status"),
                createdAt == null ? null : createdAt.toInstant(),
                cancelledAt == null ? null : cancelledAt.toInstant());
    }

    public record ReservationRecord(String reservationId,
            long customerId,
            long boatId,
            String email,
            String boatName,
            double boatLength,
            int slipSize,
            String slipNumber,
            LocalDate checkIn,
            double monthlyCost,
            String status,
            Instant createdAt,
            Instant cancelledAt) {
    }
}
