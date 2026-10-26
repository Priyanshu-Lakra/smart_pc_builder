
package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class BudgetPCBuilder {

    static class Component {
        int id;
        String name;
        String category;
        String socket;
        String ramType;
        double price;
        int wattage;

        Component(int id, String name, String category,
                  String socket, String ramType,
                  double price, int wattage) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.socket = socket;
            this.ramType = ramType;
            this.price = price;
            this.wattage = wattage;
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        String[] categories = {
            "CPU", "Motherboard", "RAM", "GPU", "Storage", "PSU"
        };

        Map<String, Component> selected = new LinkedHashMap<>();
        double budget;

        System.out.println("\n==================================");
        System.out.println("       BUDGET PC BUILDER");
        System.out.println("==================================");

        while (true) {
            System.out.print("Enter your PC budget in rupees: ");

            if (scanner.hasNextDouble()) {
                budget = scanner.nextDouble();

                if (budget > 0 && Double.isFinite(budget)) {
                    break;
                }
            } else {
                scanner.next();
            }

            System.out.println("Please enter a valid positive budget.");
        }

        try (Connection connection = DBConnection.getConnection()) {

            double total = 0;

            for (String category : categories) {

                System.out.println("\nAvailable " + category + ":");

                String sql = """
                    SELECT id, name, category, socket, ram_type, price, wattage
                    FROM components
                    WHERE category = ?
                    ORDER BY price ASC
                    """;

                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, category);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            System.out.printf(
                                "ID: %d | %-35s | Rs. %.2f%n",
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getDouble("price")
                            );
                        }
                    }
                }

                Component component = null;

                while (component == null) {
                    System.out.print("Choose " + category + " ID: ");

                    if (!scanner.hasNextInt()) {
                        System.out.println("Please enter a numeric ID.");
                        scanner.next();
                        continue;
                    }

                    int id = scanner.nextInt();

                    String selectSql = """
                        SELECT id, name, category, socket, ram_type, price, wattage
                        FROM components
                        WHERE id = ? AND category = ?
                        """;

                    try (PreparedStatement ps =
                             connection.prepareStatement(selectSql)) {

                        ps.setInt(1, id);
                        ps.setString(2, category);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                component = new Component(
                                    rs.getInt("id"),
                                    rs.getString("name"),
                                    rs.getString("category"),
                                    rs.getString("socket"),
                                    rs.getString("ram_type"),
                                    rs.getDouble("price"),
                                    rs.getInt("wattage")
                                );
                            } else {
                                System.out.println(
                                    "Invalid component ID. Please choose from the list."
                                );
                            }
                        }
                    }
                }

                selected.put(category, component);
                total += component.price;

                System.out.printf(
                    "Selected: %s | Rs. %.2f%n",
                    component.name, component.price
                );

                System.out.printf(
                    "Current total: Rs. %.2f%n",
                    total
                );

                if (total > budget) {
                    System.out.println(
                        "WARNING: Your current selections exceed your budget."
                    );
                }
            }

            System.out.println("\n==================================");
            System.out.println("         PC BUILD SUMMARY");
            System.out.println("==================================");

            for (Component component : selected.values()) {
                System.out.printf(
                    "%-15s %-35s Rs. %.2f%n",
                    component.category,
                    component.name,
                    component.price
                );
            }

            System.out.println("----------------------------------");
            System.out.printf("Your budget : Rs. %.2f%n", budget);
            System.out.printf("Total cost  : Rs. %.2f%n", total);
            System.out.printf("Difference  : Rs. %.2f%n", budget - total);
            System.out.println("----------------------------------");

            if (total <= budget) {
                System.out.println("BUDGET STATUS: WITHIN BUDGET");
            } else {
                System.out.println("BUDGET STATUS: OVER BUDGET");
            }

            checkCompatibility(selected);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void checkCompatibility(
            Map<String, Component> selected) {

        boolean compatible = true;

        Component cpu = selected.get("CPU");
        Component motherboard = selected.get("Motherboard");
        Component ram = selected.get("RAM");
        Component gpu = selected.get("GPU");
        Component psu = selected.get("PSU");

        System.out.println("\n==================================");
        System.out.println("       COMPATIBILITY REPORT");
        System.out.println("==================================");

        if (hasValue(cpu.socket) && hasValue(motherboard.socket)) {
            if (cpu.socket.equalsIgnoreCase(motherboard.socket)) {
                System.out.println("PASS: CPU socket matches motherboard.");
            } else {
                System.out.println("FAIL: CPU socket does not match motherboard.");
                compatible = false;
            }
        } else {
            System.out.println("UNKNOWN: Socket information is missing.");
            compatible = false;
        }

        if (hasValue(motherboard.ramType) && hasValue(ram.ramType)) {
            if (motherboard.ramType.equalsIgnoreCase(ram.ramType)) {
                System.out.println("PASS: RAM type matches motherboard.");
            } else {
                System.out.println("FAIL: RAM type does not match motherboard.");
                compatible = false;
            }
        } else {
            System.out.println("UNKNOWN: RAM type information is missing.");
            compatible = false;
        }

        int knownPowerDemand = cpu.wattage + gpu.wattage;

        if (psu.wattage > 0 && knownPowerDemand > 0) {
            if (psu.wattage >= knownPowerDemand) {
                System.out.println(
                    "PASS: PSU wattage covers the listed CPU and GPU wattage."
                );
            } else {
                System.out.println(
                    "FAIL: PSU wattage is below the listed CPU and GPU wattage."
                );
                compatible = false;
            }
        } else {
            System.out.println("UNKNOWN: PSU or component wattage is missing.");
            compatible = false;
        }

        System.out.println("----------------------------------");

        if (compatible) {
            System.out.println("COMPATIBILITY: CHECKED RULES PASSED");
        } else {
            System.out.println(
                "COMPATIBILITY: FAILED OR NEEDS VERIFICATION"
            );
        }

        System.out.println(
            "Note: This is a basic compatibility check, not a guarantee "
            + "of complete hardware compatibility."
        );
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}

