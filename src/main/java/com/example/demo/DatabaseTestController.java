package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class DatabaseTestController {

    private final DataSource dataSource;

    public DatabaseTestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/database-test")
    public String databaseTest() {

        try (Connection connection = dataSource.getConnection()) {

            return "SUCCESS: Java/Spring is connected to the Moffat Bay database.";

        } catch (Exception e) {

            return "ERROR: Database connection failed. " + e.getMessage();
        }
    }
}
