
package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PCComponentDAO {

    public List<PCComponent> getAllComponents() throws SQLException {

        List<PCComponent> components = new ArrayList<>();

        String sql = """
                SELECT id, name, category, brand, price,
                       socket, ram_type, wattage
                FROM components
                ORDER BY category, name
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                components.add(new PCComponent(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("category"),
                        result.getString("brand"),
                        result.getDouble("price"),
                        result.getString("socket"),
                        result.getString("ram_type"),
                        result.getInt("wattage")
                ));
            }
        }

        return components;
    }
}

