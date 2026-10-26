
package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class PCBuilder {

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

        Map<String, Component> selectedComponents = new LinkedHashMap<>();
        double totalPrice = 0;

        try (Connection connection = DBConnection.getConnection()) {

            System.out.println("\n======================================");
            System.out.println("       SMART PC BUILDER");
            System.out.println("======================================");

            for (String category : categories) {

                System.out.println("\nAvailable " + category + " components:");

                String sql = """
                    SELECT id, name, category, socket, ram_type, price, wattage
                    FROM components
                    WHERE category = ?
                    ORDER BY id
                    """;

                try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                    statement.setString(1, category);

                    try (ResultSet rs = statement.executeQuery()) {

                        while (rs.next()) {
                            System.out.printf(
                                "ID: %d | %s | Rs. %.2f%n",
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getDouble("price")
                            );
                        }
                    }
                }

                Component selected = null;

                while (selected == null) {

                    System.out.print(
                        "Enter the ID of your chosen " + category + ": "
                    );

                    if (!scanner.hasNextInt()) {
                        System.out.println("Please enter a numeric ID.");
                        scanner.next();
                        continue;
                    }

                    int id = scanner.nextInt();

                    String findSql = """
                        SELECT id, name, category, socket, ram_type, price, wattage
                        FROM components
                        WHERE id = ? AND category = ?
                        """;

                    try (PreparedStatement statement =
                             connection.prepareStatement(findSql)) {

                        statement.setInt(1, id);
                        statement.setString(2, category);

                        try (ResultSet rs = statement.executeQuery()) {

                            if (rs.next()) {
                                selected = new Component(
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
                                    "Invalid ID. Choose an ID from the "
                                    + category + " list."
                                );
                            }
                        }
                    }
                }

                selectedComponents.put(category, selected);
                totalPrice += selected.price;
            }

            System.out.println("\n======================================");
            System.out.println("         COMPATIBILITY REPORT");
            System.out.println("======================================");

            boolean compatible = true;

            Component cpu = selectedComponents.get("CPU");
            Component motherboard = selectedComponents.get("Motherboard");
            Component ram = selectedComponents.get("RAM");

            // Check CPU and motherboard socket
            if (hasValue(cpu.socket) && hasValue(motherboard.socket)) {

                if (cpu.socket.equalsIgnoreCase(motherboard.socket)) {
                    System.out.println("PASS: CPU and motherboard sockets match.");
                } else {
                    System.out.println("FAIL: CPU and motherboard sockets do not match.");
                    System.out.println("CPU socket: " + cpu.socket);
                    System.out.println("Motherboard socket: " + motherboard.socket);
                    compatible = false;
                }

            } else {
                System.out.println(
                    "WARNING: Socket information is missing; compatibility not verified."
                );
                compatible = false;
            }

            // Check motherboard and RAM type
            if (hasValue(motherboard.ramType) && hasValue(ram.ramType)) {

                if (motherboard.ramType.equalsIgnoreCase(ram.ramType)) {
                    System.out.println("PASS: Motherboard and RAM types match.");
                } else {
                    System.out.println("FAIL: Motherboard and RAM types do not match.");
                    System.out.println("Motherboard RAM type: " + motherboard.ramType);
                    System.out.println("RAM type: " + ram.ramType);
                    compatible = false;
                }

            } else {
                System.out.println(
                    "WARNING: RAM type information is missing; compatibility not verified."
                );
                compatible = false;
            }

            // Check power supply wattage against listed CPU and GPU wattage
            Component gpu = selectedComponents.get("GPU");
            Component psu = selectedComponents.get("PSU");

            int knownPowerDemand = cpu.wattage + gpu.wattage;

            if (psu.wattage > 0 && knownPowerDemand > 0) {

                if (psu.wattage >= knownPowerDemand) {
                    System.out.println(
                        "PASS: PSU wattage covers the listed CPU and GPU wattage."
                    );
                } else {
                    System.out.println(
                        "FAIL: PSU wattage is below the combined listed CPU and GPU wattage."
                    );
                    compatible = false;
                }

            } else {
                System.out.println(
                    "WARNING: Wattage information is missing; PSU compatibility not verified."
                );
                compatible = false;
            }

            System.out.println("\n======================================");
            System.out.println("            YOUR PC BUILD");
            System.out.println("======================================");

            for (Component component : selectedComponents.values()) {
                System.out.printf(
                    "%-15s %-35s Rs. %.2f%n",
                    component.category,
                    component.name,
                    component.price
                );
            }

            System.out.println("--------------------------------------");
            System.out.printf("TOTAL PRICE: Rs. %.2f%n", totalPrice);
            System.out.println("--------------------------------------");

            if (compatible) {
                System.out.println("FINAL RESULT: BUILD PASSED CHECKED RULES");
                System.out.println("All compatibility checks implemented here passed.");
            } else {
                System.out.println("FINAL RESULT: BUILD FAILED OR NEEDS VERIFICATION");
                System.out.println("Review the FAIL and WARNING messages above.");
            }

            System.out.println("======================================");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}

