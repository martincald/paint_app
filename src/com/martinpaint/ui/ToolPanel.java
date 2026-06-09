package com.martinpaint.ui;

import com.martinpaint.color.ColorManager;
import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Narrow single-column tool rail on the left edge of the window. */
public class ToolPanel extends VBox {

    private static final double ICON_SIZE = 18;

    public ToolPanel(ToolManager toolManager, ColorManager colorManager) {
        getStyleClass().add("tool-rail");
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(6, 0, 6, 0));
        setSpacing(2);

        ToggleGroup group = new ToggleGroup();
        List<Tool> tools = toolManager.getTools();
        Map<Tool, ToggleButton> toolButtons = new HashMap<>();

        for (int i = 0; i < tools.size(); i++) {
            Tool tool = tools.get(i);
            if (tool.getSpec().separatorBefore()) getChildren().add(separator());

            ToggleButton btn = createToolButton(tool, toolManager, group);
            toolButtons.put(tool, btn);
            if (tool == toolManager.getActiveTool()) btn.setSelected(true);
            getChildren().add(btn);
        }

        // Spacer pushes the FG/BG chips to the bottom.
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // FG/BG color chips at the bottom of the rail.
        getChildren().add(buildRailColorChips(colorManager));

        // Keep button state in sync when the active tool changes from elsewhere.
        toolManager.activeToolProperty().addListener((_, _, newTool) -> {
            if (newTool == null) {
                group.selectToggle(null);
            } else {
                ToggleButton btn = toolButtons.get(newTool);
                if (btn != null) btn.setSelected(true);
            }
        });
    }

    private ToggleButton createToolButton(Tool tool, ToolManager mgr, ToggleGroup group) {
        ToggleButton btn = new ToggleButton();
        btn.setToggleGroup(group);
        btn.getStyleClass().add("tool-btn");

        Node svgIcon = ToolIcons.iconFor(tool.getSpec());
        if (svgIcon != null) {
            svgIcon.setOpacity(0.75);
            Node graphic = withKeyHint(svgIcon, tool);
            btn.setGraphic(graphic);
            btn.selectedProperty().addListener((_, _, sel) -> svgIcon.setOpacity(sel ? 1.0 : 0.75));
            btn.hoverProperty().addListener((_, _, hover) -> {
                if (!btn.isSelected()) svgIcon.setOpacity(hover ? 1.0 : 0.75);
            });
        } else {
            btn.setText(tool.getName().substring(0, 1).toUpperCase());
        }

        Tooltip tip = new Tooltip(tool.getName());
        btn.setTooltip(tip);

        btn.setOnAction(_ -> {
            if (mgr.getActiveTool() == tool) mgr.clearActiveTool();
            else mgr.setActiveTool(tool.getSpec());
        });

        return btn;
    }

    // Wraps an icon node in a StackPane with a small keyboard-shortcut hint label.
    private Node withKeyHint(Node iconNode, Tool tool) {
        String key = tool.getSpec().shortcut().getName();
        if (key == null) return iconNode;

        Label hint = new Label(key);
        hint.getStyleClass().add("tool-key-hint");
        StackPane.setAlignment(hint, Pos.BOTTOM_RIGHT);

        StackPane stack = new StackPane(iconNode, hint);
        stack.setAlignment(Pos.CENTER);
        stack.setPrefSize(ICON_SIZE + 4, ICON_SIZE + 4);
        return stack;
    }

    // FG/BG overlapping chips + swap/reset at the bottom of the rail.
    private Pane buildRailColorChips(ColorManager colorManager) {
        Region bgChip = Panels.colorChip("rail-color-chip", 22);
        bgChip.setLayoutX(14);
        bgChip.setLayoutY(14);

        Region fgChip = Panels.colorChip("rail-color-chip", 22);
        fgChip.setLayoutX(0);
        fgChip.setLayoutY(0);

        Runnable refresh = () -> {
            Color fg = colorManager.getCurrentColor();
            Color bg = colorManager.getBackgroundColor();
            if (fg != null) Panels.setColorFill(fgChip, fg);
            if (bg != null) Panels.setColorFill(bgChip, bg);
        };
        refresh.run();
        colorManager.currentColorProperty().addListener((_, _, _) -> refresh.run());
        colorManager.backgroundColorProperty().addListener((_, _, _) -> refresh.run());

        // Swap & reset labels
        Label swapLbl = Panels.actionLabel("⇄", "rail-chip-action");
        swapLbl.setLayoutX(28);
        swapLbl.setLayoutY(-2);
        swapLbl.setOnMouseClicked(_ -> colorManager.swapColors());

        Label resetLbl = Panels.actionLabel("↺", "rail-chip-action");
        resetLbl.setLayoutX(-2);
        resetLbl.setLayoutY(28);
        resetLbl.setOnMouseClicked(_ -> colorManager.resetColors());

        Pane chips = new Pane(bgChip, fgChip, swapLbl, resetLbl);
        chips.setPrefSize(36, 36);
        chips.setMaxSize(36, 36);
        VBox.setMargin(chips, new Insets(4, 0, 4, 0));
        return chips;
    }

    private Region separator() {
        Region sep = new Region();
        sep.getStyleClass().add("tool-separator");
        VBox.setMargin(sep, new Insets(4, 0, 4, 0));
        return sep;
    }
}
