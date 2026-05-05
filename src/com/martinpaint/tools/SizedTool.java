package com.martinpaint.tools;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

// Base class for stroke style tools that share size + last-point logic
public abstract class SizedTool extends Tool {

    private double size;
    private double lastX;
    private double lastY;

    protected SizedTool(double initialSize) {
        this.size = initialSize;
    }

    public double getSize() { return size; }
    public void setSize(double size) { this.size = size; }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        lastX = x;
        lastY = y;
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        gc.setStroke(strokeColor());
        gc.setLineWidth(size);
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
    public void onMouseReleased(double x, double y, GraphicsContext gc) { }

    // The color used to draw the stroke for this tool.
    protected abstract Color strokeColor();
}
