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
        tools = List.of(new BrushTool(), new EraserTool(), new FillTool(),
                        new EyeDropperTool(), selectionTool);

        for (Tool tool : tools) {
            toolsByName.put(tool.getName(), tool);
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
            // SelectionTool and EyeDropperTool manage their own undo snapshots.
            if (!(tool instanceof EyeDropperTool) && !(tool instanceof SelectionTool)) {
                canvasManager.saveStateForUndo();
            }
            tool.onMousePressed(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseDragged(event -> {
            activeTool.get().onMouseDragged(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseReleased(event -> {
            activeTool.get().onMouseReleased(event.getX(), event.getY(), gc);
        });
    }
}
