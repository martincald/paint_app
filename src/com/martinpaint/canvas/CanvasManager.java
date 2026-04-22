package com.martinpaint.canvas;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class CanvasManager {

    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;

    private final Canvas canvas;
    private final GraphicsContext gc;

    public CanvasManager() {
        canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
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

    public void resize(double width, double height) {
        WritableImage snapshot = getSnapshot();

        canvas.setWidth(width);
        canvas.setHeight(height);

        clear();
        gc.drawImage(snapshot, 0, 0);
    }

    public WritableImage getSnapshot() {
        return canvas.snapshot(null, null);
    }
}
