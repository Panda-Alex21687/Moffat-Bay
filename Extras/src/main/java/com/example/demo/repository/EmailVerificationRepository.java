package com.example.demo.repository;

import com.example.demo.model.EmailVerification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class EmailVerificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmailVerificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long save(EmailVerification verification) {

    	    String sql = """
    	        INSERT INTO email_verifications
    	        (customer_id, token_hash, expires_at, verified_at)
    	        VALUES (?, ?, ?, ?)
    	        """;

    	    KeyHolder keyHolder = new GeneratedKeyHolder();

    	    jdbcTemplate.update(connection -> {
    	        var statement = connection.prepareStatement(
    	            sql,
    	            java.sql.Statement.RETURN_GENERATED_KEYS
    	        );

    	        statement.setLong(1, verification.getCustomerId());
    	        statement.setString(2, verification.getTokenHash());
    	        statement.setObject(3, verification.getExpiresAt());
    	        statement.setObject(4, verification.getVerifiedAt());

    	        return statement;
    	    }, keyHolder);

    	    Number key = keyHolder.getKey();

    	    if (key == null) {
    	        throw new IllegalStateException(
    	            "Email verification was inserted, but no verification ID was returned."
    	        );
    	    }

    	    return key.longValue();
    }
    public EmailVerification findValid(String rawToken, String hashedToken) {

        String sql = """
                SELECT *
                FROM email_verifications
                WHERE (token_hash = ? OR token_hash = ?)
                  AND verified_at IS NULL
                  AND expires_at >= CURRENT_TIMESTAMP
                ORDER BY verification_id DESC
                LIMIT 1
                """;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {

                EmailVerification verification = new EmailVerification();

                verification.setVerificationId(
                        rs.getLong("verification_id"));

                verification.setCustomerId(
                        rs.getLong("customer_id"));

                verification.setTokenHash(
                        rs.getString("token_hash"));

                verification.setExpiresAt(
                        rs.getTimestamp("expires_at").toLocalDateTime());

                if (rs.getTimestamp("verified_at") != null) {
                    verification.setVerifiedAt(
                            rs.getTimestamp("verified_at").toLocalDateTime());
                }

                if (rs.getTimestamp("created_at") != null) {
                    verification.setCreatedAt(
                            rs.getTimestamp("created_at").toLocalDateTime());
                }

                return verification;
            }

            return null;
        }, hashedToken, rawToken);
    }
    public int markVerified(long verificationId) {

        String sql = """
                UPDATE email_verifications
                SET verified_at = CURRENT_TIMESTAMP
                WHERE verification_id = ?
                  AND verified_at IS NULL
                """;

        return jdbcTemplate.update(sql, verificationId);
    }
}