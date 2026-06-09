package com.martinpaint.ui;

import com.martinpaint.tools.FillTool;
import com.martinpaint.tools.OpacityAware;
import com.martinpaint.tools.SizedTool;
import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

// Contextual options bar below the system menu bar.
// Shows the active tool name and its key settings at a glance.
public class OptionsBar extends HBox {

    private final ToolManager toolManager;
    private final InvalidationListener settingsListener;
    private Tool observedTool;

    public OptionsBar(ToolManager toolManager) {
        this.toolManager = toolManager;
        this.settingsListener = _ -> rebuild(this.toolManager.getActiveTool());
        getStyleClass().add("options-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(0);
        setPadding(new Insets(0, 12, 0, 12));

        observeTool(toolManager.getActiveTool());
        toolManager.activeToolProperty().addListener((_, _, tool) -> observeTool(tool));
    }

    private void observeTool(Tool tool) {
        if (observedTool != null) {
            observedTool.settingsVersionProperty().removeListener(settingsListener);
        }
        observedTool = tool;
        if (observedTool != null) {
            observedTool.settingsVersionProperty().addListener(settingsListener);
        }
        rebuild(tool);
    }

    private void rebuild(Tool tool) {
        getChildren().clear();
        if (tool == null) return;

        // Tool name pill
        Label name = new Label(tool.getName());
        name.getStyleClass().add("options-tool-name");
        getChildren().add(name);

        // Size
        if (tool instanceof SizedTool sized) {
            getChildren().add(divider());
            getChildren().add(optGroup("Size",
                pill(String.format("%.0fpx", sized.getSize()))));
        }

        // Opacity
        if (tool instanceof OpacityAware op) {
            getChildren().add(divider());
            getChildren().add(optGroup("Opacity",
                pill(String.format("%.0f%%", op.getOpacity() * 100))));
        }

        // Fill tolerance
        if (tool instanceof FillTool fill) {
            getChildren().add(divider());
            getChildren().add(optGroup("Tolerance",
                pill(String.valueOf(fill.getTolerance()))));
        }
    }

    private HBox optGroup(String labelText, Label value) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("options-label");
        HBox g = new HBox(6, lbl, value);
        g.setAlignment(Pos.CENTER_LEFT);
        g.setPadding(new Insets(0, 12, 0, 12));
        return g;
    }

    private Label pill(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("options-pill");
        return l;
    }

    private Region divider() {
        Region r = new Region();
        r.getStyleClass().add("options-divider");
        return r;
    }
}
