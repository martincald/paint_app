package com.martinpaint.ui;

import com.martinpaint.color.ColorManager;
import com.martinpaint.color.ColorUtils;
import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Narrow single-column tool rail on the left edge of the window.
public class ToolPanel extends VBox {

    private static final double ICON_SIZE = 18;

    // Keyboard shortcut for each tool (by getName())
    private static final Map<String, String> TOOL_KEYS = Map.of(
        "Brush",       "B",
        "Pencil",      "N",
        "Eraser",      "E",
        "Bucket Fill", "G",
        "Eyedropper",  "I",
        "Selection",   "M",
        "Hand",        "H",
        "Zoom",        "Z"
    );

    // Separator before these tool indices (0-based)
    // Tool order: Brush(0) Pencil(1) Eraser(2) Fill(3) Eyedropper(4) Selection(5) Hand(6) Zoom(7)
    private static final java.util.Set<Integer> SEP_BEFORE = java.util.Set.of(2, 3, 5, 6);

    public ToolPanel(ToolManager toolManager, ColorManager colorManager) {
        getStyleClass().add("tool-rail");
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(6, 0, 6, 0));
        setSpacing(2);

        ToggleGroup group = new ToggleGroup();
        List<Tool> tools = toolManager.getTools();
        Map<Tool, ToggleButton> toolButtons = new HashMap<>();

        for (int i = 0; i < tools.size(); i++) {
            if (SEP_BEFORE.contains(i)) getChildren().add(separator());

            Tool tool = tools.get(i);
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

        // Prefer vector SVG icon, fall back to raster PNG, then first letter.
        Node svgIcon = ToolIcons.iconFor(tool.getName());
        if (svgIcon != null) {
            svgIcon.setOpacity(0.75);
            Node graphic = withKeyHint(svgIcon, tool.getName());
            btn.setGraphic(graphic);
            btn.selectedProperty().addListener((_, _, sel) -> svgIcon.setOpacity(sel ? 1.0 : 0.75));
            btn.hoverProperty().addListener((_, _, hover) -> {
                if (!btn.isSelected()) svgIcon.setOpacity(hover ? 1.0 : 0.75);
            });
        } else {
            Image icon = tool.getIcon();
            if (icon != null) {
                ImageView iv = new ImageView(icon);
                iv.setFitWidth(ICON_SIZE);
                iv.setFitHeight(ICON_SIZE);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                iv.setOpacity(0.75);
                Node graphic = withKeyHint(iv, tool.getName());
                btn.setGraphic(graphic);
                btn.selectedProperty().addListener((_, _, sel) -> iv.setOpacity(sel ? 1.0 : 0.75));
                btn.hoverProperty().addListener((_, _, hover) -> {
                    if (!btn.isSelected()) iv.setOpacity(hover ? 1.0 : 0.75);
                });
            } else {
                btn.setText(tool.getName().substring(0, 1).toUpperCase());
            }
        }

        Tooltip tip = new Tooltip(tool.getName());
        tip.setStyle("-fx-background-color: #000; -fx-text-fill: #e6e6e6; -fx-font-size: 11px;");
        btn.setTooltip(tip);

        btn.setOnAction(_ -> {
            if (mgr.getActiveTool() == tool) mgr.clearActiveTool();
            else mgr.setActiveTool(tool.getName());
        });

        return btn;
    }

    // Wraps an icon node in a StackPane with a small keyboard-shortcut hint label.
    private Node withKeyHint(Node iconNode, String toolName) {
        String key = TOOL_KEYS.get(toolName);
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
        Region bgChip = new Region();
        bgChip.getStyleClass().add("rail-color-chip");
        bgChip.setPrefSize(22, 22);
        bgChip.setMinSize(22, 22);
        bgChip.setMaxSize(22, 22);
        bgChip.setLayoutX(14);
        bgChip.setLayoutY(14);

        Region fgChip = new Region();
        fgChip.getStyleClass().add("rail-color-chip");
        fgChip.setPrefSize(22, 22);
        fgChip.setMinSize(22, 22);
        fgChip.setMaxSize(22, 22);
        fgChip.setLayoutX(0);
        fgChip.setLayoutY(0);

        Runnable refresh = () -> {
            Color fg = colorManager.getCurrentColor();
            Color bg = colorManager.getBackgroundColor();
            if (fg != null) fgChip.setStyle("-fx-background-color: " + ColorUtils.toWebHex(fg) +
                "; -fx-background-radius: 2; -fx-border-color: #555; -fx-border-width: 0.5; -fx-border-radius: 2;");
            if (bg != null) bgChip.setStyle("-fx-background-color: " + ColorUtils.toWebHex(bg) +
                "; -fx-background-radius: 2; -fx-border-color: #555; -fx-border-width: 0.5; -fx-border-radius: 2;");
        };
        refresh.run();
        colorManager.currentColorProperty().addListener((_, _, _) -> refresh.run());
        colorManager.backgroundColorProperty().addListener((_, _, _) -> refresh.run());

        // Swap & reset labels
        Label swapLbl = new Label("⇄");
        swapLbl.setStyle("-fx-text-fill: #8a8a8a; -fx-font-size: 9px; -fx-cursor: hand;");
        swapLbl.setLayoutX(28);
        swapLbl.setLayoutY(-2);
        swapLbl.setOnMouseClicked(_ -> colorManager.swapColors());

        Label resetLbl = new Label("↺");
        resetLbl.setStyle("-fx-text-fill: #8a8a8a; -fx-font-size: 9px; -fx-cursor: hand;");
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
