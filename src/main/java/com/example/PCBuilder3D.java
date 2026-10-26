package com.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.io.IOException;
import java.net.URL;
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.importers.Model3D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PCBuilder3D extends Application {
    private final Map<String, ComboBox<PCComponent>> selectors = new LinkedHashMap<>();
    private final Label totalLabel = new Label("₹0");
    private final Label budgetLabel = new Label();
    private final Label compatibilityLabel = new Label();
    private final Label upgradeLabel = new Label();
    private final Label selectedCountLabel = new Label("0 / 6 components selected");
    private final TextField budgetField = new TextField("100000");
    private final ProgressBar budgetProgress = new ProgressBar(0);

    private double mouseX, mouseY;
    private final Rotate rotateX = new Rotate(-8, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);

    private static final String[] CATEGORIES = {"CPU", "Motherboard", "RAM", "GPU", "Storage", "PSU"};
    private static final String GOLD = "#D4AF37";
    private static final String BG = "#0D0F12";
    private static final String PANEL = "#171A1F";
    private static final String MUTED = "#9299A5";
    private static final String GREEN = "#55D68B";
    private static final String RED = "#FF6868";

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG + "; -fx-font-family:'Segoe UI';");
        root.setTop(createHeader());

        HBox content = new HBox(16);
        content.setPadding(new Insets(16, 20, 16, 20));
        content.setAlignment(Pos.TOP_CENTER);

        VBox showcase = createShowcase();
        VBox config = createConfiguration();
        VBox summary = createSummary();
        HBox.setHgrow(showcase, Priority.ALWAYS);
        HBox.setHgrow(config, Priority.ALWAYS);
        showcase.setMinWidth(280);
        config.setMinWidth(300);
        summary.setMinWidth(245);
        summary.setPrefWidth(275);
        summary.setMaxWidth(310);
        content.getChildren().addAll(showcase, config, summary);

        ScrollPane pageScroll = new ScrollPane(content);
        pageScroll.setFitToWidth(true);
        pageScroll.setStyle("-fx-background:#0D0F12; -fx-background-color:#0D0F12;");
        root.setCenter(pageScroll);
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 1440, 900, true);
        stage.setTitle("NEXUS | Smart PC Builder");
        stage.setMinWidth(1050);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();

        loadComponents();
        updateBuild();
    }

    private VBox createHeader() {
        Label brand = new Label("NEXUS");
        brand.setStyle("-fx-text-fill:" + GOLD + "; -fx-font-size:25px; -fx-font-weight:900;");
        Label slash = new Label("/");
        slash.setStyle("-fx-text-fill:#50545C; -fx-font-size:22px;");
        Label product = new Label("SMART PC BUILDER");
        product.setStyle("-fx-text-fill:#F4F5F7; -fx-font-size:16px; -fx-font-weight:bold;");
        HBox brandLine = new HBox(10, brand, slash, product);
        brandLine.setAlignment(Pos.CENTER_LEFT);
        Label tagline = new Label("DESIGN YOUR DREAM RIG  •  CHECK COMPATIBILITY  •  STAY ON BUDGET");
        tagline.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:10px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label status = new Label("●  SYSTEM BUILDER");
        status.setStyle("-fx-text-fill:" + GREEN + "; -fx-font-size:11px; -fx-font-weight:bold;");
        HBox line = new HBox(20, new VBox(4, brandLine, tagline), spacer, status);
        line.setAlignment(Pos.CENTER_LEFT);
        line.setPadding(new Insets(18, 25, 18, 25));
        line.setStyle("-fx-background-color:#14171B; -fx-border-color:transparent transparent #2A2D32 transparent;");
        return new VBox(line);
    }

    private VBox createShowcase() {
        VBox card = card();
        card.setMinWidth(280);
        card.setPrefWidth(470);

        Label eyebrow = eyebrow("01  /  YOUR BUILD");
        Label heading = heading("PC Showcase");
        Label hint = smallText("Drag the model to rotate it");
        VBox titleBox = new VBox(5, eyebrow, heading, hint);

        // Load the OBJ model from src/main/resources/models/gaming-pc.
        Group modelView = new Group();
        Label loadingLabel = smallText("Loading 3D gaming PC model...");
        loadingLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11px;");

        try {
            URL modelUrl = PCBuilder3D.class.getResource(
                    "/models/gaming-pc/CUSTOM_GAMING_PC.obj"
            );

            if (modelUrl == null) {
                throw new IOException(
                        "Model not found at /models/gaming-pc/CUSTOM_GAMING_PC.obj"
                );
            }

            Model3D importedModel = Importer3D.load(modelUrl);
            Group importedRoot = importedModel.getRoot();

            if (importedRoot.getChildren().isEmpty()) {
                throw new IOException("The OBJ file loaded but contained no visible mesh.");
            }

            // Measure the imported model and normalize its size.
            javafx.geometry.Bounds bounds = importedRoot.getBoundsInLocal();
            double modelWidth = bounds.getWidth();
            double modelHeight = bounds.getHeight();
            double modelDepth = bounds.getDepth();
            double largestDimension = Math.max(
                    modelWidth,
                    Math.max(modelHeight, modelDepth)
            );

            if (!Double.isFinite(largestDimension) || largestDimension <= 0) {
                throw new IOException("The imported model has invalid dimensions.");
            }

            // Center the model, then scale it to fit the showcase.
            Group centeredModel = new Group(importedRoot);
            centeredModel.setTranslateX(-(bounds.getMinX() + modelWidth / 2.0));
            centeredModel.setTranslateY(-(bounds.getMinY() + modelHeight / 2.0));
            centeredModel.setTranslateZ(-(bounds.getMinZ() + modelDepth / 2.0));

            double scaleFactor = 360.0 / largestDimension;
            Group scaledModel = new Group(centeredModel);
            scaledModel.setScaleX(scaleFactor);
            scaledModel.setScaleY(scaleFactor);
            scaledModel.setScaleZ(scaleFactor);

            // This model was exported with its long axis along X; rotate it upright.
            scaledModel.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));

            // Apply the same rotations controlled by the mouse-drag handler.
            modelView.getChildren().add(scaledModel);
            modelView.getTransforms().setAll(rotateX, rotateY);

            loadingLabel.setText("3D model loaded");
            loadingLabel.setStyle("-fx-text-fill:" + GREEN + "; -fx-font-size:11px;");
        } catch (Exception ex) {
            loadingLabel.setText(
                    "Could not load 3D model: " + ex.getMessage()
            );
            loadingLabel.setStyle("-fx-text-fill:" + RED + "; -fx-font-size:11px;");
            ex.printStackTrace();
        }

        SubScene modelScene = new SubScene(
                modelView, 460, 500, true, SceneAntialiasing.BALANCED
        );
        modelScene.setFill(Color.web("#111419"));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(35);
        camera.setTranslateZ(-850);
        modelScene.setCamera(camera);

        AmbientLight ambientLight = new AmbientLight(Color.color(0.38, 0.38, 0.42));
        PointLight whiteLight = new PointLight(Color.WHITE);
        whiteLight.setTranslateX(-220);
        whiteLight.setTranslateY(-160);
        whiteLight.setTranslateZ(-250);

        PointLight goldLight = new PointLight(Color.web(GOLD));
        goldLight.setTranslateX(240);
        goldLight.setTranslateY(-90);
        goldLight.setTranslateZ(-180);

        modelView.getChildren().addAll(ambientLight, whiteLight, goldLight);

        StackPane modelWrap = new StackPane(modelScene);
        modelWrap.setMinHeight(390);
        modelWrap.setPrefHeight(500);
        VBox.setVgrow(modelWrap, Priority.ALWAYS);

        // Keep the 3D viewport matched to the showcase card.
        modelScene.widthProperty().bind(modelWrap.widthProperty());
        modelScene.heightProperty().bind(modelWrap.heightProperty());

        modelScene.setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });
        modelScene.setOnMouseDragged(e -> {
            rotateY.setAngle(rotateY.getAngle()
                    + (e.getSceneX() - mouseX) * 0.45);
            rotateX.setAngle(rotateX.getAngle()
                    - (e.getSceneY() - mouseY) * 0.45);
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        Button reset = secondaryButton("Reset View");
        reset.setOnAction(e -> {
            rotateX.setAngle(-8);
            rotateY.setAngle(-20);
        });

        Label modelTag = new Label("NEXUS  /  GAMING PC MODEL");
        modelTag.setStyle("-fx-text-fill:" + GOLD
                + "; -fx-font-size:10px; -fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox controls = new HBox(10, modelTag, spacer, reset);
        controls.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(titleBox, modelWrap, loadingLabel, controls);
        return card;
    }

    private VBox createConfiguration() {
        VBox card = card();
        card.setPrefWidth(430);
        card.getChildren().addAll(eyebrow("02  /  COMPONENTS"), heading("Configure your rig"),
                smallText("Choose hardware for your ideal build."), new Separator());

        for (String category : CATEGORIES) {
            VBox field = new VBox(6);
            Label label = new Label(category.toUpperCase(Locale.ROOT));
            label.setStyle("-fx-text-fill:#C7CBD2; -fx-font-size:10px; -fx-font-weight:bold;");
            ComboBox<PCComponent> combo = new ComboBox<>();
            combo.setPromptText("Select " + category);
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setPrefHeight(39);
            combo.setStyle("-fx-background-color:#22262D; -fx-control-inner-background:#22262D; -fx-text-fill:#F5F5F5;");
            combo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(PCComponent item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                    setTextFill(Color.web("#F2F3F5"));
                    setStyle("-fx-background-color:#22262D;");
                }
            });
            selectors.put(category, combo);
            combo.setOnAction(e -> updateBuild());
            field.getChildren().addAll(label, combo);
            card.getChildren().add(field);
        }
        return card;
    }

    private VBox createSummary() {
        VBox card = card();
        card.getChildren().addAll(eyebrow("03  /  BUILD SUMMARY"), heading("Your build"), selectedCountLabel, new Separator());
        selectedCountLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11px;");
        totalLabel.setStyle("-fx-text-fill:" + GOLD + "; -fx-font-size:23px; -fx-font-weight:900;");
        Label budgetTitle = sectionTitle("YOUR BUDGET (₹)");
        budgetField.setPrefHeight(38);
        budgetField.setStyle("-fx-background-color:#22262D; -fx-control-inner-background:#22262D; -fx-text-fill:white;");
        budgetField.setOnAction(e -> updateBuild());
        Button apply = primaryButton("Update Budget");
        apply.setMaxWidth(Double.MAX_VALUE);
        apply.setOnAction(e -> updateBuild());
        budgetProgress.setMaxWidth(Double.MAX_VALUE);
        budgetProgress.setPrefHeight(12);
        budgetLabel.setWrapText(true);
        budgetLabel.setStyle("-fx-text-fill:#D9DCE1; -fx-font-size:12px;");
        compatibilityLabel.setWrapText(true);
        compatibilityLabel.setStyle("-fx-text-fill:#D9DCE1; -fx-font-size:12px;");
        upgradeLabel.setWrapText(true);
        upgradeLabel.setStyle("-fx-text-fill:#D9DCE1; -fx-font-size:12px;");
        card.getChildren().addAll(totalLabel, budgetTitle, budgetField, apply, budgetProgress, budgetLabel,
                new Separator(), sectionTitle("COMPATIBILITY"), compatibilityLabel,
                new Separator(), sectionTitle("SMART UPGRADE ADVISOR"), upgradeLabel);
        return card;
    }

    private HBox createFooter() {
        Label left = smallText("NEXUS  •  SMART PC BUILDER");
        Label right = smallText("JAVA 21  |  JAVAFX  |  MYSQL");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(12, left, spacer, right);
        footer.setPadding(new Insets(12, 25, 12, 25));
        footer.setStyle("-fx-background-color:#14171B; -fx-border-color:#2A2D32 transparent transparent transparent;");
        return footer;
    }

    private VBox card() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:" + PANEL + "; -fx-background-radius:14; -fx-border-color:#2B3038; -fx-border-radius:14;");
        return box;
    }

    private Label eyebrow(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:" + GOLD + "; -fx-font-size:10px; -fx-font-weight:bold;");
        return label;
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#F4F5F7; -fx-font-size:20px; -fx-font-weight:bold;");
        return label;
    }

    private Label smallText(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11px;");
        label.setWrapText(true);
        return label;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:" + GOLD + "; -fx-font-size:10px; -fx-font-weight:bold;");
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:" + GOLD + "; -fx-text-fill:#151515; -fx-font-weight:bold; -fx-background-radius:7; -fx-padding:10 14 10 14; -fx-cursor:hand;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:#262A31; -fx-text-fill:#E7E9ED; -fx-border-color:#454A53; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:7 11 7 11; -fx-cursor:hand;");
        return button;
    }

    private void loadComponents() {
        try {
            List<PCComponent> components = new PCComponentDAO().getAllComponents();
            for (String category : CATEGORIES) {
                List<PCComponent> filtered = components.stream()
                        .filter(c -> categoryMatches(category, c.getCategory()))
                        .collect(Collectors.toList());
                ComboBox<PCComponent> combo = selectors.get(category);
                combo.setItems(FXCollections.observableArrayList(filtered));
                if (filtered.isEmpty()) combo.setPromptText("No " + category + " found");
            }
            if (components.isEmpty()) compatibilityLabel.setText("No components found in the database.");
        } catch (SQLException exception) {
            compatibilityLabel.setText("Database error: " + exception.getMessage());
        }
    }

    private boolean categoryMatches(String requested, String actual) {
        if (actual == null) return false;
        String category = actual.trim().toUpperCase(Locale.ROOT);
        switch (requested) {
            case "CPU": return category.equals("CPU") || category.equals("PROCESSOR");
            case "Motherboard": return category.equals("MOTHERBOARD") || category.equals("MOTHER BOARD");
            case "RAM": return category.equals("RAM") || category.equals("MEMORY");
            case "GPU": return category.equals("GPU") || category.equals("GRAPHICS CARD") || category.equals("GRAPHICS");
            case "Storage": return category.equals("STORAGE") || category.equals("SSD") || category.equals("HDD");
            case "PSU": return category.equals("PSU") || category.equals("POWER SUPPLY");
            default: return false;
        }
    }

    private void updateBuild() {
        double total = 0;
        int count = 0;
        for (ComboBox<PCComponent> combo : selectors.values()) {
            PCComponent c = combo.getValue();
            if (c != null) { total += c.getPrice(); count++; }
        }
        totalLabel.setText(String.format(Locale.US, "₹%,.0f", total));
        selectedCountLabel.setText(count + " / 6 components selected");
        try {
            double budget = Double.parseDouble(budgetField.getText().trim());
            if (!Double.isFinite(budget) || budget < 0) throw new NumberFormatException();
            double remaining = budget - total;
            budgetLabel.setText(remaining >= 0
                    ? String.format(Locale.US, "Remaining: ₹%,.0f", remaining)
                    : String.format(Locale.US, "Over budget by: ₹%,.0f", -remaining));
            budgetProgress.setProgress(budget == 0 ? (total == 0 ? 0 : 1) : Math.min(1, total / budget));
            budgetProgress.setStyle("-fx-accent:" + (remaining >= 0 ? GOLD : RED) + ";");
        } catch (NumberFormatException ex) {
            budgetLabel.setText("Enter a valid non-negative budget.");
            budgetProgress.setProgress(0);
        }
        checkCompatibility();
        updateRecommendations(total);
    }

    private void checkCompatibility() {
        PCComponent cpu = selected("CPU"), board = selected("Motherboard"), ram = selected("RAM");
        PCComponent gpu = selected("GPU"), psu = selected("PSU");
        StringBuilder out = new StringBuilder();
        boolean warning = false;

        if (cpu != null && board != null) {
            String a = cpu.getSocket(), b = board.getSocket();
            if (a != null && b != null && !a.isBlank() && !b.isBlank()) {
                if (a.trim().equalsIgnoreCase(b.trim())) out.append("✓ CPU and motherboard sockets match.\n");
                else { out.append("✕ CPU and motherboard sockets differ.\n"); warning = true; }
            } else { out.append("⚠ Socket information incomplete.\n"); warning = true; }
        } else out.append("• Select CPU and motherboard to check sockets.\n");

        if (board != null && ram != null) {
            String a = board.getRamType(), b = ram.getRamType();
            if (a != null && b != null && !a.isBlank() && !b.isBlank()) {
                if (a.trim().equalsIgnoreCase(b.trim())) out.append("✓ RAM type matches motherboard.\n");
                else { out.append("✕ RAM type does not match motherboard.\n"); warning = true; }
            } else { out.append("⚠ RAM compatibility data incomplete.\n"); warning = true; }
        } else out.append("• Select motherboard and RAM to check memory.\n");

        if (psu != null) {
            if (psu.getWattage() <= 0) { out.append("⚠ PSU wattage missing; verify specification.\n"); warning = true; }
            else {
                int required = (cpu != null && cpu.getWattage() > 0 ? cpu.getWattage() : 0)
                        + (gpu != null && gpu.getWattage() > 0 ? gpu.getWattage() : 0);
                if (required > 0) {
                    int recommended = (int) Math.ceil(required * 1.5);
                    if (psu.getWattage() >= recommended) out.append("✓ PSU passes basic wattage estimate.\n");
                    else { out.append("⚠ PSU may be undersized; verify full-system needs.\n"); warning = true; }
                }
            }
        }
        compatibilityLabel.setText((warning ? "REVIEW REQUIRED\n" : "CHECKS\n") + out);
        compatibilityLabel.setStyle("-fx-text-fill:" + (warning ? RED : GREEN) + "; -fx-font-size:12px; -fx-line-spacing:4px;");
    }

    private void updateRecommendations(double total) {
        StringBuilder out = new StringBuilder();
        if (selected("RAM") == null) out.append("• Select RAM to complete the build.\n");
        else out.append("• Verify memory capacity and motherboard limits.\n");
        if (selected("Storage") == null) out.append("• Add an SSD for storage.\n");
        else out.append("• Check available M.2/SATA slots before expanding.\n");
        if (selected("GPU") == null) out.append("• Add a GPU if your workload needs one.\n");
        else out.append("• Check GPU dimensions, connectors and case clearance.\n");
        try {
            double budget = Double.parseDouble(budgetField.getText().trim());
            if (Double.isFinite(budget) && budget >= 0) {
                double remaining = budget - total;
                if (remaining > 0) out.append(String.format(Locale.US, "• Upgrade budget available: ₹%,.0f", remaining));
                else out.append("• Consider lower-cost compatible parts to meet budget.");
            }
        } catch (NumberFormatException ignored) { }
        upgradeLabel.setText(out.toString());
    }

    private PCComponent selected(String category) {
        ComboBox<PCComponent> combo = selectors.get(category);
        return combo == null ? null : combo.getValue();
    }

    private Group createPCCase() {
        Group group = new Group();
        PhongMaterial caseMat = material("#252A31");
        PhongMaterial darkMat = material("#111419");
        PhongMaterial goldMat = material(GOLD);
        PhongMaterial glassMat = material("#53616B");
        PhongMaterial boardMat = material("#17372E");
        PhongMaterial fanMat = material("#8C6A19");

        // Keep all geometry close to the origin and facing the camera (+Z).
        Box chassis = new Box(280, 430, 190);
        chassis.setMaterial(caseMat);
        Box front = new Box(250, 405, 8);
        front.setMaterial(darkMat);
        front.setTranslateZ(-98);
        Box sideGlass = new Box(245, 370, 4);
        sideGlass.setMaterial(glassMat);
        sideGlass.setTranslateX(-145);
        sideGlass.setTranslateZ(0);
        Box goldTrim = new Box(5, 365, 5);
        goldTrim.setMaterial(goldMat);
        goldTrim.setTranslateX(132);
        goldTrim.setTranslateZ(-8);
        Box base = new Box(295, 14, 205);
        base.setMaterial(goldMat);
        base.setTranslateY(222);
        Box top = new Box(270, 10, 185);
        top.setMaterial(darkMat);
        top.setTranslateY(-220);

        Cylinder fan1 = fan(-82, -118, fanMat);
        Cylinder fan2 = fan(-82, -30, fanMat);
        Cylinder fan3 = fan(-82, 58, fanMat);
        Box motherboard = new Box(105, 180, 8);
        motherboard.setMaterial(boardMat);
        motherboard.setTranslateX(15);
        motherboard.setTranslateY(-5);
        motherboard.setTranslateZ(100);
        Box gpu = new Box(120, 26, 28);
        gpu.setMaterial(material("#414750"));
        gpu.setTranslateX(10);
        gpu.setTranslateY(75);
        gpu.setTranslateZ(120);
        Box gpuStripe = new Box(88, 4, 3);
        gpuStripe.setMaterial(goldMat);
        gpuStripe.setTranslateX(10);
        gpuStripe.setTranslateY(75);
        gpuStripe.setTranslateZ(136);
        Box ram1 = new Box(9, 72, 7);
        ram1.setMaterial(goldMat);
        ram1.setTranslateX(48);
        ram1.setTranslateY(-50);
        ram1.setTranslateZ(115);
        Box ram2 = new Box(9, 72, 7);
        ram2.setMaterial(goldMat);
        ram2.setTranslateX(64);
        ram2.setTranslateY(-50);
        ram2.setTranslateZ(115);

        group.getChildren().addAll(chassis, front, sideGlass, goldTrim, base, top,
                fan1, fan2, fan3, motherboard, gpu, gpuStripe, ram1, ram2);
        group.getTransforms().setAll(rotateX, rotateY);
        group.getChildren().addAll(new AmbientLight(Color.color(0.8, 0.8, 0.8)), new PointLight(Color.WHITE), goldLight());
        return group;
    }

    private Cylinder fan(double x, double y, PhongMaterial mat) {
        Cylinder fan = new Cylinder(34, 7);
        fan.setMaterial(mat);
        fan.setTranslateX(x);
        fan.setTranslateY(y);
        fan.setTranslateZ(108);
        return fan;
    }

    private PhongMaterial material(String color) {
        return new PhongMaterial(Color.web(color));
    }

    private PointLight goldLight() {
        PointLight light = new PointLight(Color.web(GOLD));
        light.setTranslateX(330);
        light.setTranslateY(-120);
        light.setTranslateZ(350);
        return light;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

