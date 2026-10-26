
package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CompatibilityChecker {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in);
             Connection connection = DBConnection.getConnection()) {

            System.out.println("========================================");
            System.out.println("       SMART PC BUILDER");
            System.out.println("   HARDWARE COMPATIBILITY CHECKER");
            System.out.println("========================================");

            // Display CPUs
            System.out.println("\nAvailable CPUs:");

            String cpuQuery =
                    "SELECT id, name, socket FROM components WHERE category = 'CPU'";

            try (PreparedStatement statement =
                         connection.prepareStatement(cpuQuery);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    System.out.printf("%d. %s | Socket: %s%n",
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("socket"));
                }
            }

            int cpuId = readId(scanner, "\nEnter CPU ID: ");

            // Display motherboards
            System.out.println("\nAvailable Motherboards:");

            String motherboardQuery =
                    "SELECT id, name, socket, ram_type "
                            + "FROM components WHERE category = 'Motherboard'";

            try (PreparedStatement statement =
                         connection.prepareStatement(motherboardQuery);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    System.out.printf("%d. %s | Socket: %s | RAM: %s%n",
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("socket"),
                            resultSet.getString("ram_type"));
                }
            }

            int motherboardId = readId(scanner, "\nEnter Motherboard ID: ");

            // Display RAM
            System.out.println("\nAvailable RAM:");

            String ramQuery =
                    "SELECT id, name, ram_type FROM components WHERE category = 'RAM'";

            try (PreparedStatement statement =
                         connection.prepareStatement(ramQuery);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    System.out.printf("%d. %s | Type: %s%n",
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("ram_type"));
                }
            }

            int ramId = readId(scanner, "\nEnter RAM ID: ");

            // Fetch selected component details
            String detailsQuery =
                    "SELECT id, name, category, socket, ram_type "
                            + "FROM components WHERE id IN (?, ?, ?)";

            String cpuName = null;
            String cpuSocket = null;
            String motherboardName = null;
            String motherboardSocket = null;
            String motherboardRamType = null;
            String ramName = null;
            String ramType = null;

            try (PreparedStatement statement =
                         connection.prepareStatement(detailsQuery)) {

                statement.setInt(1, cpuId);
                statement.setInt(2, motherboardId);
                statement.setInt(3, ramId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    while (resultSet.next()) {

                        String category = resultSet.getString("category");

                        if ("CPU".equals(category)
                                && resultSet.getInt("id") == cpuId) {

                            cpuName = resultSet.getString("name");
                            cpuSocket = resultSet.getString("socket");

                        } else if ("Motherboard".equals(category)
                                && resultSet.getInt("id") == motherboardId) {

                            motherboardName = resultSet.getString("name");
                            motherboardSocket = resultSet.getString("socket");
                            motherboardRamType = resultSet.getString("ram_type");

                        } else if ("RAM".equals(category)
                                && resultSet.getInt("id") == ramId) {

                            ramName = resultSet.getString("name");
                            ramType = resultSet.getString("ram_type");
                        }
                    }
                }
            }

            System.out.println("\n========== COMPATIBILITY REPORT ==========");

            if (cpuName == null
                    || motherboardName == null
                    || ramName == null) {

                System.out.println(
                        "Invalid selection. Choose a valid CPU, motherboard, and RAM ID.");

            } else {

                System.out.println("CPU: " + cpuName);
                System.out.println("Motherboard: " + motherboardName);
                System.out.println("RAM: " + ramName);

                boolean cpuCompatible =
                        cpuSocket != null
                                && motherboardSocket != null
                                && cpuSocket.equalsIgnoreCase(motherboardSocket);

                boolean ramCompatible =
                        motherboardRamType != null
                                && ramType != null
                                && motherboardRamType.equalsIgnoreCase(ramType);

                System.out.println("\nCPU / Motherboard: "
                        + (cpuCompatible ? "COMPATIBLE" : "NOT COMPATIBLE"));

                System.out.println("Motherboard / RAM: "
                        + (ramCompatible ? "COMPATIBLE" : "NOT COMPATIBLE"));

                if (cpuCompatible && ramCompatible) {
                    System.out.println("\nOVERALL RESULT: COMPATIBLE");
                } else {
                    System.out.println("\nOVERALL RESULT: NOT COMPATIBLE");
                }
            }

            System.out.println("==========================================");

        } catch (SQLException e) {

            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static int readId(Scanner scanner, String prompt) {

        while (true) {
            System.out.print(prompt);

            if (scanner.hasNextInt()) {
                int id = scanner.nextInt();

                if (id > 0) {
                    return id;
                }

                System.out.println("Please enter a positive ID.");
            } else {
                System.out.println("Invalid input. Enter a numeric ID.");
                scanner.next();
            }
        }
    }
}

