package com.moffatbaymarina.dao;

import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.model.Reservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    public Reservation insert(Connection connection, Reservation reservation) throws SQLException {
        String sql = """
                INSERT INTO reservations
                (customer_id, boat_id, slip_id, check_in_date,
                 expected_term, monthly_cost, status, cancelled_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, reservation.getCustomerId());
            statement.setLong(2, reservation.getBoatId());
            statement.setLong(3, reservation.getSlipId());
            statement.setDate(4, Date.valueOf(reservation.getCheckInDate()));
            statement.setString(5, reservation.getExpectedTerm());
            statement.setBigDecimal(6, reservation.getMonthlyCost());
            statement.setString(7, reservation.getStatus());
            if (reservation.getCancelledAt() == null) {
                statement.setNull(8, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(8, Timestamp.valueOf(reservation.getCancelledAt()));
            }
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Reservation insert did not create one row.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No reservation_id was generated.");
                }
                reservation.setReservationId(keys.getLong(1));
            }
        }
        return reservation;
    }

    public Reservation findById(long reservationId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reservationId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public Reservation findByIdForUpdate(Connection connection, long reservationId)
            throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reservationId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public List<Reservation> findByCustomerId(long customerId) throws SQLException {
        String sql = """
                SELECT * FROM reservations
                WHERE customer_id = ?
                ORDER BY created_at DESC, reservation_id DESC
                """;
        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    reservations.add(map(result));
                }
            }
        }
        return reservations;
    }

    public List<Reservation> search(Long reservationId, String email) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT r.* FROM reservations r
                JOIN customers c ON c.customer_id = r.customer_id
                WHERE 1 = 1
                """);
        if (reservationId != null) {
            sql.append(" AND r.reservation_id = ?");
        }
        if (email != null && !email.isBlank()) {
            sql.append(" AND LOWER(c.email) = LOWER(?)");
        }
        sql.append(" ORDER BY r.created_at DESC, r.reservation_id DESC");

        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int parameterIndex = 1;

            if (reservationId != null) {
                statement.setLong(parameterIndex++, reservationId);
            }

            if (email != null && !email.isBlank()) {
                statement.setString(parameterIndex, email.trim());
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    reservations.add(map(result));
                }
            }
        }
        return reservations;
    }

    public boolean updateStatus(Connection connection, long reservationId,
                                String status, LocalDateTime cancelledAt) throws SQLException {
        String sql = "UPDATE reservations SET status = ?, cancelled_at = ? WHERE reservation_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            if (cancelledAt == null) statement.setNull(2, java.sql.Types.TIMESTAMP);
            else statement.setTimestamp(2, Timestamp.valueOf(cancelledAt));
            statement.setLong(3, reservationId);
            return statement.executeUpdate() == 1;
        }
    }

    private Reservation map(ResultSet result) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(result.getLong("reservation_id"));
        reservation.setCustomerId(result.getLong("customer_id"));
        reservation.setBoatId(result.getLong("boat_id"));
        reservation.setSlipId(result.getLong("slip_id"));
        Date checkIn = result.getDate("check_in_date");
        reservation.setCheckInDate(checkIn == null ? null : checkIn.toLocalDate());
        reservation.setExpectedTerm(result.getString("expected_term"));
        reservation.setMonthlyCost(result.getBigDecimal("monthly_cost"));
        reservation.setStatus(result.getString("status"));
        Timestamp created = result.getTimestamp("created_at");
        reservation.setCreatedAt(created == null ? null : created.toLocalDateTime());
        Timestamp cancelled = result.getTimestamp("cancelled_at");
        reservation.setCancelledAt(cancelled == null ? null : cancelled.toLocalDateTime());
        return reservation;
    }
}
