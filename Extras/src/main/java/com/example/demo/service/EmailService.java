package com.example.demo.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailUsername;

    @Value("${moffat.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public String buildVerificationUrl(String verificationToken) {

        if (verificationToken == null || verificationToken.isBlank()) {

            throw new IllegalArgumentException(
                    "Verification token cannot be empty.");

        }

        String encodedToken = URLEncoder.encode(
                verificationToken,
                StandardCharsets.UTF_8);

        return baseUrl + "/verification.html?token=" + encodedToken;
    }
    
    public void sendVerificationEmail(
            String recipientEmail,
            String firstName,
            String verificationToken) throws MessagingException {

        validateRecipient(recipientEmail);

        if (verificationToken == null || verificationToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Verification token cannot be empty.");
        }

        String encodedToken = URLEncoder.encode(
                verificationToken,
                StandardCharsets.UTF_8);

        String verificationLink = baseUrl
                + "/verification.html?token="
                + encodedToken;

        String safeFirstName =
                firstName == null || firstName.isBlank()
                        ? "Customer"
                        : firstName.trim();

        String subject = "Verify Your Moffat Bay Marina Account";

        String plainTextBody =
                "Hello " + safeFirstName + ",\n\n"
                + "Welcome to Moffat Bay Marina!\n\n"
                + "Please verify your email address by "
                + "clicking the link below:\n\n"
                + verificationLink
                + "\n\n"
                + "This verification link will expire. "
                + "If you did not create a Moffat Bay Marina "
                + "account, you can ignore this email.\n\n"
                + "Thank you,\n"
                + "Moffat Bay Marina";

        String htmlBody = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Verify Your Email</title>
                </head>
                <body style="
                    margin: 0;
                    padding: 0;
                    background-color: #f4f6f8;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #172033;
                ">

                    <table width="100%%"
                           cellspacing="0"
                           cellpadding="0"
                           border="0"
                           style="
                               background-color: #f4f6f8;
                               padding: 40px 15px;
                           ">
                        <tr>
                            <td align="center">

                                <table width="100%%"
                                       cellspacing="0"
                                       cellpadding="0"
                                       border="0"
                                       style="
                                           max-width: 600px;
                                           background-color: #ffffff;
                                           border-radius: 10px;
                                           overflow: hidden;
                                           box-shadow:
                                           0 5px 20px
                                           rgba(0,0,0,0.08);
                                       ">

                                    <tr>
                                        <td style="
                                            background-color: #061b3a;
                                            padding: 30px;
                                            text-align: center;
                                            border-bottom:
                                            4px solid #eab53f;
                                        ">
                                            <h1 style="
                                                color: #ffffff;
                                                margin: 0;
                                                font-size: 28px;
                                            ">
                                                Moffat Bay Marina
                                            </h1>

                                            <p style="
                                                color: #eab53f;
                                                margin: 8px 0 0 0;
                                                font-size: 14px;
                                            ">
                                                Your Gateway to the Water
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            padding: 40px;
                                        ">

                                            <h2 style="
                                                color: #061b3a;
                                                margin-top: 0;
                                            ">
                                                Verify Your Email
                                            </h2>

                                            <p style="
                                                font-size: 16px;
                                                line-height: 1.7;
                                            ">
                                                Hello %s,
                                            </p>

                                            <p style="
                                                font-size: 16px;
                                                line-height: 1.7;
                                            ">
                                                Thank you for creating
                                                your Moffat Bay Marina
                                                account.
                                            </p>

                                            <p style="
                                                font-size: 16px;
                                                line-height: 1.7;
                                            ">
                                                Before you can access
                                                your account and manage
                                                your marina reservations,
                                                please verify your email
                                                address.
                                            </p>

                                            <table
                                                cellspacing="0"
                                                cellpadding="0"
                                                border="0"
                                                width="100%%">
                                                <tr>
                                                    <td
                                                        align="center"
                                                        style="
                                                            padding:
                                                            25px 0;
                                                        ">
                                                        <a href="%s"
                                                           style="
                                                           background-color:
                                                           #eab53f;
                                                           color:
                                                           #061b3a;
                                                           padding:
                                                           15px 30px;
                                                           border-radius:
                                                           6px;
                                                           text-decoration:
                                                           none;
                                                           font-weight:
                                                           bold;
                                                           font-size:
                                                           16px;
                                                           display:
                                                           inline-block;
                                                           ">
                                                            Verify My Email
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="
                                                font-size: 14px;
                                                line-height: 1.6;
                                                color: #667085;
                                            ">
                                                If the button above does
                                                not work, copy and paste
                                                the following link into
                                                your browser:
                                            </p>

                                            <p style="
                                                font-size: 13px;
                                                line-height: 1.5;
                                                word-break: break-all;
                                                color: #102b52;
                                            ">
                                                %s
                                            </p>

                                            <hr style="
                                                border: none;
                                                border-top:
                                                1px solid #d9dde5;
                                                margin: 30px 0;
                                            ">

                                            <p style="
                                                font-size: 13px;
                                                line-height: 1.6;
                                                color: #667085;
                                            ">
                                                This verification link
                                                will expire for security
                                                purposes.
                                            </p>

                                            <p style="
                                                font-size: 13px;
                                                line-height: 1.6;
                                                color: #667085;
                                            ">
                                                If you did not create a
                                                Moffat Bay Marina account,
                                                you can safely ignore
                                                this email.
                                            </p>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            background-color: #061b3a;
                                            color: #ffffff;
                                            padding: 25px;
                                            text-align: center;
                                        ">

                                            <p style="
                                                margin: 0;
                                                font-size: 13px;
                                            ">
                                                © %d Moffat Bay Marina
                                            </p>

                                            <p style="
                                                margin: 8px 0 0 0;
                                                color: #eab53f;
                                                font-size: 12px;
                                            ">
                                                Thank you for choosing
                                                Moffat Bay Marina.
                                            </p>

                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """.formatted(
                        escapeHtml(safeFirstName),
                        verificationLink,
                        verificationLink,
                        Year.now().getValue());

        sendEmail(
                recipientEmail,
                subject,
                plainTextBody,
                htmlBody);
    }

    private void sendEmail(
            String recipientEmail,
            String subject,
            String plainTextBody,
            String htmlBody) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(
                        message,
                        true,
                        StandardCharsets.UTF_8.name());

        try {
            helper.setFrom(emailUsername, "Moffat Bay Marina");
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException(
                    "Unable to configure email sender.",
                    e);
        }
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(plainTextBody, htmlBody);

        mailSender.send(message);
    }

    public void sendPlainTextEmail(
            String recipientEmail,
            String subject,
            String body) {

        validateRecipient(recipientEmail);

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(emailUsername);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    private void validateRecipient(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email cannot be empty.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException(
                    "Invalid recipient email address: " + email);
        }
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}