package com.martinpaint.canvas;

import javafx.beans.property.*;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

public class Layer {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final StringProperty name;
    private final BooleanProperty visible;
    private final DoubleProperty opacity;

    public Layer(String name) {
        this.canvas = new Canvas(CanvasManager.CANVAS_SIZE, CanvasManager.CANVAS_SIZE);
        this.gc = this.canvas.getGraphicsContext2D();
        this.name = new SimpleStringProperty(name);
        this.visible = new SimpleBooleanProperty(true);
        this.opacity = new SimpleDoubleProperty(1.0);
    }

    public Canvas getCanvas()          { return canvas; }
    public GraphicsContext getGc()     { return gc; }

    public StringProperty nameProperty()    { return name; }
    public String getName()                 { return name.get(); }
    public void setName(String n)           { name.set(n); }

    public BooleanProperty visibleProperty() { return visible; }
    public boolean isVisible()               { return visible.get(); }
    public void setVisible(boolean v)        { visible.set(v); }

    public DoubleProperty opacityProperty()  { return opacity; }
    public double getOpacity()               { return opacity.get(); }
    public void setOpacity(double o)         { opacity.set(Math.clamp(o, 0.0, 1.0)); }

    /** Clears all pixels to transparent. */
    public void clear() {
        gc.clearRect(0, 0, CanvasManager.CANVAS_SIZE, CanvasManager.CANVAS_SIZE);
    }

    /** Returns raw layer pixels, ignoring display opacity/visibility bindings. */
    public WritableImage snapshot() {
        return snapshot(null, new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight()));
    }

    public WritableImage snapshot(SnapshotParameters params, WritableImage target) {
        boolean opacityBound = canvas.opacityProperty().isBound();
        boolean visibleBound = canvas.visibleProperty().isBound();
        double oldOpacity = canvas.getOpacity();
        boolean oldVisible = canvas.isVisible();

        if (opacityBound) canvas.opacityProperty().unbind();
        if (visibleBound) canvas.visibleProperty().unbind();

        try {
            canvas.setOpacity(1.0);
            canvas.setVisible(true);
            return canvas.snapshot(params == null ? CanvasManager.transparentSnapshotParameters() : params, target);
        } finally {
            canvas.setOpacity(oldOpacity);
            canvas.setVisible(oldVisible);
            if (opacityBound) canvas.opacityProperty().bind(opacity);
            if (visibleBound) canvas.visibleProperty().bind(visible);
        }
    }
}
