package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorUtils;
import com.martinpaint.io.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;

// Bucket fill via iterative flood-fill on an int[] ARGB buffer
public class FillTool extends Tool {

    private int tolerance = 30;

    public int getTolerance() { return tolerance; }
    public void setTolerance(int tolerance) { this.tolerance = tolerance; }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        if (colorManager == null) return;

        int startX = (int) x;
        int startY = (int) y;
        int width  = (int) gc.getCanvas().getWidth();
        int height = (int) gc.getCanvas().getHeight();
        if (startX < 0 || startX >= width || startY < 0 || startY >= height) return;

        Color fillColor = colorManager.getCurrentColor();
        if (fillColor == null) return;

        WritableImage snapshot = CanvasManager.snapshotUnscaled(gc.getCanvas());
        PixelReader reader = snapshot.getPixelReader();
        if (reader == null) return;

        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);

        int fillArgb   = ColorUtils.toArgb(fillColor);
        int targetArgb = pixels[startY * width + startX];
        if (fillArgb == targetArgb) return;

        floodFill(pixels, width, height, startX, startY, targetArgb, fillArgb);

        snapshot.getPixelWriter().setPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        gc.drawImage(snapshot, 0, 0);
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) { }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) { }

    @Override
    public String getName() { return "Fill"; }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/bucket.png");
    }

    private void floodFill(int[] pixels, int width, int height,
                           int startX, int startY, int targetArgb, int fillArgb) {
        boolean[] visited = new boolean[width * height];
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(startY * width + startX);

        while (!stack.isEmpty()) {
            int idx = stack.pop();
            if (visited[idx]) continue;
            visited[idx] = true;

            if (!argbMatch(pixels[idx], targetArgb)) continue;
            pixels[idx] = fillArgb;

            int px = idx % width;
            int py = idx / width;
            if (px + 1 < width)  stack.push(idx + 1);
            if (px - 1 >= 0)     stack.push(idx - 1);
            if (py + 1 < height) stack.push(idx + width);
            if (py - 1 >= 0)     stack.push(idx - width);
        }
    }

    private boolean argbMatch(int a, int b) {
        return Math.abs(((a >> 24) & 0xFF) - ((b >> 24) & 0xFF)) <= tolerance
            && Math.abs(((a >> 16) & 0xFF) - ((b >> 16) & 0xFF)) <= tolerance
            && Math.abs(((a >>  8) & 0xFF) - ((b >>  8) & 0xFF)) <= tolerance
            && Math.abs(( a        & 0xFF) - ( b        & 0xFF)) <= tolerance;
    }
}
