package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorManager;
import com.martinpaint.app.HapticFeedback;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Owns all tool instances, wires canvas input listeners, and tracks the active tool. */
public class ToolManager {

    private final CanvasManager canvasManager;
    private final ColorManager  colorManager;
    private final List<Tool>    tools;
    private final Map<ToolSpec, Tool> toolsBySpec = new EnumMap<>(ToolSpec.class);
    private final SelectionTool selectionTool;

    private final ObjectProperty<Tool> activeTool = new SimpleObjectProperty<>();
    private Tool gestureTool;
    private GraphicsContext gestureGc;

    public ToolManager(CanvasManager canvasManager, ColorManager colorManager) {
        this.canvasManager = canvasManager;
        this.colorManager  = colorManager;

        List<Tool> createdTools = new ArrayList<>();

        for (ToolSpec spec : ToolSpec.toolbarOrder()) {
            Tool tool = spec.createTool();
            createdTools.add(tool);
            toolsBySpec.put(spec, tool);
        }

        tools = List.copyOf(createdTools);
        selectionTool = (SelectionTool) toolsBySpec.get(ToolSpec.SELECTION);

        for (Tool tool : tools) {
            tool.configure(canvasManager, colorManager);
            // Give every SizedTool a reference to the shared preview canvas.
            if (tool instanceof SizedTool st) {
                st.setPreviewCanvas(canvasManager.getPreviewCanvas());
            }
        }

        activeTool.set(tools.getFirst());

        // Call lifecycle hooks when the active tool changes.
        activeTool.addListener((_, oldTool, newTool) -> {
            if (oldTool != null) oldTool.onDeactivated();
            if (newTool != null) {
                newTool.onActivated();
            }
        });

        attachCanvasListeners();
    }

    // Returns the SelectionTool instance so the viewport can wire the overlay into it.
    public SelectionTool getSelectionTool() {
        return selectionTool;
    }

    public void setActiveTool(ToolSpec spec) {
        Tool tool = toolsBySpec.get(spec);
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

        canvas.setOnMousePressed(event -> {
            gestureTool = activeTool.get();
            if (gestureTool == null) {
                gestureGc = null;
                return;
            }
            gestureGc = canvasManager.getGraphicsContext();
            gestureTool.onMousePressed(event.getX(), event.getY(), gestureGc);
        });

        canvas.setOnMouseDragged(event -> {
            if (gestureTool != null) {
                gestureTool.onMouseDragged(event.getX(), event.getY(), gestureGc);
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (gestureTool != null) {
                gestureTool.onMouseReleased(event.getX(), event.getY(), gestureGc);
                gestureTool = null;
                gestureGc = null;
            }
        });
    }
}
