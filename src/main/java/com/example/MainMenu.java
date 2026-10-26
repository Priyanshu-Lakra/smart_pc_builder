
package com.example;

import java.util.Scanner;

public class MainMenu {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {

            System.out.println("\n");
            System.out.println("==========================================");
            System.out.println("          SMART PC BUILDER SYSTEM");
            System.out.println("==========================================");
            System.out.println("1. View All Components");
            System.out.println("2. Check CPU and Motherboard Compatibility");
            System.out.println("3. Build Your PC and Check Compatibility");
            System.out.println("4. Test Database Connection");
            System.out.println("5. Budget PC Builder");
            System.out.println("6. Automatic PC Recommendation");
            System.out.println("7. Exit");
            System.out.println("==========================================");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    System.out.println("\nOpening Component Viewer...");
                    ComponentViewer.main(new String[0]);
                    break;

                case "2":
                    System.out.println("\nOpening Compatibility Checker...");
                    CompatibilityChecker.main(new String[0]);
                    break;

                case "3":
                    System.out.println("\nOpening PC Builder...");
                    PCBuilder.main(new String[0]);
                    break;

                case "4":
                    System.out.println("\nTesting database connection...");
                    DBConnection.main(new String[0]);
                    break;

                case "5":
                    System.out.println("\nOpening Budget PC Builder...");
                    BudgetPCBuilder.main(new String[0]);
                    break;

                case "6":
                    System.out.println("\nOpening Automatic PC Recommendation...");
                    AutoPCRecommender.main(new String[0]);
                    break;

                case "7":
                    running = false;
                    System.out.println("\nThank you for using Smart PC Builder!");
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println(
                        "Invalid choice! Please enter a number from 1 to 7."
                    );
                    break;
            }
        }

        scanner.close();
    }
}

