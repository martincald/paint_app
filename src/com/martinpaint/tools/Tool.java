package com.martinpaint.tools;

import com.martinpaint.color.ColorManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class Tool {

    protected ColorManager colorManager;
    protected double brushSize;

    public void configure(ColorManager colorManager, double brushSize) {
        this.colorManager = colorManager;
        this.brushSize = brushSize;
    }

    public abstract void onMousePressed(double x, double y, GraphicsContext gc);

    public abstract void onMouseDragged(double x, double y, GraphicsContext gc);

    public abstract void onMouseReleased(double x, double y, GraphicsContext gc);

    public abstract String getName();

    public abstract Image getIcon();
}