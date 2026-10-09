package com.example.demo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SlipRepository {

    private final JdbcTemplate jdbcTemplate;

    public SlipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countAvailable(long slipTypeId) {

        String sql = """
                SELECT COUNT(*)
                FROM slips
                WHERE slip_type_id = ?
                  AND UPPER(status) = 'AVAILABLE'
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                slipTypeId
        );

        return count == null ? 0 : count;
    }
}