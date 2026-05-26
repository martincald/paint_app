package com.martinpaint.tools;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Pan tool — canvas interaction is handled at the viewport level; canvas events are no-ops.
public class HandTool extends Tool {

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {}

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {}

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {}

    @Override
    public String getName() { return "Hand"; }

    @Override
    public Image getIcon() { return null; }
}
