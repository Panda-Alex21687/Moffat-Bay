package com.moffatbaymarina.dao;

import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.model.SlipType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SlipTypeDAO {
    public SlipType findById(long slipTypeId) throws SQLException {
        String sql = "SELECT * FROM slip_types WHERE slip_type_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, slipTypeId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public SlipType findRequiredForBoatLength(BigDecimal boatLengthFt) throws SQLException {
        String sql = """
                SELECT * FROM slip_types
                WHERE size_ft >= ?
                ORDER BY size_ft
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, boatLengthFt);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public List<SlipType> findAll() throws SQLException {
        String sql = "SELECT * FROM slip_types ORDER BY size_ft";
        List<SlipType> list = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                list.add(map(result));
            }
        }
        return list;
    }

    private SlipType map(ResultSet result) throws SQLException {
        return new SlipType(
                result.getLong("slip_type_id"),
                result.getBigDecimal("size_ft"),
                result.getInt("total_capacity"),
                result.getBigDecimal("rate_per_foot"),
                result.getBigDecimal("electric_fee")
        );
    }
}
