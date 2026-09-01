package com.moffatbaymarina.dao;

import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.model.WaitlistEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WaitlistDAO {
    public WaitlistEntry insert(WaitlistEntry entry) throws SQLException {
        String sql = """
                INSERT INTO waitlist_entries
                (customer_id, boat_id, slip_type_id, status)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, entry.getCustomerId());
            statement.setLong(2, entry.getBoatId());
            statement.setLong(3, entry.getSlipTypeId());
            statement.setString(4, entry.getStatus());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Waitlist insert did not create one row.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No waitlist_id was generated.");
                entry.setWaitlistId(keys.getLong(1));
            }
        }
        return entry;
    }

    public boolean hasActiveEntry(long customerId, long boatId, long slipTypeId)
            throws SQLException {
        String sql = """
                SELECT waitlist_id FROM waitlist_entries
                WHERE customer_id = ? AND boat_id = ? AND slip_type_id = ?
                  AND UPPER(status) IN ('WAITING', 'CONTACTED')
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            statement.setLong(2, boatId);
            statement.setLong(3, slipTypeId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public List<WaitlistEntry> findByCustomerId(long customerId) throws SQLException {
        String sql = """
                SELECT * FROM waitlist_entries
                WHERE customer_id = ?
                ORDER BY joined_at, waitlist_id
                """;
        List<WaitlistEntry> entries = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) entries.add(map(result));
            }
        }
        return entries;
    }

    public int getPosition(WaitlistEntry entry) throws SQLException {
        if (entry.getJoinedAt() == null) return 0;
        String sql = """
                SELECT COUNT(*) FROM waitlist_entries
                WHERE slip_type_id = ? AND UPPER(status) = 'WAITING'
                  AND (joined_at < ? OR (joined_at = ? AND waitlist_id <= ?))
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp joined = Timestamp.valueOf(entry.getJoinedAt());
            statement.setLong(1, entry.getSlipTypeId());
            statement.setTimestamp(2, joined);
            statement.setTimestamp(3, joined);
            statement.setLong(4, entry.getWaitlistId());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private WaitlistEntry map(ResultSet result) throws SQLException {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setWaitlistId(result.getLong("waitlist_id"));
        entry.setCustomerId(result.getLong("customer_id"));
        entry.setBoatId(result.getLong("boat_id"));
        entry.setSlipTypeId(result.getLong("slip_type_id"));
        Timestamp joined = result.getTimestamp("joined_at");
        entry.setJoinedAt(joined == null ? null : joined.toLocalDateTime());
        entry.setStatus(result.getString("status"));
        return entry;
    }
}
