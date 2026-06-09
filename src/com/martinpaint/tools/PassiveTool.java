package com.martinpaint.tools;

import javafx.scene.canvas.GraphicsContext;

/** No-op tool used for Hand and Zoom, which are handled entirely by CanvasViewport. */
public class PassiveTool extends Tool {

    public PassiveTool(ToolSpec spec) {
        super(spec);
    }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {}

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {}

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {}
}
