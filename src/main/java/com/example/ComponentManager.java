
package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ComponentManager {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            System.out.println("\n======================================");
            System.out.println("       COMPONENT MANAGEMENT");
            System.out.println("======================================");
            System.out.println("1. View Components");
            System.out.println("2. Add Component");
            System.out.println("3. Update Component Price");
            System.out.println("4. Delete Component");
            System.out.println("5. Back to Main Menu");
            System.out.println("======================================");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    viewComponents();
                    break;

                case "2":
                    addComponent();
                    break;

                case "3":
                    updatePrice();
                    break;

                case "4":
                    deleteComponent();
                    break;

                case "5":
                    running = false;
                    System.out.println("Returning to the main menu...");
                    break;

                default:
                    System.out.println("Invalid choice. Enter 1 to 5.");
            }
        }
    }

    private static void viewComponents() {

        String sql = """
            SELECT id, name, category, brand, price, socket,
                   ram_type, wattage
            FROM components
            ORDER BY id
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            System.out.println("\n--------------- COMPONENTS ---------------");

            while (rs.next()) {

                System.out.printf(
                    "ID: %d | Name: %s | Category: %s | Brand: %s "
                    + "| Price: Rs. %.2f | Socket: %s | RAM: %s "
                    + "| Wattage: %d%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getDouble("price"),
                    rs.getString("socket"),
                    rs.getString("ram_type"),
                    rs.getInt("wattage")
                );
            }

        } catch (SQLException e) {
            System.out.println("Could not load components: " + e.getMessage());
        }
    }

    private static void addComponent() {

        System.out.println("\n--- ADD COMPONENT ---");

        System.out.print("Component name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Category (CPU/Motherboard/RAM/GPU/Storage/PSU): ");
        String category = scanner.nextLine().trim();

        if (!isValidCategory(category)) {
            System.out.println("Invalid category.");
            return;
        }

        System.out.print("Brand: ");
        String brand = scanner.nextLine().trim();

        Double price = readNonNegativeDouble("Price in rupees: ");

        if (price == null) {
            return;
        }

        System.out.print("Socket (press Enter if not applicable): ");
        String socket = scanner.nextLine().trim();

        System.out.print("RAM type (DDR4/DDR5 or blank): ");
        String ramType = scanner.nextLine().trim();

        Integer wattage = readNonNegativeInt("Wattage (0 if unknown/not applicable): ");

        if (wattage == null) {
            return;
        }

        String sql = """
            INSERT INTO components
                (name, category, brand, price, socket, ram_type, wattage)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, category);
            statement.setString(3, brand.isEmpty() ? null : brand);
            statement.setDouble(4, price);
            statement.setString(5, socket.isEmpty() ? null : socket);
            statement.setString(6, ramType.isEmpty() ? null : ramType);
            statement.setInt(7, wattage);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Component added successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Could not add component: " + e.getMessage());
        }
    }

    private static void updatePrice() {

        System.out.println("\n--- UPDATE COMPONENT PRICE ---");

        Integer id = readPositiveInt("Enter component ID: ");

        if (id == null) {
            return;
        }

        Double price = readNonNegativeDouble("Enter new price: ");

        if (price == null) {
            return;
        }

        String sql = "UPDATE components SET price = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, price);
            statement.setInt(2, id);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Component price updated successfully!");
            } else {
                System.out.println("No component found with ID " + id);
            }

        } catch (SQLException e) {
            System.out.println("Could not update price: " + e.getMessage());
        }
    }

    private static void deleteComponent() {

        System.out.println("\n--- DELETE COMPONENT ---");

        Integer id = readPositiveInt("Enter component ID to delete: ");

        if (id == null) {
            return;
        }

        System.out.print(
            "Are you sure you want to delete component ID " + id
            + "? Type YES to confirm: "
        );

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equals("YES")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        String sql = "DELETE FROM components WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Component deleted successfully!");
            } else {
                System.out.println("No component found with ID " + id);
            }

        } catch (SQLException e) {
            System.out.println("Could not delete component: " + e.getMessage());
        }
    }

    private static boolean isValidCategory(String category) {

        String[] categories = {
            "CPU", "Motherboard", "RAM", "GPU", "Storage", "PSU"
        };

        for (String valid : categories) {
            if (valid.equalsIgnoreCase(category)) {
                return true;
            }
        }

        return false;
    }

    private static Double readNonNegativeDouble(String prompt) {

        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            double value = Double.parseDouble(input);

            if (Double.isFinite(value) && value >= 0) {
                return value;
            }

        } catch (NumberFormatException ignored) {
            // Display the validation message below.
        }

        System.out.println("Please enter a valid non-negative number.");
        return null;
    }

    private static Integer readNonNegativeInt(String prompt) {

        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            int value = Integer.parseInt(input);

            if (value >= 0) {
                return value;
            }

        } catch (NumberFormatException ignored) {
            // Display the validation message below.
        }

        System.out.println("Please enter a valid non-negative integer.");
        return null;
    }

    private static Integer readPositiveInt(String prompt) {

        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            int value = Integer.parseInt(input);

            if (value > 0) {
                return value;
            }

        } catch (NumberFormatException ignored) {
            // Display the validation message below.
        }

        System.out.println("Please enter a valid positive integer.");
        return null;
    }
}

