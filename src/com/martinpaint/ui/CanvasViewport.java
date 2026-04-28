package com.martinpaint.ui;

import com.martinpaint.canvas.CanvasManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

// a scrollable, zoomable viewport that hosts the paint canvas

public class CanvasViewport extends ScrollPane {

    private static final double WORKSPACE_PADDING = 400.0;
    private static final Color WORKSPACE_COLOR = Color.web("#3A3A3A");
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 10.0;

    private final Group canvasGroup;
    private final Scale scale;

    public CanvasViewport(CanvasManager canvasManager) {
        // Group wrapping the canvas, this is what we scale
        canvasGroup = new Group(canvasManager.getCanvas());

        // Single Scale transform we keep reusing for zoom.
        scale = new Scale(1.0, 1.0, 0, 0);
        canvasGroup.getTransforms().add(scale);

        // workspace is a StackPane that centers the canvasGroup
        StackPane workspace = new StackPane(canvasGroup);
        workspace.setAlignment(Pos.CENTER);
        workspace.setPadding(new Insets(WORKSPACE_PADDING));
        workspace.setBackground(new Background(
                new BackgroundFill(WORKSPACE_COLOR, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        setContent(workspace);

        setStyle("-fx-background: #3A3A3A; -fx-background-color: #3A3A3A;");
        setPannable(false);
        setFitToWidth(false);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Trackpad zoom handler
        canvasGroup.setOnZoom(event -> {
            double factor = event.getZoomFactor();
            double newX = clamp(scale.getX() * factor, MIN_SCALE, MAX_SCALE);
            double newY = clamp(scale.getY() * factor, MIN_SCALE, MAX_SCALE);
            scale.setX(newX);
            scale.setY(newY);
            event.consume();
        });
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * @return the {@link Scale} transform applied to the canvas group (useful for
     *         programmatic zoom control, e.g. a "reset zoom" button).
     */
    public Scale getScaleTransform() {
        return scale;
    }
}