package com.martinpaint.ui;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.canvas.Layer;
import com.martinpaint.canvas.LayerManager;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.collections.ListChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import java.util.ArrayList;
import java.util.List;

/** Scrollable, zoomable canvas viewport. */
public class CanvasViewport extends ScrollPane {

    public enum NavMode { NONE, PAN, ZOOM }

    private static final double PADDING_X = 1300.0;
    private static final double PADDING_Y = 680.0;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 7.0;
    private static final double INITIAL_SCALE = 0.5;

    // How aggressively the current scale follows the target each frame
    private static final double ZOOM_SMOOTHING = 0.20;
    // Stop the animation once we are within this distance of the target scale
    private static final double ZOOM_EPSILON = 0.0005;

    private final Group      canvasGroup;
    private final StackPane  workspace;
    private final Scale      scale;
    private final List<Node> overlayNodes = new ArrayList<>();

    // Key-event handler for the active selection tool (set via setKeyHandler)
    private javafx.event.EventHandler<javafx.scene.input.KeyEvent> selectionKeyHandler;

    private NavMode navMode = NavMode.NONE;
    private double panStartX, panStartY, panStartH, panStartV;

    private double targetScale = INITIAL_SCALE;
    private double anchorSceneX;
    private double anchorSceneY;
    private AnimationTimer zoomTimer;

