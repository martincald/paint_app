package com.martinpaint.tools;

import com.martinpaint.color.ColorManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Base contract for any drawing tool, ui is handled by ToolSettingsContainer.
public abstract class Tool {

    protected ColorManager colorManager;

    public void configure(ColorManager colorManager) {
        this.colorManager = colorManager;
    }

    public abstract void onMousePressed(double x, double y, GraphicsContext gc);
    public abstract void onMouseDragged(double x, double y, GraphicsContext gc);
    public abstract void onMouseReleased(double x, double y, GraphicsContext gc);

    public abstract String getName();
    public abstract Image getIcon();
}
