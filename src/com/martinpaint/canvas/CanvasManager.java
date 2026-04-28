package com.martinpaint.canvas;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/*
 * This class will be refactored in the future to delegate drawing to a
 * LayerManager so that multiple layers can be composited together.
 * {@link #getGraphicsContext()} will return the currently
 * active layer GraphicsContext rather than the single backing canvas.
 */
public class CanvasManager {

    public static final double CANVAS_SIZE = 1024;

    private final Canvas canvas;
    private final GraphicsContext gc;

    public CanvasManager() {
        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        gc = canvas.getGraphicsContext2D();
        clear();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    public void clear() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public WritableImage getSnapshot() {
        return canvas.snapshot(null, null);
    }
}
