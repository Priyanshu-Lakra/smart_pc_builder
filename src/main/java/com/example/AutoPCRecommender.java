
package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AutoPCRecommender {

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

        System.out.println("\n======================================");
        System.out.println("     AUTOMATIC PC RECOMMENDER");
        System.out.println("======================================");

        System.out.print("Enter your budget in rupees: ");

        if (!scanner.hasNextDouble()) {
            System.out.println("Invalid budget. Please enter a number.");
            scanner.close();
            return;
        }

        double budget = scanner.nextDouble();

        if (!Double.isFinite(budget) || budget <= 0) {
            System.out.println("Budget must be greater than zero.");
            scanner.close();
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            List<Component> cpus = load(connection, "CPU");
            List<Component> motherboards = load(connection, "Motherboard");
            List<Component> rams = load(connection, "RAM");
            List<Component> gpus = load(connection, "GPU");
            List<Component> storage = load(connection, "Storage");
            List<Component> psus = load(connection, "PSU");

            if (cpus.isEmpty() || motherboards.isEmpty()
                    || rams.isEmpty() || gpus.isEmpty()
                    || storage.isEmpty() || psus.isEmpty()) {
                System.out.println(
                    "Cannot recommend a complete PC: one or more categories are empty."
                );
                return;
            }

            List<Component> bestBuild = null;
            double bestPrice = -1;

            for (Component cpu : cpus) {
                for (Component motherboard : motherboards) {

                    if (!hasValue(cpu.socket)
                            || !hasValue(motherboard.socket)
                            || !cpu.socket.equalsIgnoreCase(motherboard.socket)) {
                        continue;
                    }

                    for (Component ram : rams) {

                        if (!hasValue(motherboard.ramType)
                                || !hasValue(ram.ramType)
                                || !motherboard.ramType.equalsIgnoreCase(ram.ramType)) {
                            continue;
                        }

                        for (Component gpu : gpus) {
                            for (Component disk : storage) {
                                for (Component psu : psus) {

                                    if (cpu.wattage <= 0
                                            || gpu.wattage <= 0
                                            || psu.wattage <= 0) {
                                        continue;
                                    }

                                    int listedPower = cpu.wattage + gpu.wattage;

                                    if (psu.wattage < listedPower) {
                                        continue;
                                    }

                                    double total = cpu.price
                                            + motherboard.price
                                            + ram.price
                                            + gpu.price
                                            + disk.price
                                            + psu.price;

                                    if (total <= budget && total > bestPrice) {

                                        bestPrice = total;

                                        bestBuild = List.of(
                                            cpu, motherboard, ram,
                                            gpu, disk, psu
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (bestBuild == null) {
                System.out.println(
                    "\nNo complete build passed these checks within your budget."
                );
                System.out.println(
                    "Try a higher budget or add more components to the database."
                );
                return;
            }

            System.out.println("\n======================================");
            System.out.println("       RECOMMENDED PC BUILD");
            System.out.println("======================================");

            for (Component component : bestBuild) {
                System.out.printf(
                    "%-15s %-32s Rs. %.2f%n",
                    component.category,
                    component.name,
                    component.price
                );
            }

            System.out.println("--------------------------------------");
            System.out.printf("Budget       : Rs. %.2f%n", budget);
            System.out.printf("Total price  : Rs. %.2f%n", bestPrice);
            System.out.printf("Remaining    : Rs. %.2f%n", budget - bestPrice);
            System.out.println("--------------------------------------");
            System.out.println("Socket check: PASS");
            System.out.println("RAM type check: PASS");
            System.out.println("Basic PSU wattage check: PASS");
            System.out.println(
                "\nRecommendation found using the specifications in your database."
            );
            System.out.println(
                "This is not a guarantee of complete hardware compatibility."
            );

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static List<Component> load(
            Connection connection, String category) throws SQLException {

        List<Component> components = new ArrayList<>();

        String sql = """
            SELECT id, name, category, socket, ram_type, price, wattage
            FROM components
            WHERE category = ?
            ORDER BY price ASC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    components.add(new Component(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("socket"),
                        rs.getString("ram_type"),
                        rs.getDouble("price"),
                        rs.getInt("wattage")
                    ));
                }
            }
        }

        return components;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
