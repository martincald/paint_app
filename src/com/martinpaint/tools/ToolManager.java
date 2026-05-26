package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorManager;
import com.martinpaint.app.HapticFeedback;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Tool manager.
public class ToolManager {

    private final CanvasManager canvasManager;
    private final ColorManager  colorManager;
    private final List<Tool>    tools;
    private final Map<String, Tool> toolsByName = new HashMap<>();
    private final SelectionTool selectionTool;

    private final ObjectProperty<Tool> activeTool = new SimpleObjectProperty<>();

    public ToolManager(CanvasManager canvasManager, ColorManager colorManager) {
        this.canvasManager = canvasManager;
        this.colorManager  = colorManager;

        selectionTool = new SelectionTool();
        tools = List.of(new BrushTool(), new PencilTool(), new EraserTool(), new FillTool(),
                        new EyeDropperTool(), selectionTool, new HandTool(), new ZoomTool());

        for (Tool tool : tools) {
            toolsByName.put(tool.getName(), tool);
            // Give every SizedTool a reference to the shared preview canvas.
            if (tool instanceof SizedTool st) {
                st.setPreviewCanvas(canvasManager.getPreviewCanvas());
            }
        }

        activeTool.set(tools.getFirst());
        activeTool.get().configure(colorManager);

        // Call lifecycle hooks when the active tool changes.
        activeTool.addListener((_, oldTool, newTool) -> {
            if (oldTool != null) oldTool.onDeactivated();
            if (newTool != null) {
                newTool.configure(colorManager);
                newTool.onActivated();
            }
        });

        attachCanvasListeners();
    }

    // Returns the SelectionTool instance so the viewport can wire the overlay into it.
    public SelectionTool getSelectionTool() {
        return selectionTool;
    }

    public void setActiveTool(String name) {
        Tool tool = toolsByName.get(name);
        if (tool != null && tool != activeTool.get()) {
            activeTool.set(tool);
            HapticFeedback.toolSwitch();
        }
    }

    // Deselects the current tool so no tool is active.
    public void clearActiveTool() {
        if (activeTool.get() != null) {
            activeTool.set(null);
        }
    }

    public Tool getActiveTool() {
        return activeTool.get();
    }

    public ObjectProperty<Tool> activeToolProperty() {
        return activeTool;
    }

    public List<Tool> getTools() {
        return tools;
    }

    private void attachCanvasListeners() {
        Canvas canvas = canvasManager.getCanvas();
        GraphicsContext gc = canvasManager.getGraphicsContext();

        canvas.setOnMousePressed(event -> {
            Tool tool = activeTool.get();
            if (tool == null) return; // No tool selected, do nothing.
            // These tools don't draw on the canvas; skip undo snapshot for them.
            if (!(tool instanceof EyeDropperTool) && !(tool instanceof SelectionTool)
                    && !(tool instanceof HandTool) && !(tool instanceof ZoomTool)) {
                canvasManager.saveStateForUndo();
            }
            tool.onMousePressed(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseDragged(event -> {
            Tool tool = activeTool.get();
            if (tool == null) return;
            tool.onMouseDragged(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseReleased(event -> {
            Tool tool = activeTool.get();
            if (tool == null) return;
            tool.onMouseReleased(event.getX(), event.getY(), gc);
        });
    }
}
