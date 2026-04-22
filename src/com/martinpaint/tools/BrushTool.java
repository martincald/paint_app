package com.martinpaint.tools;

import com.martinpaint.io.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class BrushTool extends Tool {

    private double lastX;
    private double lastY;

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        lastX = x;
        lastY = y;
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        gc.setStroke(colorManager.getCurrentColor());
        gc.setLineWidth(brushSize);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        gc.beginPath();
        gc.moveTo(lastX, lastY);
        gc.lineTo(x, y);
        gc.stroke();

        lastX = x;
        lastY = y;
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {
        //Placeholder for future brush functionality
    }

    @Override
    public String getName() {
        return "Brush";
    }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/paint-brush.png");
    }
}