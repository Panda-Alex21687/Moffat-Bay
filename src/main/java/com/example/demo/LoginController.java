package com.example.demo;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Controller
public class LoginController {

    private final DataSource dataSource;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginController(DataSource dataSource) {
        this.dataSource = dataSource;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session) {

        String sql = """
                SELECT customer_id, first_name, last_name, email, password_hash
                FROM customers
                WHERE email = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim().toLowerCase());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    String storedHash =
                            resultSet.getString("password_hash");

                    if (passwordEncoder.matches(password, storedHash)) {

                        session.setAttribute(
                                "customer_id",
                                resultSet.getInt("customer_id")
                        );

                        session.setAttribute(
                                "first_name",
                                resultSet.getString("first_name")
                        );

                        session.setAttribute(
                                "email",
                                resultSet.getString("email")
                        );

                        return "redirect:/post_login.html";
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/login.html?error=1";
    }
}