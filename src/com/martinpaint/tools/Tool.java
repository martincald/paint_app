package com.martinpaint.tools;

import com.martinpaint.color.ColorManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Base class for drawing tools.
public abstract class Tool {

    protected ColorManager colorManager;

    public void configure(ColorManager colorManager) {
        this.colorManager = colorManager;
    }

    public abstract void onMousePressed(double x, double y, GraphicsContext gc);
    public abstract void onMouseDragged(double x, double y, GraphicsContext gc);
    public abstract void onMouseReleased(double x, double y, GraphicsContext gc);

    // Called when the tool is activated.
    public void onActivated() {}

    // Called when the tool is deactivated.
    public void onDeactivated() {}

    public abstract String getName();
    public abstract Image getIcon();
}
