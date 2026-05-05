package com.martinpaint.ui;

import com.martinpaint.color.ColorHistory;
import com.martinpaint.color.ColorManager;
import com.martinpaint.color.ColorUtils;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

// Recently used color slots.
public class ColorHistoryPanel extends VBox {

    private static final int    HISTORY_SIZE = 5;
    private static final double SLOT_SIZE    = 70.0;

    private final ColorManager colorManager;
    private final Region[]     historyTiles = new Region[HISTORY_SIZE];
    private final ColorPicker  hiddenPicker = new ColorPicker();

    public ColorHistoryPanel(ColorManager colorManager) {
        this.colorManager = colorManager;

        setSpacing(12);
        setPadding(new Insets(4, 0, 4, 0));
        setFillWidth(true);
        setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Color Selector");
        title.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < HISTORY_SIZE; i++) {
            historyTiles[i] = createColorTile();
            grid.add(historyTiles[i], i % 3, i / 3);
        }
        grid.add(createAddTile(), 2, 1);

        hiddenPicker.setVisible(false);
        hiddenPicker.setManaged(false);
        hiddenPicker.setOnAction(_ -> {
            Color picked = hiddenPicker.getValue();
            if (picked != null) colorManager.setCurrentColor(picked);
        });

        getChildren().addAll(title, grid, hiddenPicker);

        refreshFromHistory();
        ObservableList<Color> colors = colorManager.getColorHistory().getColors();
        colors.addListener((ListChangeListener<Color>) _ -> refreshFromHistory());
    }

    private Region createColorTile() {
        Region tile = new Region();
        tile.getStyleClass().add("color-slot");
        tile.setPrefSize(SLOT_SIZE, SLOT_SIZE);
        tile.setMinSize(SLOT_SIZE, SLOT_SIZE);
        tile.setMaxSize(SLOT_SIZE, SLOT_SIZE);
        tile.setOnMouseClicked(_ -> {
            Color stored = (Color) tile.getUserData();
            if (stored != null) colorManager.setCurrentColor(stored);
        });
        applyFill(tile, null);
        return tile;
    }

    private StackPane createAddTile() {
        Region bg = new Region();
        bg.getStyleClass().add("color-slot-add");
        bg.setPrefSize(SLOT_SIZE, SLOT_SIZE);
        bg.setMinSize(SLOT_SIZE, SLOT_SIZE);
        bg.setMaxSize(SLOT_SIZE, SLOT_SIZE);
        bg.setStyle("-fx-background-color: #FFFFFF;");

        Label plus = new Label("+");
        plus.getStyleClass().add("color-add-plus");

        StackPane pane = new StackPane(bg, plus);
        pane.setCursor(Cursor.HAND);
        pane.setPrefSize(SLOT_SIZE, SLOT_SIZE);
        pane.setOnMouseClicked(_ -> hiddenPicker.show());
        return pane;
    }

    // null color = empty slot which cannot be clicked
    private void applyFill(Region tile, Color color) {
        tile.setUserData(color);
        if (color == null) {
            tile.setStyle("-fx-background-color: #2E2E2E;");
            tile.setCursor(Cursor.DEFAULT);
        } else {
            tile.setStyle("-fx-background-color: " + ColorUtils.toWebHex(color) + ";");
            tile.setCursor(Cursor.HAND);
        }
    }

    private void refreshFromHistory() {
        ColorHistory history = colorManager.getColorHistory();
        ObservableList<Color> colors = history.getColors();
        for (int i = 0; i < HISTORY_SIZE; i++) {
            Color c = i < colors.size() ? colors.get(i) : null;
            applyFill(historyTiles[i], c);
        }
    }
}
