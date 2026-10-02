package com.example.demo.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.model.WaitlistEntry;

@Repository
public class WaitlistRepository {

    private final JdbcTemplate jdbcTemplate;

    public WaitlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public WaitlistEntry insert(WaitlistEntry entry) {

        String sql = """
                INSERT INTO waitlist_entries
                (customer_id, boat_id, slip_type_id, status)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);

            statement.setLong(1, entry.getCustomerId());
            statement.setLong(2, entry.getBoatId());
            statement.setLong(3, entry.getSlipTypeId());
            statement.setString(4, entry.getStatus());

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("No waitlist_id was generated.");
        }

        entry.setWaitlistId(key.longValue());

        return entry;
    }
    
    public boolean hasActiveEntry(long customerId, long boatId, long slipTypeId) {

        String sql = """
                SELECT COUNT(*)
                FROM waitlist_entries
                WHERE customer_id = ?
                  AND boat_id = ?
                  AND slip_type_id = ?
                  AND UPPER(status) IN ('WAITING', 'CONTACTED')
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                customerId,
                boatId,
                slipTypeId
        );

        return count != null && count > 0;
    }
    
    public List<WaitlistEntry> findByCustomerId(long customerId) {

        String sql = """
                SELECT waitlist_id, customer_id, boat_id,
                       slip_type_id, joined_at, status
                FROM waitlist_entries
                WHERE customer_id = ?
                ORDER BY joined_at, waitlist_id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    WaitlistEntry entry = new WaitlistEntry();

                    entry.setWaitlistId(rs.getLong("waitlist_id"));
                    entry.setCustomerId(rs.getLong("customer_id"));
                    entry.setBoatId(rs.getLong("boat_id"));
                    entry.setSlipTypeId(rs.getLong("slip_type_id"));

                    Timestamp joined = rs.getTimestamp("joined_at");
                    entry.setJoinedAt(
                            joined == null ? null : joined.toLocalDateTime()
                    );

                    entry.setStatus(rs.getString("status"));

                    return entry;
                },
                customerId
        );
    }
    
    public int getPosition(WaitlistEntry entry) {

        if (entry.getJoinedAt() == null) {
            return 0;
        }

        String sql = """
                SELECT COUNT(*)
                FROM waitlist_entries
                WHERE slip_type_id = ?
                  AND UPPER(status) = 'WAITING'
                  AND (
                        joined_at < ?
                        OR (joined_at = ? AND waitlist_id <= ?)
                      )
                """;

        Timestamp joined = Timestamp.valueOf(entry.getJoinedAt());

        Integer position = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                entry.getSlipTypeId(),
                joined,
                joined,
                entry.getWaitlistId()
        );

        return position == null ? 0 : position;
    }
    
    public int countWaiting(long slipTypeId) {

        String sql = """
                SELECT COUNT(*)
                FROM waitlist_entries
                WHERE slip_type_id = ?
                  AND UPPER(status) = 'WAITING'
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                slipTypeId
        );

        return count == null ? 0 : count;
    }
}