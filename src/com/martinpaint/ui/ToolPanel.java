package com.martinpaint.ui;

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

import java.util.ArrayList;
import java.util.List;

// 3x2 grid of tool selection buttons
public class ToolPanel extends VBox {

    private static final double ICON_SIZE = 36;
    private static final double CELL_SIZE = 72;
    private static final int    COLUMNS = 3;
    private static final int    ROWS    = 2;
    private static final int    TOTAL   = COLUMNS * ROWS;

    public ToolPanel(ToolManager toolManager) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        ToggleGroup group = new ToggleGroup();
        List<Tool> tools = toolManager.getTools();

        List<ToggleButton> realButtons = new ArrayList<>();
        List<Tool>         buttonTools = new ArrayList<>();

        for (int i = 0; i < TOTAL; i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;

            ToggleButton btn;
            if (i < tools.size()) {
                Tool tool = tools.get(i);
                btn = createToolButton(tool, toolManager, group);
                realButtons.add(btn);
                buttonTools.add(tool);
                if (tool == toolManager.getActiveTool()) {
                    btn.setSelected(true);
                }
            } else {
                btn = createPlaceholder();
            }
            grid.add(btn, col, row);
        }

        toolManager.activeToolProperty().addListener((_, _, newT) -> {
            for (int i = 0; i < realButtons.size(); i++) {
                realButtons.get(i).setSelected(buttonTools.get(i) == newT);
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
            ImageView v = new ImageView(icon);
            v.setFitWidth(ICON_SIZE);
            v.setFitHeight(ICON_SIZE);
            v.setPreserveRatio(true);
            btn.setGraphic(v);
        } else {
            btn.setText(tool.getName());
        }

        btn.setTooltip(new Tooltip(tool.getName()));
        btn.setOnAction(_ -> mgr.setActiveTool(tool.getName()));
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