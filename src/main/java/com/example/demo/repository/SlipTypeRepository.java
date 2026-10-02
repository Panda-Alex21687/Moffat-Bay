package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.model.SlipType;

@Repository
public class SlipTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    public SlipTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SlipType findById(long slipTypeId) {

        String sql = """
                SELECT slip_type_id, size_ft, total_capacity,
                       rate_per_foot, electric_fee
                FROM slip_types
                WHERE slip_type_id = ?
                """;

        List<SlipType> slipTypes = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> map(rs),
                slipTypeId
        );

        return slipTypes.isEmpty() ? null : slipTypes.get(0);
    }

    public SlipType findRequiredForBoatLength(BigDecimal boatLengthFt) {

        String sql = """
                SELECT slip_type_id, size_ft, total_capacity,
                       rate_per_foot, electric_fee
                FROM slip_types
                WHERE size_ft >= ?
                ORDER BY size_ft
                LIMIT 1
                """;

        List<SlipType> slipTypes = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> map(rs),
                boatLengthFt
        );

        return slipTypes.isEmpty() ? null : slipTypes.get(0);
    }

    public List<SlipType> findAll() {

        String sql = """
                SELECT slip_type_id, size_ft, total_capacity,
                       rate_per_foot, electric_fee
                FROM slip_types
                ORDER BY size_ft
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> map(rs)
        );
    }

    private SlipType map(java.sql.ResultSet rs)
            throws java.sql.SQLException {

        return new SlipType(
                rs.getLong("slip_type_id"),
                rs.getBigDecimal("size_ft"),
                rs.getInt("total_capacity"),
                rs.getBigDecimal("rate_per_foot"),
                rs.getBigDecimal("electric_fee")
        );
    }
}