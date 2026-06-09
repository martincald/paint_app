package com.martinpaint.tools;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Hard-edged pencil tool: draws directly without the opacity-preview buffer. */
public class PencilTool extends SizedTool {

    public PencilTool() { super(ToolSpec.PENCIL); }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        saveUndoState();
        super.onMousePressed(x, y, gc);
        // Stamp a dot on click so a single press always marks.
        gc.save();
        gc.setFill(strokeColor());
        double r = getSize() / 2.0;
        gc.fillOval(x - r, y - r, getSize(), getSize());
        gc.restore();
        markDrawingChanged();
    }

    @Override
    protected Color strokeColor() {
        return colorManager != null ? colorManager.getCurrentColor() : Color.BLACK;
    }
}
