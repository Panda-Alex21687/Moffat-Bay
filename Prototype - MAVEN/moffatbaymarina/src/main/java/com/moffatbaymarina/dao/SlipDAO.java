package com.moffatbaymarina.dao;

import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.model.Slip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SlipDAO {
    public Slip findById(long slipId) throws SQLException {
        String sql = "SELECT * FROM slips WHERE slip_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, slipId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public Slip findBySlipNumber(String slipNumber) throws SQLException {
        String sql = "SELECT * FROM slips WHERE slip_number = ? LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, slipNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public List<Slip> findBySlipType(long slipTypeId) throws SQLException {
        String sql = "SELECT * FROM slips WHERE slip_type_id = ? ORDER BY slip_number";
        List<Slip> slips = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, slipTypeId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    slips.add(map(result));
                }
            }
        }
        return slips;
    }

    public Slip findAvailableForUpdate(Connection connection, long slipTypeId)
            throws SQLException {
        String sql = """
                SELECT * FROM slips
                WHERE slip_type_id = ? AND UPPER(status) = 'AVAILABLE'
                ORDER BY slip_number
                LIMIT 1
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, slipTypeId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public int countAvailable(long slipTypeId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM slips
                WHERE slip_type_id = ? AND UPPER(status) = 'AVAILABLE'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, slipTypeId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    public boolean updateStatus(Connection connection, long slipId, String status)
            throws SQLException {
        String sql = "UPDATE slips SET status = ? WHERE slip_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, slipId);
            return statement.executeUpdate() == 1;
        }
    }

    private Slip map(ResultSet result) throws SQLException {
        return new Slip(
                result.getLong("slip_id"),
                result.getLong("slip_type_id"),
                result.getString("slip_number"),
                result.getString("status")
        );
    }
}
