package com.martinpaint.tools;

import com.martinpaint.io.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

// Eraser tool clears the drawing layer.
public class EraserTool extends SizedTool {

    public EraserTool() { super(20.0); }

    private double lastX;
    private double lastY;
    private boolean hasLast;

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        lastX = x;
        lastY = y;
        hasLast = true;
        eraseAt(x, y, gc);
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        if (!hasLast) {
            eraseAt(x, y, gc);
            lastX = x; lastY = y; hasLast = true;
            return;
        }
        // Walk along the segment from last point to current to make the stroke continuous.
        double dx = x - lastX;
        double dy = y - lastY;
        double dist = Math.hypot(dx, dy);
        double step = Math.max(1.0, getSize() / 4.0);
        int steps = Math.max(1, (int) Math.ceil(dist / step));
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            eraseAt(lastX + dx * t, lastY + dy * t, gc);
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {
        hasLast = false;
    }

    private void eraseAt(double x, double y, GraphicsContext gc) {
        double s = getSize();
        gc.clearRect(x - s / 2.0, y - s / 2.0, s, s);
    }

    @Override
    public String getName() { return "Eraser"; }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/eraser.png");
    }
}
