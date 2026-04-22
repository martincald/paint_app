package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public class ToolManager {

    private static final double DEFAULT_BRUSH_SIZE = 5.0;

    private final CanvasManager canvasManager;
    private final ColorManager colorManager;
    private final List<Tool> tools;

    private Tool activeTool;
    private double brushSize;

    public ToolManager(CanvasManager canvasManager, ColorManager colorManager) {
        this.canvasManager = canvasManager;
        this.colorManager = colorManager;
        this.brushSize = DEFAULT_BRUSH_SIZE;

        tools = List.of(new BrushTool(), new EraserTool(), new FillTool());
        activeTool = tools.getFirst();

        attachCanvasListeners();
    }

    public void setActiveTool(String name) {
        for (Tool tool : tools) {
            if (tool.getName().equals(name)) {
                activeTool = tool;
                return;
            }
        }
    }

    public Tool getActiveTool() {
        return activeTool;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public double getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(double size) {
        this.brushSize = size;
    }

    private void attachCanvasListeners() {
        Canvas canvas = canvasManager.getCanvas();
        GraphicsContext gc = canvasManager.getGraphicsContext();

        canvas.setOnMousePressed(event -> {
            activeTool.configure(colorManager, brushSize);
            activeTool.onMousePressed(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseDragged(event -> {
            activeTool.configure(colorManager, brushSize);
            activeTool.onMouseDragged(event.getX(), event.getY(), gc);
        });

        canvas.setOnMouseReleased(event -> {
            activeTool.configure(colorManager, brushSize);
            activeTool.onMouseReleased(event.getX(), event.getY(), gc);
        });
    }
}