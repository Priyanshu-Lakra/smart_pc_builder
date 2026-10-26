
package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComponentViewer {

    public static void main(String[] args) {

        String sql = """
                SELECT id, name, category, brand, price
                FROM components
                ORDER BY category, name
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("\n========== SMART PC BUILDER ==========");
            System.out.printf("%-4s %-32s %-14s %-18s %10s%n",
                    "ID", "NAME", "CATEGORY", "BRAND", "PRICE (INR)");

            System.out.println(
                    "----------------------------------------------------------------------------"
            );

            while (resultSet.next()) {

                System.out.printf("%-4d %-32s %-14s %-18s ₹%,10.2f%n",
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("category"),
                        resultSet.getString("brand"),
                        resultSet.getBigDecimal("price"));
            }

            System.out.println(
                    "======================================"
            );

        } catch (SQLException e) {

            System.out.println("Could not retrieve components.");
            System.out.println("Reason: " + e.getMessage());
        }
    }
}