    public CanvasViewport(CanvasManager canvasManager) {
        getStyleClass().add("canvas-viewport");

        // Background, layer canvases, interaction canvas, preview, and overlays zoom together as one group.
        canvasGroup = new Group();
        Canvas bgCanvas          = canvasManager.getBackgroundCanvas();
        Canvas interactionCanvas = canvasManager.getCanvas();
        Canvas prevCanvas        = canvasManager.getPreviewCanvas();
        LayerManager layerManager = canvasManager.getLayerManager();

        // Build the initial z-ordered group: background → layers → interaction → preview
        rebuildCanvasGroup(bgCanvas, interactionCanvas, prevCanvas, layerManager);

        // Keep group in sync whenever the layer list changes (add/remove/reorder/undo)
        layerManager.getLayers().addListener((ListChangeListener<Layer>) _ ->
                rebuildCanvasGroup(bgCanvas, interactionCanvas, prevCanvas, layerManager));

        canvasGroup.setFocusTraversable(true);
        scale = new Scale(INITIAL_SCALE, INITIAL_SCALE, 0, 0);
        canvasGroup.getTransforms().add(scale);

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

        // Center the canvas the first time the ScrollPane's viewport is fully laid out.
        // viewportBoundsProperty() is the authoritative signal: it is updated by
        // ScrollPaneSkin.layoutChildren() — the exact moment getViewportBounds() returns
        // valid, non-zero dimensions. Platform.runLater races against that skin pass and
        // can fire before it completes, producing a stale midpoint. This listener fires
        // once, centers, then removes itself so it never interferes with zoom/pan.
        ChangeListener<Bounds> centerOnce = new ChangeListener<>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Bounds> obs,
                                Bounds oldBounds, Bounds newBounds) {
                if (newBounds.getWidth() > 0 && newBounds.getHeight() > 0) {
                    setHvalue((getHmax() + getHmin()) / 2.0);
                    setVvalue((getVmax() + getVmin()) / 2.0);
                    viewportBoundsProperty().removeListener(this);
                }
            }
        };
        viewportBoundsProperty().addListener(centerOnce);

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

        sceneProperty().addListener((_, _, scene) -> {
            if (scene == null) stopZoomTimer();
        });

        // Navigation tool handlers (Hand pan, Zoom click).
        workspace.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (navMode == NavMode.PAN) {
                panStartX = e.getSceneX(); panStartY = e.getSceneY();
                panStartH = getHvalue();   panStartV = getVvalue();
                workspace.setCursor(Cursor.CLOSED_HAND);
                e.consume();
            } else if (navMode == NavMode.ZOOM) {
                double factor = (e.isShiftDown() || e.isAltDown()) ? 0.8 : 1.25;
                requestZoom(factor, e.getSceneX(), e.getSceneY());
                e.consume();
            }
        });
        workspace.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (navMode == NavMode.PAN) {
                double dx = e.getSceneX() - panStartX;
                double dy = e.getSceneY() - panStartY;
                Bounds vp = getViewportBounds();
                Bounds ct = getContent().getBoundsInLocal();
                double hRange = Math.max(1, ct.getWidth()  - vp.getWidth());
                double vRange = Math.max(1, ct.getHeight() - vp.getHeight());
                setHvalue(Math.clamp(panStartH - dx / hRange, 0.0, 1.0));
                setVvalue(Math.clamp(panStartV - dy / vRange, 0.0, 1.0));
                e.consume();
            }
        });
        workspace.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (navMode == NavMode.PAN) {
                workspace.setCursor(Cursor.OPEN_HAND);
                e.consume();
            }
        });
    }

    /**
     * Rebuilds canvasGroup children in the correct z-order:
     *   [backgroundCanvas, layer[0].canvas, ..., layer[N-1].canvas, interactionCanvas, previewCanvas, ...overlays]
     * Called on construction and whenever the layer list changes.
     */
    private void rebuildCanvasGroup(Canvas bg, Canvas interaction, Canvas preview,
                                    LayerManager layerManager) {
        // Unbind opacity/visibility from any layer canvases currently in the group
        for (Node child : canvasGroup.getChildren()) {
            if (child instanceof Canvas c && c != bg && c != interaction && c != preview) {
                c.opacityProperty().unbind();
                c.visibleProperty().unbind();
            }
        }

        List<Node> newChildren = new ArrayList<>();
        newChildren.add(bg);
        for (Layer layer : layerManager.getLayers()) {
            Canvas c = layer.getCanvas();
            c.opacityProperty().bind(layer.opacityProperty());
            c.visibleProperty().bind(layer.visibleProperty());
            newChildren.add(c);
        }
        newChildren.add(interaction);
        newChildren.add(preview);
        newChildren.addAll(overlayNodes);   // selection overlays stay on top

        canvasGroup.getChildren().setAll(newChildren);
    }

    // Adds an overlay node with the same scale.
    public void addCanvasOverlay(Node overlay) {
        if (overlayNodes.contains(overlay)) return;
        overlayNodes.add(overlay);
        canvasGroup.getChildren().add(overlay);
    }

    // Observable zoom scale (read-only; 1.0 = 100%).
    public ReadOnlyDoubleProperty zoomProperty() {
        return scale.xProperty();
    }

    // Zooms in one step (~25%) anchored at the viewport center.
    public void zoomIn() {
        Point2D sc = viewportCenter();
        requestZoom(1.25, sc.getX(), sc.getY());
    }

    // Zooms out one step (~20%) anchored at the viewport center.
    public void zoomOut() {
        Point2D sc = viewportCenter();
        requestZoom(0.8, sc.getX(), sc.getY());
    }

    // Zooms to an absolute scale (e.g., 1.0 = 100%) anchored at the viewport center.
    public void zoomTo(double absoluteScale) {
        double current = scale.getX();
        if (current == 0) return;
        Point2D sc = viewportCenter();
        requestZoom(absoluteScale / current, sc.getX(), sc.getY());
    }

    private Point2D viewportCenter() {
        Bounds vp = getViewportBounds();
        return localToScene(vp.getWidth() / 2.0, vp.getHeight() / 2.0);
    }

    public void setNavMode(NavMode mode) {
        this.navMode = mode;
        switch (mode) {
            case PAN  -> workspace.setCursor(Cursor.OPEN_HAND);
            case ZOOM -> workspace.setCursor(Cursor.CROSSHAIR);
            default   -> workspace.setCursor(Cursor.DEFAULT);
        }
    }

    // Sets a key-event handler for forwarded events.
    // Pass null to remove it.
    public void setSelectionKeyHandler(
            javafx.event.EventHandler<javafx.scene.input.KeyEvent> handler) {
        this.selectionKeyHandler = handler;
    }

    // Starts the zoom animation.
    private void requestZoom(double zoomFactor, double sceneX, double sceneY) {
        targetScale = Math.clamp(targetScale * zoomFactor, MIN_SCALE, MAX_SCALE);
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
            stopZoomTimer();
            return;
        }
        double next = current + diff * ZOOM_SMOOTHING;
        applyScale(next, anchorSceneX, anchorSceneY);
    }

    private void stopZoomTimer() {
        if (zoomTimer != null) {
            zoomTimer.stop();
        }
    }

    // Applies an absolute scale value while keeping the given scene point stable.
    private void applyScale(double newScale, double sceneX, double sceneY) {
        double oldScale = scale.getX();
        if (newScale == oldScale) return;

        double baseWidth  = canvasGroup.getLayoutBounds().getWidth();
        double baseHeight = canvasGroup.getLayoutBounds().getHeight();
        double widthOld  = baseWidth  * oldScale + PADDING_X * 2;
        double heightOld = baseHeight * oldScale + PADDING_Y * 2;
        double hValOld = getHvalue();
        double vValOld = getVvalue();

        Point2D mouseInWorkspace = workspace.sceneToLocal(sceneX, sceneY);

        scale.setX(newScale);
        scale.setY(newScale);

        double widthNew  = baseWidth  * newScale + PADDING_X * 2;
        double heightNew = baseHeight * newScale + PADDING_Y * 2;

        double rx = mouseInWorkspace.getX() - widthOld  / 2.0;
        double ry = mouseInWorkspace.getY() - heightOld / 2.0;
        double newMouseX = widthNew  / 2.0 + rx * (newScale / oldScale);
        double newMouseY = heightNew / 2.0 + ry * (newScale / oldScale);

        Bounds vp = getViewportBounds();
        double vW = vp.getWidth();
        double vH = vp.getHeight();

        if (widthNew > vW) {
            double hNew = (newMouseX - mouseInWorkspace.getX() + hValOld * (widthOld - vW)) / (widthNew - vW);
            setHvalue(Math.clamp(hNew, 0.0, 1.0));
        }
        if (heightNew > vH) {
            double vNew = (newMouseY - mouseInWorkspace.getY() + vValOld * (heightOld - vH)) / (heightNew - vH);
            setVvalue(Math.clamp(vNew, 0.0, 1.0));
        }
    }

}
