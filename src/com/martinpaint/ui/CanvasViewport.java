package com.martinpaint.ui;

import com.martinpaint.canvas.CanvasManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

// Scrollable, zoomable canvas viewport.
public class CanvasViewport extends ScrollPane {

    private static final double PADDING_X = 1300.0;
    private static final double PADDING_Y = 680.0;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 7.0;
    private static final double INITIAL_SCALE = 0.5;

    // How aggressively the current scale follows the target each frame
    private static final double ZOOM_SMOOTHING = 0.20;
    // Stop the animation once we are within this distance of the target scale
    private static final double ZOOM_EPSILON = 0.0005;

    private final Group     canvasGroup;
    private final StackPane workspace;
    private final Scale     scale;

    // Key-event handler for the active selection tool (set via setKeyHandler)
    private javafx.event.EventHandler<javafx.scene.input.KeyEvent> selectionKeyHandler;

    private double targetScale = INITIAL_SCALE;
    private double anchorSceneX;
    private double anchorSceneY;
    private AnimationTimer zoomTimer;

    public CanvasViewport(CanvasManager canvasManager) {
        getStyleClass().add("canvas-viewport");

        // Background and drawing canvases share the same Scale transform.
        // They zoom together to stay aligned.
        canvasGroup = new Group(canvasManager.getBackgroundCanvas(), canvasManager.getCanvas());
        canvasGroup.setFocusTraversable(true);
        scale = new Scale(INITIAL_SCALE, INITIAL_SCALE, 0, 0);
        canvasManager.getBackgroundCanvas().getTransforms().add(scale);
        canvasManager.getCanvas().getTransforms().add(scale);

        workspace = new StackPane(canvasGroup);
        workspace.getStyleClass().add("canvas-workspace");
        workspace.setAlignment(Pos.CENTER);
        workspace.setPadding(new Insets(PADDING_Y, PADDING_X, PADDING_Y, PADDING_X));

        setContent(workspace);
        setPannable(false);
        setFitToWidth(false);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        // Center the canvas once the layout has been computed.
        javafx.application.Platform.runLater(() -> {
            workspace.applyCss();
            workspace.layout();
            setHvalue((getHmax() + getHmin()) / 2.0);
            setVvalue((getVmax() + getVmin()) / 2.0);
        });

        setOnZoom(event -> {
            requestZoom(event.getZoomFactor(), event.getSceneX(), event.getSceneY());
            event.consume();
        });

        addEventFilter(ScrollEvent.ANY, event -> {
            if (event.isShortcutDown()) {
                double delta = event.getDeltaY();
                if (delta != 0) {
                    requestZoom(Math.pow(1.005, delta), event.getSceneX(), event.getSceneY());
                }
                event.consume();
            }
        });

        // Forward key events to the selection tool.
        addEventFilter(javafx.scene.input.KeyEvent.ANY, event -> {
            if (selectionKeyHandler != null) {
                selectionKeyHandler.handle(event);
            }
        });
    }

    // Adds an overlay node with the same scale.
    public void addCanvasOverlay(Node overlay) {
        overlay.getTransforms().add(scale);
        canvasGroup.getChildren().add(overlay);
    }

    // Returns the current zoom scale.
    public double getScaleValue() {
        return scale.getX();
    }

    // Sets a key-event handler for forwarded events.
    // Pass null to remove it.
    public void setSelectionKeyHandler(
            javafx.event.EventHandler<javafx.scene.input.KeyEvent> handler) {
        this.selectionKeyHandler = handler;
    }

    // Starts the zoom animation.
    private void requestZoom(double zoomFactor, double sceneX, double sceneY) {
        targetScale = clamp(targetScale * zoomFactor, MIN_SCALE, MAX_SCALE);
        anchorSceneX = sceneX;
        anchorSceneY = sceneY;
        startZoomTimer();
    }

    private void startZoomTimer() {
        if (zoomTimer == null) {
            zoomTimer = new AnimationTimer() {
                @Override public void handle(long now) { stepZoom(); }
            };
        }
        zoomTimer.start();
    }

    private void stepZoom() {
        double current = scale.getX();
        double diff = targetScale - current;
        if (Math.abs(diff) < ZOOM_EPSILON) {
            applyScale(targetScale, anchorSceneX, anchorSceneY);
            zoomTimer.stop();
            return;
        }
        double next = current + diff * ZOOM_SMOOTHING;
        applyScale(next, anchorSceneX, anchorSceneY);
    }

    // Applies an absolute scale value while keeping the given scene point stable.
    private void applyScale(double newScale, double sceneX, double sceneY) {
        double oldScale = scale.getX();
        if (newScale == oldScale) return;

        double widthOld  = canvasGroup.getLayoutBounds().getWidth()  + PADDING_X * 2;
        double heightOld = canvasGroup.getLayoutBounds().getHeight() + PADDING_Y * 2;
        double hValOld = getHvalue();
        double vValOld = getVvalue();

        Point2D mouseInWorkspace = workspace.sceneToLocal(sceneX, sceneY);

        scale.setX(newScale);
        scale.setY(newScale);

        double widthNew  = canvasGroup.getLayoutBounds().getWidth()  + PADDING_X * 2;
        double heightNew = canvasGroup.getLayoutBounds().getHeight() + PADDING_Y * 2;

        double rx = mouseInWorkspace.getX() - widthOld  / 2.0;
        double ry = mouseInWorkspace.getY() - heightOld / 2.0;
        double newMouseX = widthNew  / 2.0 + rx * (newScale / oldScale);
        double newMouseY = heightNew / 2.0 + ry * (newScale / oldScale);

        Bounds vp = getViewportBounds();
        double vW = vp.getWidth();
        double vH = vp.getHeight();

        if (widthNew > vW) {
            double hNew = (newMouseX - mouseInWorkspace.getX() + hValOld * (widthOld - vW)) / (widthNew - vW);
            setHvalue(clamp(hNew, 0, 1));
        }
        if (heightNew > vH) {
            double vNew = (newMouseY - mouseInWorkspace.getY() + vValOld * (heightOld - vH)) / (heightNew - vH);
            setVvalue(clamp(vNew, 0, 1));
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
