package com.martinpaint.ui;

import com.martinpaint.color.ColorManager;
import com.martinpaint.color.ColorUtils;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

// Swatch panel: 32 preset color tiles + recently used colors.
public class ColorHistoryPanel extends VBox {

    private static final double SWATCH_SIZE = 22;

    private static final String[] PRESET_SWATCHES = {
        "#000000", "#3F3F3F", "#7F7F7F", "#BFBFBF", "#FFFFFF", "#FF0F0F", "#FF6A00", "#FFD600",
        "#7BD63B", "#1BB766", "#19B5A4", "#1492E6", "#2F4FE0", "#7445E8", "#B33ACF", "#E73C9A",
        "#5E3A1F", "#8B5A2B", "#C28856", "#E6B780", "#F5DCB7", "#A52A2A", "#D02F2F", "#E25757",
        "#1B3A3A", "#1E5957", "#2F8076", "#5BAA9F", "#A8D5CC", "#0B1F4D", "#15326E", "#2C5BB8",
    };

    private final ColorManager colorManager;

    public ColorHistoryPanel(ColorManager colorManager) {
        this.colorManager = colorManager;
        getStyleClass().add("panel-box");

        HBox header = panelHeader("SWATCHES");

        FlowPane swatchGrid = new FlowPane(3, 3);
        swatchGrid.setPrefWrapLength(8 * (SWATCH_SIZE + 3));
        swatchGrid.setPadding(new Insets(0, 0, 2, 0));
        for (String hex : PRESET_SWATCHES) {
            swatchGrid.getChildren().add(createSwatch(hex));
        }

        FlowPane recentRow = new FlowPane(3, 3);
        recentRow.setPadding(new Insets(6, 0, 0, 0));
        refreshRecent(recentRow);
        colorManager.getColorHistory().getColors()
            .addListener((javafx.collections.ListChangeListener<Color>) _ -> refreshRecent(recentRow));

        VBox body = new VBox(swatchGrid);
        body.setPadding(new Insets(8, 10, 8, 10));

        getChildren().addAll(header, body);

        colorManager.getColorHistory().getColors().addListener(
            (javafx.collections.ListChangeListener<Color>) _ -> {
                if (!body.getChildren().contains(recentRow) && !recentRow.getChildren().isEmpty()) {
                    body.getChildren().add(recentRow);
                }
            }
        );
    }

    private Region createSwatch(String hex) {
        Region sw = new Region();
        sw.getStyleClass().add("color-slot");
        sw.setPrefSize(SWATCH_SIZE, SWATCH_SIZE);
        sw.setMinSize(SWATCH_SIZE, SWATCH_SIZE);
        sw.setMaxSize(SWATCH_SIZE, SWATCH_SIZE);
        sw.setStyle("-fx-background-color: " + hex + ";");
        Tooltip.install(sw, new Tooltip(hex));
        sw.setCursor(Cursor.HAND);
        sw.setOnMouseClicked(_ -> colorManager.setCurrentColor(Color.web(hex)));
        return sw;
    }

    private void refreshRecent(FlowPane row) {
        row.getChildren().clear();
        ObservableList<Color> colors = colorManager.getColorHistory().getColors();
        for (Color c : colors) {
            String hex = ColorUtils.toWebHex(c);
            row.getChildren().add(createSwatch(hex));
        }
    }

    // ── Reusable header helpers ───────────────────────────────────
    static HBox panelHeader(String title) {
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(title);
        lbl.getStyleClass().add("panel-title");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        HBox actions = new HBox(2);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(lbl, actions);
        header.getStyleClass().add("panel-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    static Button headerBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("panel-action-btn");
        return btn;
    }
}
