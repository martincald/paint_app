package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorManager;
import com.martinpaint.app.HapticFeedback;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

// Tool manager.
public class ToolManager {

    private final CanvasManager canvasManager;
    private final ColorManager colorManager;
    private final List<Tool> tools;

    private final ObjectProperty<Tool> activeTool = new SimpleObjectProperty<>();

    public ToolManager(CanvasManager canvasManager, ColorManager colorManager) {
        this.canvasManager = canvasManager;
        this.colorManager = colorManager;

        tools = List.of(new BrushTool(), new EraserTool(), new FillTool(), new EyeDropperTool());
        activeTool.set(tools.getFirst());

        attachCanvasListeners();
    }

    public void setActiveTool(String name) {
        for (Tool tool : tools) {
            if (tool.getName().equals(name)) {
                if (tool != activeTool.get()) {
                    activeTool.set(tool);
                    HapticFeedback.toolSwitch();
                }
                return;
            }
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
            tool.configure(colorManager);
            // Eye-dropper does not draw, so no undo state is needed.
            if (!(tool instanceof EyeDropperTool)) {
                canvasManager.saveStateForUndo();
            }
            tool.onMousePressed(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseDragged(event -> {
            activeTool.get().configure(colorManager);
            activeTool.get().onMouseDragged(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseReleased(event -> {
            activeTool.get().configure(colorManager);
            activeTool.get().onMouseReleased(event.getX(), event.getY(), gc);
        });
    }
}
