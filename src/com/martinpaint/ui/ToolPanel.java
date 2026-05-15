package com.martinpaint.ui;

import com.martinpaint.tools.SelectionTool;
import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Grid of tool buttons.
public class ToolPanel extends VBox {

    private static final double ICON_SIZE            = 36;
    private static final double SELECTION_ICON_SIZE  = 46; // Selection icon is larger for clarity.
    private static final double CELL_SIZE = 72;
    private static final int    COLUMNS = 3;
    private static final int    ROWS    = 2;
    private static final int    TOTAL   = COLUMNS * ROWS;

    public ToolPanel(ToolManager toolManager) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // ToggleGroup with no default selected — allows full deselection.
        ToggleGroup group = new ToggleGroup();
        List<Tool> tools = toolManager.getTools();
        Map<Tool, ToggleButton> toolButtons = new HashMap<>();

        for (int i = 0; i < TOTAL; i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;

            ToggleButton btn;
            if (i < tools.size()) {
                Tool tool = tools.get(i);
                btn = createToolButton(tool, toolManager, group);
                toolButtons.put(tool, btn);
                if (tool == toolManager.getActiveTool()) {
                    btn.setSelected(true);
                }
            } else {
                btn = createPlaceholder();
            }
            grid.add(btn, col, row);
        }

        // Keep button state in sync when the active tool changes from outside.
        toolManager.activeToolProperty().addListener((_, _, newTool) -> {
            if (newTool == null) {
                // No tool selected — deselect all buttons.
                group.selectToggle(null);
            } else {
                ToggleButton btn = toolButtons.get(newTool);
                if (btn != null) btn.setSelected(true);
            }
        });

        setSpacing(0);
        setPadding(new Insets(4, 0, 4, 0));
        setAlignment(Pos.CENTER);
        getChildren().add(grid);
    }

    private ToggleButton createToolButton(Tool tool, ToolManager mgr, ToggleGroup group) {
        ToggleButton btn = new ToggleButton();
        btn.setToggleGroup(group);
        btn.getStyleClass().add("tool-slot");
        applySquareSize(btn);

        Image icon = tool.getIcon();
        if (icon != null) {
            double iconSize = (tool instanceof SelectionTool) ? SELECTION_ICON_SIZE : ICON_SIZE;
            ImageView v = new ImageView(icon);
            v.setFitWidth(iconSize);
            v.setFitHeight(iconSize);
            v.setPreserveRatio(true);
            btn.setGraphic(v);
        } else {
            btn.setText(tool.getName());
        }

        btn.setTooltip(new Tooltip(tool.getName()));

        btn.setOnAction(_ -> {
            // If this tool is already active, clicking again deselects it.
            if (mgr.getActiveTool() == tool) {
                mgr.clearActiveTool();
            } else {
                mgr.setActiveTool(tool.getName());
            }
        });

        return btn;
    }

    private ToggleButton createPlaceholder() {
        ToggleButton btn = new ToggleButton();
        btn.getStyleClass().add("tool-slot-empty");
        applySquareSize(btn);
        btn.setDisable(true);
        btn.setTooltip(new Tooltip("Coming soon"));
        return btn;
    }

    private void applySquareSize(ToggleButton btn) {
        btn.setPrefSize(CELL_SIZE, CELL_SIZE);
        btn.setMinSize(CELL_SIZE, CELL_SIZE);
        btn.setMaxSize(CELL_SIZE, CELL_SIZE);
    }
}
