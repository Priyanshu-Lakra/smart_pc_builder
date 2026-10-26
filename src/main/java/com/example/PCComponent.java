
package com.example;

public class PCComponent {

    private final int id;
    private final String name;
    private final String category;
    private final String brand;
    private final double price;
    private final String socket;
    private final String ramType;
    private final int wattage;

    public PCComponent(
            int id,
            String name,
            String category,
            String brand,
            double price,
            String socket,
            String ramType,
            int wattage) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.socket = socket;
        this.ramType = ramType;
        this.wattage = wattage;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    public String getSocket() {
        return socket;
    }

    public String getRamType() {
        return ramType;
    }

    public int getWattage() {
        return wattage;
    }

    @Override
    public String toString() {
        return name + " - ₹" + String.format("%.2f", price);
    }
}

