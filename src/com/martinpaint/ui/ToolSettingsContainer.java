package com.martinpaint.ui;

import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Shows the active tool's settings below a panel header. */
public class ToolSettingsContainer extends VBox {

    private static final String PLACEHOLDER_TEXT = "No options for this tool.";

    private final StackPane contentArea;
    private final Label     placeholder;
    private final Button    resetBtn;

    public ToolSettingsContainer(ToolManager toolManager) {
        getStyleClass().add("panel-box");

        // ── Panel header ──────────────────────────────────────────
        var header = Panels.panelHeader("PROPERTIES");
        resetBtn = Panels.headerBtn("↺");
        resetBtn.setTooltip(new javafx.scene.control.Tooltip("Reset defaults"));
        resetBtn.setOnAction(_ -> {
            Tool tool = toolManager.getActiveTool();
            if (tool == null) return;
            tool.resetSettings();
            showSettingsFor(tool);
        });
        Panels.addHeaderAction(header, resetBtn);

        // ── Content area ──────────────────────────────────────────
        placeholder = new Label(PLACEHOLDER_TEXT);
        placeholder.getStyleClass().add("placeholder-text");
        placeholder.setWrapText(true);
        placeholder.setMaxWidth(220);
        placeholder.setAlignment(Pos.CENTER);

        contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_LEFT);
        contentArea.setPadding(new Insets(10));

        getChildren().addAll(header, contentArea);

        showSettingsFor(toolManager.getActiveTool());
        toolManager.activeToolProperty()
                .addListener((_, _, newTool) -> showSettingsFor(newTool));
    }

    private void showSettingsFor(Tool tool) {
        contentArea.getChildren().clear();
        Node panel = tool != null ? ToolSettingsView.create(tool) : null;
        resetBtn.setDisable(panel == null);
        contentArea.getChildren().add(panel != null ? panel : placeholder);
    }
}
