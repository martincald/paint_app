package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.io.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

// Picks the color of a drawn pixel.
public class EyeDropperTool extends Tool {

    private PixelReader reader;

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        WritableImage snapshot = CanvasManager.snapshotUnscaled(gc.getCanvas());
        reader = snapshot.getPixelReader();
        pickColor(x, y, gc);
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        pickColor(x, y, gc);
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {
        reader = null;
    }

    private void pickColor(double x, double y, GraphicsContext gc) {
        if (colorManager == null || reader == null) return;

        int px = (int) x;
        int py = (int) y;
        int w  = (int) gc.getCanvas().getWidth();
        int h  = (int) gc.getCanvas().getHeight();
        if (px < 0 || px >= w || py < 0 || py >= h) return;

        Color picked = reader.getColor(px, py);
        // Skip transparent pixels.
        if (picked.getOpacity() <= 0.001) return;

        colorManager.setCurrentColor(picked);
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
