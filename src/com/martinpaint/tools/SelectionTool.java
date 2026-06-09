package com.martinpaint.tools;

import com.martinpaint.selection.SelectionController;
import javafx.scene.canvas.GraphicsContext;

/** Selection tool for rectangular areas. */
public class SelectionTool extends Tool {

    private SelectionController controller;

    public SelectionTool() {
        super(ToolSpec.SELECTION);
    }

    // Sets the shared SelectionController.
    public void setController(SelectionController controller) {
        this.controller = controller;
    }

    public SelectionController getController() {
        return controller;
    }

    @Override
    public void onDeactivated() {
        // Commit any in-progress selection so pixels are not lost when the user
        // switches to another tool.
        if (controller != null && controller.hasFloat()) {
            controller.commit();
        } else if (controller != null) {
            // Still defining marquee.
            controller.reset();
        }
    }

    // Mouse events delegated to SelectionController

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        if (controller != null) controller.onCanvasPressed(x, y);
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        if (controller != null) controller.onCanvasDragged(x, y);
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {
        if (controller != null) controller.onCanvasReleased(x, y);
    }
}
