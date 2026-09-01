package com.moffatbaymarina.dao;

import com.moffatbaymarina.config.DatabaseConnection;
import com.moffatbaymarina.model.EmailVerification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class EmailVerificationDAO {
    public EmailVerification insert(Connection connection, EmailVerification verification)
            throws SQLException {
        String sql = """
                INSERT INTO email_verifications
                (customer_id, token_hash, expires_at, verified_at)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, verification.getCustomerId());
            statement.setString(2, verification.getTokenHash());
            statement.setTimestamp(3, Timestamp.valueOf(verification.getExpiresAt()));
            if (verification.getVerifiedAt() == null) statement.setNull(4, java.sql.Types.TIMESTAMP);
            else statement.setTimestamp(4, Timestamp.valueOf(verification.getVerifiedAt()));
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Email verification insert failed.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No verification_id was generated.");
                verification.setVerificationId(keys.getLong(1));
            }
        }
        return verification;
    }

    /**
     * New registrations store SHA-256 token hashes. The supplied seed data
     * stores demonstration tokens as plain text, so both are accepted here.
     */
    public EmailVerification findValid(String rawToken, String hashedToken) throws SQLException {
        String sql = """
                SELECT * FROM email_verifications
                WHERE (token_hash = ? OR token_hash = ?)
                  AND verified_at IS NULL
                  AND expires_at >= CURRENT_TIMESTAMP
                ORDER BY verification_id DESC
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hashedToken);
            statement.setString(2, rawToken);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public boolean markVerified(Connection connection, long verificationId) throws SQLException {
        String sql = """
                UPDATE email_verifications
                SET verified_at = CURRENT_TIMESTAMP
                WHERE verification_id = ? AND verified_at IS NULL
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, verificationId);
            return statement.executeUpdate() == 1;
        }
    }

    private EmailVerification map(ResultSet result) throws SQLException {
        EmailVerification verification = new EmailVerification();
        verification.setVerificationId(result.getLong("verification_id"));
        verification.setCustomerId(result.getLong("customer_id"));
        verification.setTokenHash(result.getString("token_hash"));
        Timestamp expires = result.getTimestamp("expires_at");
        verification.setExpiresAt(expires == null ? null : expires.toLocalDateTime());
        Timestamp verified = result.getTimestamp("verified_at");
        verification.setVerifiedAt(verified == null ? null : verified.toLocalDateTime());
        Timestamp created = result.getTimestamp("created_at");
        verification.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return verification;
    }
}
