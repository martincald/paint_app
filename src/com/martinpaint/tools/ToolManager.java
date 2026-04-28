package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public class ToolManager {

    private final CanvasManager canvasManager;
    private final ColorManager colorManager;
    private final List<Tool> tools;

    private final ObjectProperty<Tool> activeTool = new SimpleObjectProperty<>();

    public ToolManager(CanvasManager canvasManager, ColorManager colorManager) {
        this.canvasManager = canvasManager;
        this.colorManager = colorManager;

        tools = List.of(new BrushTool(), new EraserTool(), new FillTool());
        activeTool.set(tools.getFirst());

        attachCanvasListeners();
    }

    public void setActiveTool(String name) {
        for (Tool tool : tools) {
            if (tool.getName().equals(name)) {
                activeTool.set(tool);
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
            activeTool.get().configure(colorManager);
            activeTool.get().onMousePressed(event.getX(), event.getY(), gc);
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