package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorManager;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.canvas.GraphicsContext;

/** Base class for drawing tools. */
public abstract class Tool {

    private final ToolSpec spec;
    private final ReadOnlyIntegerWrapper settingsVersion = new ReadOnlyIntegerWrapper();

    protected CanvasManager canvasManager;
    protected ColorManager colorManager;

    protected Tool(ToolSpec spec) {
        this.spec = spec;
    }

    public void configure(CanvasManager canvasManager, ColorManager colorManager) {
        this.canvasManager = canvasManager;
        this.colorManager = colorManager;
    }

    public abstract void onMousePressed(double x, double y, GraphicsContext gc);
    public abstract void onMouseDragged(double x, double y, GraphicsContext gc);
    public abstract void onMouseReleased(double x, double y, GraphicsContext gc);

    // Called when the tool is activated.
    public void onActivated() {}

    // Called when the tool is deactivated.
    public void onDeactivated() {}

    public void resetSettings() {}

    public ToolSpec getSpec() {
        return spec;
    }

    public String getName() {
        return spec.displayName();
    }

    public ReadOnlyIntegerProperty settingsVersionProperty() {
        return settingsVersion.getReadOnlyProperty();
    }

    protected void markSettingsChanged() {
        settingsVersion.set(settingsVersion.get() + 1);
    }

    protected void saveUndoState() {
        if (canvasManager != null) {
            canvasManager.saveStateForUndo();
        }
    }

    protected void markDrawingChanged() {
        if (canvasManager != null) {
            canvasManager.markDrawingChanged();
        }
    }
}
