package com.example;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.fxyz3d.importers.Model3D;
import org.fxyz3d.importers.Importer3D;

import java.io.IOException;
import java.net.URL;

public final class GamingPCModel {

    private GamingPCModel() {
    }

    /**
     * Creates a JavaFX 3D view of the gaming PC OBJ model.
     *
     * Expected resources:
     * /models/gaming-pc/CUSTOM GAMING PC.obj
     *
     * Keep the OBJ, MTL and referenced texture files together.
     */
    public static SubScene create3DView(double width, double height)
            throws IOException {

        URL modelUrl = GamingPCModel.class.getResource(
                "/models/gaming-pc/CUSTOM GAMING PC.obj"
        );

        if (modelUrl == null) {
            throw new IOException(
                    "Gaming PC model not found. Check src/main/resources/models/gaming-pc/"
            );
        }

        // Load the OBJ model.
        Model3D model = Importer3D.load(modelUrl);

        Node modelNode = model.getRoot();

        // Wrap the imported model so we can position and rotate it.
        Group modelGroup = new Group(modelNode);

        Bounds bounds = modelGroup.getBoundsInLocal();

        double modelWidth = bounds.getWidth();
        double modelHeight = bounds.getHeight();
        double modelDepth = bounds.getDepth();

        double largestDimension = Math.max(
                modelWidth,
                Math.max(modelHeight, modelDepth)
        );

        if (largestDimension <= 0) {
            throw new IOException("The imported 3D model has invalid dimensions.");
        }

        // Scale the model to a consistent size.
        double scaleFactor = 220.0 / largestDimension;

        Scale scale = new Scale(
                scaleFactor,
                scaleFactor,
                scaleFactor
        );

        modelGroup.getTransforms().add(scale);

        // Center the model around the origin.
        modelGroup.getTransforms().add(
                new Translate(
                        -(bounds.getMinX() + modelWidth / 2.0),
                        -(bounds.getMinY() + modelHeight / 2.0),
                        -(bounds.getMinZ() + modelDepth / 2.0)
                )
        );

        // Rotation controls.
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        Rotate rotateX = new Rotate(-8, Rotate.X_AXIS);

        modelGroup.getTransforms().addAll(rotateY, rotateX);

        Group world = new Group(modelGroup);

        // Lighting.
        AmbientLight ambientLight = new AmbientLight(
                Color.color(0.72, 0.72, 0.78)
        );

        PointLight goldLight = new PointLight(
                Color.rgb(255, 190, 75)
        );
        goldLight.setTranslateX(-220);
        goldLight.setTranslateY(-150);
        goldLight.setTranslateZ(-250);

        PointLight whiteLight = new PointLight(Color.WHITE);
        whiteLight.setTranslateX(180);
        whiteLight.setTranslateY(-80);
        whiteLight.setTranslateZ(-180);

        world.getChildren().addAll(ambientLight, goldLight, whiteLight);

        SubScene subScene = new SubScene(
                world,
                width,
                height,
                true,
                SceneAntialiasing.BALANCED
        );

        subScene.setFill(Color.rgb(15, 17, 23));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setTranslateZ(-600);

        subScene.setCamera(camera);

        // Rotate the model by dragging the mouse.
        final double[] mousePosition = new double[2];

        subScene.setOnMousePressed(event -> {
            mousePosition[0] = event.getSceneX();
            mousePosition[1] = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mousePosition[0];
            double deltaY = event.getSceneY() - mousePosition[1];

            rotateY.setAngle(rotateY.getAngle() + deltaX * 0.7);

            double newAngle = rotateX.getAngle() - deltaY * 0.5;
            rotateX.setAngle(Math.max(-80, Math.min(80, newAngle)));

            mousePosition[0] = event.getSceneX();
            mousePosition[1] = event.getSceneY();
        });

        return subScene;
    }
}

