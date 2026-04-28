package com.martinpaint.ui;

import com.martinpaint.color.ColorManager;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

//Dark panel shoing the color history
public class ColorHistoryPanel extends VBox {

    private static final int HISTORY_SIZE = 5;
    private static final double CIRCLE_RADIUS = 18.0;
    private static final Color STROKE_COLOR = Color.rgb(200, 200, 200, 0.6);

    private final ColorManager colorManager;
    private final Circle[] historyCircles = new Circle[HISTORY_SIZE];
    private final ColorPicker hiddenPicker = new ColorPicker();

    public ColorHistoryPanel(ColorManager colorManager) {
        this.colorManager = colorManager;

        setSpacing(8);
        setPadding(new Insets(8));
        setFillWidth(true);
        setStyle("-fx-background-color: transparent;");

        Label title = new Label("Color Selector");
        title.setStyle(
                "-fx-text-fill: #e0e0e0;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        // First 5 cells: history circles
        for (int i = 0; i < HISTORY_SIZE; i++) {
            Circle circle = createHistoryCircle(Color.WHITE);
            historyCircles[i] = circle;
            int col = i % 3;
            int row = i / 3;
            grid.add(wrapCell(circle), col, row);
        }

        // 6th cell: + button
        StackPane addButton = createAddButton();
        grid.add(addButton, 2, 1);

        // hidden color picker
        hiddenPicker.setVisible(false);
        hiddenPicker.setManaged(false);
        hiddenPicker.setOnAction(e -> {
            Color picked = hiddenPicker.getValue();
            if (picked != null) {
                colorManager.setCurrentColor(picked);
            }
        });

        getChildren().addAll(title, grid, hiddenPicker);

        refreshFromHistory();

        // bind to the observable list so history changes auto-refresh UI
        ObservableList<Color> colors = colorManager.getColorHistory().getColors();
        colors.addListener((ListChangeListener<Color>) c -> refreshFromHistory());
    }

    private Circle createHistoryCircle(Color fill) {
        Circle circle = new Circle(CIRCLE_RADIUS);
        circle.setFill(fill);
        circle.setStroke(STROKE_COLOR);
        circle.setStrokeWidth(1.0);
        circle.setCursor(Cursor.HAND);
        circle.setOnMouseClicked(e -> {
            Paint paint = circle.getFill();
            if (paint instanceof Color) {
                colorManager.setCurrentColor((Color) paint);
            }
        });
        return circle;
    }

    private StackPane wrapCell(Circle circle) {
        StackPane cell = new StackPane(circle);
        cell.setPrefSize(CIRCLE_RADIUS * 2 + 4, CIRCLE_RADIUS * 2 + 4);
        return cell;
    }

    private StackPane createAddButton() {
        Circle bg = new Circle(CIRCLE_RADIUS);
        bg.setFill(Color.WHITE);
        bg.setStroke(STROKE_COLOR);
        bg.setStrokeWidth(1.0);

        Label plus = new Label("+");
        plus.setStyle(
                "-fx-text-fill: black;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        StackPane pane = new StackPane(bg, plus);
        pane.setCursor(Cursor.HAND);
        pane.setPrefSize(CIRCLE_RADIUS * 2 + 4, CIRCLE_RADIUS * 2 + 4);
        pane.setOnMouseClicked(e -> hiddenPicker.show());
        return pane;
    }

    private void refreshFromHistory() {
        ObservableList<Color> colors = colorManager.getColorHistory().getColors();
        for (int i = 0; i < HISTORY_SIZE; i++) {
            Color c = i < colors.size() ? colors.get(i) : Color.WHITE;
            historyCircles[i].setFill(c);
        }
    }
}