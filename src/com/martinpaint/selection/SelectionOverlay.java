package com.martinpaint.selection;

import com.martinpaint.canvas.CanvasManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

// Transparent overlay rendered above the canvas
public class SelectionOverlay extends Pane {

    private final Rectangle marquee   = new Rectangle();
    private final Circle[]   handles   = new Circle[ResizeHandle.values().length];
    private final ImageView floatView = new ImageView();

    // Callbacks set by SelectionController
    private Consumer<ResizeHandle> onResizeStart;
    private Consumer<double[]>     onResizeDrag;   // double[]{x, y}
    private Runnable               onResizeEnd;

    public SelectionOverlay() {
        this(CanvasManager.CANVAS_SIZE, CanvasManager.CANVAS_SIZE);
    }

    public SelectionOverlay(double canvasWidth, double canvasHeight) {
        setPrefSize(canvasWidth, canvasHeight);
        setMaxSize(canvasWidth, canvasHeight);
        setPickOnBounds(false);

        buildMarquee();
        buildFloatView();
        buildHandles();

        getChildren().addAll(floatView, marquee);
        getChildren().addAll(handles);
        setVisible(false);
    }

    private void buildMarquee() {
        marquee.getStyleClass().add("selection-marquee");
        marquee.setMouseTransparent(true);
    }

    private void buildFloatView() {
        floatView.setPreserveRatio(false);
        floatView.setMouseTransparent(true);
    }

    private void buildHandles() {
        for (ResizeHandle rh : ResizeHandle.values()) {
            Circle c = new Circle(5.0);
            c.getStyleClass().add("selection-handle");
            c.setVisible(false);
            handles[rh.ordinal()] = c;
        }
    }

// Public API

    // Shows the dashed marquee during defining phase
    public void showMarquee(double x, double y, double w, double h) {
        setVisible(true);
        floatView.setVisible(false);
        updateMarquee(x, y, w, h);
        setHandlesVisible(false);
    }

    // Call once when entering floating: sets the image and makes everything visible.
    // Use updateFloatBounds on subsequent drag/nudge/resize ticks.
    public void showFloat(Selection selection) {
        setVisible(true);
        floatView.setImage(selection.getFloatImage());
        floatView.setVisible(true);
        updateFloatBounds(selection.getFloatBounds());
        setHandlesVisible(true);
    }

    // Updates position/size of the float image, marquee, and handles without reassigning the image
    public void updateFloatBounds(Rectangle2D b) {
        floatView.setX(b.getMinX());
        floatView.setY(b.getMinY());
        floatView.setFitWidth(b.getWidth());
        floatView.setFitHeight(b.getHeight());
        updateMarquee(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        positionHandles(b);
    }

    // Hides the overlay
    public void hide() {
        setVisible(false);
    }

// Interaction wiring

    //Wires resize handle events.
    //Move detection is handled entirely in SelectionController via onCanvasPressed/Dragged.
    public void wireInteraction(
            Consumer<ResizeHandle> onResizeStart,
            Consumer<double[]>     onResizeDrag,
            Runnable               onResizeEnd
    ) {
        this.onResizeStart = onResizeStart;
        this.onResizeDrag  = onResizeDrag;
        this.onResizeEnd   = onResizeEnd;

        for (ResizeHandle rh : ResizeHandle.values()) {
            wireHandle(handles[rh.ordinal()], rh);
        }
    }

    private void wireHandle(Circle handle, ResizeHandle rh) {
        handle.setCursor(rh.getCursor());

        handle.setOnMousePressed(e -> {
            e.consume();
            if (onResizeStart != null) onResizeStart.accept(rh);
        });

        // Convert handle-local coords to canvas (parent) space, then forward to controller
        handle.setOnMouseDragged(e -> {
            e.consume();
            javafx.geometry.Point2D p = handle.localToParent(e.getX(), e.getY());
            if (onResizeDrag != null) onResizeDrag.accept(new double[]{p.getX(), p.getY()});
        });

        handle.setOnMouseReleased(e -> {
            e.consume();
            if (onResizeEnd != null) onResizeEnd.run();
        });
    }

// Internal layout helpers

    private void updateMarquee(double x, double y, double w, double h) {
        marquee.setX(x);
        marquee.setY(y);
        marquee.setWidth(w);
        marquee.setHeight(h);
    }

    private void positionHandles(Rectangle2D b) {
        for (ResizeHandle rh : ResizeHandle.values()) {
            placeHandle(handles[rh.ordinal()], rh.handleX(b), rh.handleY(b));
        }
    }

    private void placeHandle(Circle c, double cx, double cy) {
        c.setCenterX(cx);
        c.setCenterY(cy);
    }

    private void setHandlesVisible(boolean v) {
        for (Circle c : handles) {
            c.setVisible(v);
        }
    }
}
