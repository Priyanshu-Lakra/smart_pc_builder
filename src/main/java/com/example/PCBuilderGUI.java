
package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class PCBuilderGUI extends JFrame {

    private static final Color BLACK = new Color(12, 12, 15);
    private static final Color PANEL = new Color(31, 31, 38);
    private static final Color CARD = new Color(48, 47, 55);
    private static final Color GOLD = new Color(218, 174, 75);
    private static final Color WHITE = new Color(240, 240, 240);

    private final Map<String, ComponentItem> selected =
            new LinkedHashMap<>();

    private final JLabel totalLabel = new JLabel("TOTAL: Rs. 0.00");
    private final JLabel statusLabel = new JLabel("Ready");
    private final JPanel selectionPanel = new JPanel();

    private double total = 0;

    private static class ComponentItem {
        int id;
        String name;
        String category;
        String socket;
        String ramType;
        double price;
        int wattage;

        ComponentItem(int id, String name, String category,
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

        @Override
        public String toString() {
            return name + "  -  Rs. " + String.format("%.2f", price);
        }
    }

    public PCBuilderGUI() {

        setTitle("Smart PC Builder");
        setSize(1250, 800);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BLACK);
        setLayout(new BorderLayout(12, 12));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadSelections();
        updateTotal();
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLACK);
        header.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel title = new JLabel("⬡ SMART PC BUILDER");
        title.setForeground(GOLD);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navigation.setBackground(BLACK);

        for (String text : new String[]{
                "STYLE", "PERFORMANCE", "UPGRADES", "SETUP"}) {
            JButton button = createButton(text);
            navigation.add(button);
        }

        header.add(title, BorderLayout.WEST);
        header.add(navigation, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainPanel() {

        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setBackground(BLACK);
        main.setBorder(new EmptyBorder(0, 18, 12, 18));

        // Left: PC image area
        JPanel pcPanel = new JPanel(new BorderLayout(10, 10));
        pcPanel.setBackground(PANEL);
        pcPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel pcTitle = new JLabel("YOUR PC BUILD");
        pcTitle.setForeground(GOLD);
        pcTitle.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel pcImage = new JLabel(
                "<html><div style='text-align:center;'>"
                + "<div style='font-size:100px;color:#DAAE4B;'>▣</div>"
                + "<div style='font-size:20px;color:white;'>GAMING PC</div>"
                + "<div style='font-size:13px;color:#bbbbbb;'>"
                + "Your custom configuration</div>"
                + "</div></html>",
                SwingConstants.CENTER
        );

        pcImage.setOpaque(true);
        pcImage.setBackground(new Color(18, 18, 22));

        JLabel instructions = new JLabel(
                "Select components from the configuration panel →",
                SwingConstants.CENTER
        );
        instructions.setForeground(WHITE);

        pcPanel.add(pcTitle, BorderLayout.NORTH);
        pcPanel.add(pcImage, BorderLayout.CENTER);
        pcPanel.add(instructions, BorderLayout.SOUTH);

        // Right: component configuration
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(440, 0));
        rightPanel.setBackground(PANEL);
        rightPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel configTitle = new JLabel("CONFIGURATION SUMMARY");
        configTitle.setForeground(GOLD);
        configTitle.setFont(new Font("Arial", Font.BOLD, 20));

        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.setBackground(PANEL);

        JScrollPane scrollPane = new JScrollPane(selectionPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL);

        JButton compatibilityButton = createButton("CHECK COMPATIBILITY");
        compatibilityButton.addActionListener(e -> checkCompatibility());

        JButton clearButton = createButton("CLEAR BUILD");
        clearButton.addActionListener(e -> {
            selected.clear();
            total = 0;
            loadSelections();
            updateTotal();
            statusLabel.setText("Build cleared.");
        });

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setBackground(PANEL);
        buttons.add(compatibilityButton);
        buttons.add(clearButton);

        rightPanel.add(configTitle, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(buttons, BorderLayout.SOUTH);

        main.add(pcPanel, BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.EAST);

        return main;
    }

    private JPanel createFooter() {

        JPanel footer = new JPanel(new BorderLayout(20, 0));
        footer.setBackground(new Color(20, 20, 25));
        footer.setBorder(new EmptyBorder(15, 25, 15, 25));

        totalLabel.setForeground(GOLD);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 22));

        statusLabel.setForeground(WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        footer.add(new JLabel("SYSTEM OVERVIEW") {{
            setForeground(WHITE);
            setFont(new Font("Arial", Font.BOLD, 15));
        }}, BorderLayout.WEST);

        footer.add(totalLabel, BorderLayout.CENTER);
        footer.add(statusLabel, BorderLayout.EAST);

        return footer;
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);
        button.setForeground(Color.BLACK);
        button.setBackground(GOLD);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(12, 14, 12, 14));

        return button;
    }

    private void loadSelections() {

        selectionPanel.removeAll();

        String[] categories = {
                "CPU", "Motherboard", "GPU", "RAM", "Cooler", "PSU", "Storage"
        };

        for (String category : categories) {

            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setBackground(CARD);
            card.setBorder(new EmptyBorder(12, 12, 12, 12));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

            JLabel categoryLabel = new JLabel(category.toUpperCase());
            categoryLabel.setForeground(GOLD);
            categoryLabel.setFont(new Font("Arial", Font.BOLD, 13));

            ComponentItem current = selected.get(category);

            JLabel itemLabel = new JLabel(
                    current == null ? "Not selected" : current.name
            );
            itemLabel.setForeground(WHITE);

            JButton chooseButton = createButton("EDIT");
            chooseButton.addActionListener(e -> chooseComponent(category));

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            textPanel.setBackground(CARD);
            textPanel.add(categoryLabel);
            textPanel.add(itemLabel);

            card.add(textPanel, BorderLayout.CENTER);
            card.add(chooseButton, BorderLayout.EAST);

            selectionPanel.add(card);
            selectionPanel.add(Box.createVerticalStrut(8));
        }

        selectionPanel.revalidate();
        selectionPanel.repaint();
    }

    private void chooseComponent(String category) {

        String sql = """
                SELECT id, name, category, socket, ram_type, price, wattage
                FROM components
                WHERE LOWER(category) = LOWER(?)
                ORDER BY price
                """;

        DefaultComboBoxModel<ComponentItem> model =
                new DefaultComboBoxModel<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    model.addElement(new ComponentItem(
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

        } catch (SQLException e) {
            showError("Could not load components: " + e.getMessage());
            return;
        }

        if (model.getSize() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No " + category + " components found in the database."
            );
            return;
        }

        JComboBox<ComponentItem> comboBox = new JComboBox<>(model);
        comboBox.setPreferredSize(new Dimension(380, 35));

        ComponentItem existing = selected.get(category);

        if (existing != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).id == existing.id) {
                    comboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                comboBox,
                "Choose " + category,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {

            ComponentItem chosen = (ComponentItem) comboBox.getSelectedItem();

            if (chosen != null) {
                selected.put(category, chosen);
                recalculateTotal();
                loadSelections();
                statusLabel.setText(category + " updated.");
            }
        }
    }

    private void recalculateTotal() {

        total = 0;

        for (ComponentItem item : selected.values()) {
            total += item.price;
        }

        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText(String.format("TOTAL: Rs. %.2f", total));
    }

    private void checkCompatibility() {

        ComponentItem cpu = selected.get("CPU");
        ComponentItem motherboard = selected.get("Motherboard");
        ComponentItem ram = selected.get("RAM");
        ComponentItem psu = selected.get("PSU");
        ComponentItem gpu = selected.get("GPU");

        StringBuilder report = new StringBuilder();

        boolean compatible = true;
        boolean fullyVerified = true;

        report.append("COMPATIBILITY REPORT\n\n");

        if (cpu == null || motherboard == null) {
            report.append("UNKNOWN: Select both CPU and motherboard.\n");
            compatible = false;
            fullyVerified = false;

        } else if (isBlank(cpu.socket) || isBlank(motherboard.socket)) {
            report.append("UNKNOWN: CPU or motherboard socket is missing.\n");
            compatible = false;
            fullyVerified = false;

        } else if (cpu.socket.equalsIgnoreCase(motherboard.socket)) {
            report.append("PASS: CPU socket matches motherboard.\n");

        } else {
            report.append("FAIL: CPU socket mismatch.\n");
            compatible = false;
        }

        if (ram != null && motherboard != null) {

            if (isBlank(ram.ramType) || isBlank(motherboard.ramType)) {
                report.append("UNKNOWN: RAM type information is missing.\n");
                compatible = false;
                fullyVerified = false;

            } else if (ram.ramType.equalsIgnoreCase(motherboard.ramType)) {
                report.append("PASS: RAM type matches motherboard.\n");

            } else {
                report.append("FAIL: RAM type mismatch.\n");
                compatible = false;
            }

        } else {
            report.append("UNKNOWN: Select RAM and motherboard.\n");
            compatible = false;
            fullyVerified = false;
        }

        if (cpu != null && gpu != null && psu != null) {

            if (cpu.wattage <= 0 || gpu.wattage <= 0 || psu.wattage <= 0) {
                report.append("UNKNOWN: Wattage data is missing.\n");
                compatible = false;
                fullyVerified = false;

            } else if (psu.wattage >= cpu.wattage + gpu.wattage) {
                report.append("PASS: PSU covers listed CPU and GPU wattage.\n");

            } else {
                report.append("FAIL: PSU wattage is below their combined wattage.\n");
                compatible = false;
            }

        } else {
            report.append("UNKNOWN: Select CPU, GPU and PSU to check power.\n");
            compatible = false;
            fullyVerified = false;
        }

        report.append("\n");

        if (compatible && fullyVerified) {
            report.append("RESULT: All implemented checks passed.");
        } else {
            report.append("RESULT: Failed or incomplete checks. Verify before buying.");
        }

        JTextArea area = new JTextArea(report.toString(), 13, 45);
        area.setEditable(false);
        area.setBackground(PANEL);
        area.setForeground(WHITE);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Compatibility Results",
                JOptionPane.INFORMATION_MESSAGE
        );

        statusLabel.setText(
                compatible && fullyVerified
                        ? "Compatibility checks passed."
                        : "Compatibility failed or needs verification."
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Smart PC Builder",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PCBuilderGUI gui = new PCBuilderGUI();
            gui.setVisible(true);
        });
    }
}
