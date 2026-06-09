package com.martinpaint.tools;

import com.martinpaint.color.ColorUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Arrays;

/** Bucket fill tool. */
public class FillTool extends Tool {

    private static final int MIN_TOLERANCE = 0;
    private static final int MAX_TOLERANCE = 100;
    private static final int DEFAULT_TOLERANCE = 30;

    private int tolerance = DEFAULT_TOLERANCE;

    public FillTool() {
        super(ToolSpec.FILL);
    }

    public int getTolerance() { return tolerance; }
    public void setTolerance(int tolerance) {
        int clamped = Math.clamp(tolerance, MIN_TOLERANCE, MAX_TOLERANCE);
        if (this.tolerance != clamped) {
            this.tolerance = clamped;
            markSettingsChanged();
        }
    }

    @Override
    public void resetSettings() {
        setTolerance(DEFAULT_TOLERANCE);
    }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        if (colorManager == null || canvasManager == null) return;

        int startX = (int) x;
        int startY = (int) y;
        int width  = (int) gc.getCanvas().getWidth();
        int height = (int) gc.getCanvas().getHeight();
        if (startX < 0 || startX >= width || startY < 0 || startY >= height) return;

        Color fillColor = colorManager.getCurrentColor();
        if (fillColor == null) return;

        WritableImage snapshot = canvasManager.getLayerManager().getActiveLayer().snapshot();
        PixelReader reader = snapshot.getPixelReader();
        if (reader == null) return;

        int fillArgb   = ColorUtils.toArgb(fillColor);
        int targetArgb = reader.getArgb(startX, startY);
        int tol = getTolerance();
        if (argbMatch(fillArgb, targetArgb, tol)) return;

        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);

        saveUndoState();
        floodFill(pixels, width, height, startX, startY, targetArgb, fillArgb, tol);

        snapshot.getPixelWriter().setPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        gc.drawImage(snapshot, 0, 0);
        markDrawingChanged();
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) { }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) { }

    private void floodFill(int[] pixels, int width, int height,
                           int startX, int startY, int targetArgb, int fillArgb, int tolerance) {
        if (argbMatch(fillArgb, targetArgb, tolerance)) return;

        IntStack stack = new IntStack();
        stack.push(startY * width + startX);

        while (!stack.isEmpty()) {
            int index = stack.pop();
            int y = index / width;
            int x = index - y * width;

            int x1 = x;
            int row = y * width;
            while (x1 >= 0 && argbMatch(pixels[row + x1], targetArgb, tolerance)) x1--;
            x1++;

            boolean spanAbove = false;
            boolean spanBelow = false;

            while (x1 < width && argbMatch(pixels[row + x1], targetArgb, tolerance)) {
                pixels[row + x1] = fillArgb;

                if (!spanAbove && y > 0 && argbMatch(pixels[row - width + x1], targetArgb, tolerance)) {
                    stack.push(row - width + x1);
                    spanAbove = true;
                } else if (spanAbove && y > 0 && !argbMatch(pixels[row - width + x1], targetArgb, tolerance)) {
                    spanAbove = false;
                }

                if (!spanBelow && y < height - 1 && argbMatch(pixels[row + width + x1], targetArgb, tolerance)) {
                    stack.push(row + width + x1);
                    spanBelow = true;
                } else if (spanBelow && y < height - 1 && !argbMatch(pixels[row + width + x1], targetArgb, tolerance)) {
                    spanBelow = false;
                }
                x1++;
            }
        }
    }

    private static boolean argbMatch(int a, int b, int tolerance) {
        if (a == b) return true;
        if (tolerance == 0) return false;
        return Math.abs(((a >> 24) & 0xFF) - ((b >> 24) & 0xFF)) <= tolerance
            && Math.abs(((a >> 16) & 0xFF) - ((b >> 16) & 0xFF)) <= tolerance
            && Math.abs(((a >>  8) & 0xFF) - ((b >>  8) & 0xFF)) <= tolerance
            && Math.abs(( a        & 0xFF) - ( b        & 0xFF)) <= tolerance;
    }

    private static final class IntStack {
        private int[] values = new int[4096];
        private int size;

        void push(int value) {
            if (size == values.length) {
                values = Arrays.copyOf(values, values.length * 2);
            }
            values[size++] = value;
        }

        int pop() {
            return values[--size];
        }

        boolean isEmpty() {
            return size == 0;
        }
    }
}
