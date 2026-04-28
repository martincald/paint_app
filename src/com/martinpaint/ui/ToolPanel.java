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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ToolPanel extends VBox {

    private static final double ICON_SIZE = 28;
    private static final double CELL_SIZE = 71;
    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ROWS = 2;
    private static final int TOTAL_SLOTS = GRID_COLUMNS * GRID_ROWS; // 6

    public ToolPanel(ToolManager toolManager) {
        Label title = new Label("Tools");
        title.setStyle("-fx-text-fill: #CCCCCC; -fx-font-weight: bold; -fx-font-size: 13px;");

        GridPane toolGrid = new GridPane();
        toolGrid.setHgap(8);
        toolGrid.setVgap(8);
        toolGrid.setPadding(new Insets(6, 0, 0, 0));

        ToggleGroup toggleGroup = new ToggleGroup();
        List<Tool> tools = toolManager.getTools();

        // track which button corresponds to which tool so we can sync with activeToolProperty.
        List<ToggleButton> toolButtons = new ArrayList<>();
        List<Tool> buttonTools = new ArrayList<>();

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            int col = i % GRID_COLUMNS;
            int row = i / GRID_COLUMNS;

            ToggleButton button;
            if (i < tools.size()) {
                Tool tool = tools.get(i);
                button = createToolButton(tool, toolManager, toggleGroup);
                toolButtons.add(button);
                buttonTools.add(tool);

                //select current active tool
                if (tool == toolManager.getActiveTool()) {
                    button.setSelected(true);
                }
            } else {
                button = createPlaceholderButton();
            }

            toolGrid.add(button, col, row);
        }

        // keep the active tool button selected
        toolManager.activeToolProperty().addListener((obs, oldTool, newTool) -> {
            for (int i = 0; i < toolButtons.size(); i++) {
                toolButtons.get(i).setSelected(buttonTools.get(i) == newTool);
            }
        });

        setSpacing(6);
        setPadding(new Insets(4));
        getChildren().addAll(title, toolGrid);
    }

    private ToggleButton createToolButton(Tool tool, ToolManager toolManager, ToggleGroup group) {
        ToggleButton button = new ToggleButton();
        button.setToggleGroup(group);
        applySquareSize(button);

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

    private ToggleButton createPlaceholderButton() {
        ToggleButton button = new ToggleButton();
        applySquareSize(button);
        button.setDisable(true);
        button.setTooltip(new Tooltip("Coming soon"));
        button.setStyle(
                "-fx-background-color: #3C3F41; " +
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 6; " +
                        "-fx-opacity: 0.3;"
        );
        return button;
    }

    private void applySquareSize(ToggleButton button) {
        button.setPrefSize(CELL_SIZE, CELL_SIZE);
        button.setMinSize(CELL_SIZE, CELL_SIZE);
        button.setMaxSize(CELL_SIZE, CELL_SIZE);
    }
}