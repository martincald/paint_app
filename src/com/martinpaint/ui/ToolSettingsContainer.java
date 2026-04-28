package com.martinpaint.ui;

import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ToolSettingsContainer extends VBox {

    private final Pane contentArea;

    public ToolSettingsContainer(ToolManager toolManager) {
        Label title = new Label("Tool settings");
        title.setStyle("-fx-text-fill: #CCCCCC; -fx-font-weight: bold; -fx-font-size: 13px;");

        contentArea = new Pane();

        setSpacing(8);
        setPadding(new Insets(4));
        getChildren().addAll(title, contentArea);

        //Populate with initial active tool's settings panel
        Tool initialTool = toolManager.getActiveTool();
        if (initialTool != null && initialTool.getSettingsPanel() != null) {
            contentArea.getChildren().add(initialTool.getSettingsPanel());
        }

        //React to active tool changes
        toolManager.activeToolProperty().addListener((observable, oldTool, newTool) -> {
            contentArea.getChildren().clear();
            if (newTool != null && newTool.getSettingsPanel() != null) {
                contentArea.getChildren().add(newTool.getSettingsPanel());
            }
        });
    }
}
