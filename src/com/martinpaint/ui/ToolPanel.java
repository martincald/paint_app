package com.martinpaint.ui;

import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class ToolPanel extends VBox {

    private static final double ICON_SIZE = 28;

    public ToolPanel(ToolManager toolManager) {
        Label title = new Label("Tools");
        title.setStyle("-fx-text-fill: #CCCCCC; -fx-font-weight: bold; -fx-font-size: 13px;");

        FlowPane toolButtons = new FlowPane(8, 8);
        toolButtons.setPadding(new Insets(6, 0, 0, 0));

        ToggleGroup toggleGroup = new ToggleGroup();

        for (Tool tool : toolManager.getTools()) {
            ToggleButton button = createToolButton(tool, toolManager, toggleGroup);
            toolButtons.getChildren().add(button);

            //select curent active tool
            if (tool == toolManager.getActiveTool()) {
                button.setSelected(true);
            }
        }

        setSpacing(6);
        setPadding(new Insets(4));
        getChildren().addAll(title, toolButtons);
    }

    private ToggleButton createToolButton(Tool tool, ToolManager toolManager, ToggleGroup group) {
        ToggleButton button = new ToggleButton();
        button.setToggleGroup(group);

        //Set icon for the tool
        Image icon = tool.getIcon();
        if (icon != null) {
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(ICON_SIZE);
            iconView.setFitHeight(ICON_SIZE);
            iconView.setPreserveRatio(true);
            button.setGraphic(iconView);
        } else {
            button.setText(tool.getName());
        }

        //Show tool name on hover
        button.setTooltip(new Tooltip(tool.getName()));

        // Styling
        button.setStyle(
                "-fx-background-color: #3C3F41; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 6;"
        );

        //Highlight when selected
        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                button.setStyle(
                        "-fx-background-color: #5294E2; " +
                                "-fx-border-color: #5294E2; " +
                                "-fx-padding: 6;"
                );
            } else {
                button.setStyle(
                        "-fx-background-color: #3C3F41; " +
                                "-fx-border-color: transparent; " +
                                "-fx-padding: 6;"
                );
            }
        });

        //activate tool when clicked
        button.setOnAction(event -> toolManager.setActiveTool(tool.getName()));

        return button;
    }
}