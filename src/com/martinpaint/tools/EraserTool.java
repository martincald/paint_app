package com.martinpaint.tools;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;

/** Eraser tool clears the drawing layer. */
public class EraserTool extends SizedTool {

    public EraserTool() { super(ToolSpec.ERASER); }

    // Own position tracking — SizedTool's lastX/lastY are private and unreachable here.
    private double lastX;
    private double lastY;
    private boolean hasLast;

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        saveUndoState();
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
        if (hasLast) markDrawingChanged();
        hasLast = false;
    }

    private void eraseAt(double x, double y, GraphicsContext gc) {
        double r = getSize() / 2.0;
        double r2 = r * r;
        int x0 = (int) Math.floor(x - r);
        int y0 = (int) Math.floor(y - r);
        int size = (int) Math.ceil(getSize());
        int cw = (int) gc.getCanvas().getWidth();
        int ch = (int) gc.getCanvas().getHeight();
        PixelWriter pw = gc.getPixelWriter();
        for (int dy = 0; dy < size; dy++) {
            int py = y0 + dy;
            if (py < 0 || py >= ch) continue;
            for (int dx = 0; dx < size; dx++) {
                int px = x0 + dx;
                if (px < 0 || px >= cw) continue;
                double ddx = dx - r + 0.5;
                double ddy = dy - r + 0.5;
                if (ddx * ddx + ddy * ddy <= r2) {
                    pw.setArgb(px, py, 0);
                }
            }
        }
    }
}
