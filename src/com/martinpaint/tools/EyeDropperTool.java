package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.io.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

// Picks the color of a drawn pixel. Pure white (the canvas background) is ignored
public class EyeDropperTool extends Tool {

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        pickColor(x, y, gc);
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        pickColor(x, y, gc);
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) { }

    private void pickColor(double x, double y, GraphicsContext gc) {
        if (colorManager == null) return;

        int px = (int) x;
        int py = (int) y;
        int w  = (int) gc.getCanvas().getWidth();
        int h  = (int) gc.getCanvas().getHeight();
        if (px < 0 || px >= w || py < 0 || py >= h) return;

        WritableImage snapshot = CanvasManager.snapshotUnscaled(gc.getCanvas());
        PixelReader reader = snapshot.getPixelReader();
        if (reader == null) return;

        Color picked = reader.getColor(px, py);
        // Skip the canvas background so the eyedropper only samples drawn ink.
        if (isCanvasBackground(picked)) return;

        colorManager.setCurrentColor(picked);
    }

    private static boolean isCanvasBackground(Color c) {
        return c.getRed() >= 0.999 && c.getGreen() >= 0.999 && c.getBlue() >= 0.999 && c.getOpacity() >= 0.999;
    }

    @Override
    public String getName() {
        return "Eye Dropper";
    }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/eyedropper.png");
    }
}
