
package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/smart_pc_builder";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {

        System.out.println("Connecting to Smart PC Builder database...");

        try (Connection connection = getConnection()) {

            System.out.println("MySQL Connected Successfully!");
            System.out.println("Database: " + connection.getCatalog());

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            System.out.println("Reason: " + e.getMessage());

        }
    }
}

