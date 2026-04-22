package com.martinpaint.ui;

import com.martinpaint.color.ColorManager;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ColorWheelsPanel extends VBox {

    private final ColorManager colorManager;
    private final ColorPicker colorPicker;
    private final TextField hexField;

    public ColorWheelsPanel(ColorManager colorManager) {
        this.colorManager = colorManager;

        // Title
        Label title = new Label("Color");
        title.setStyle("-fx-text-fill: #CCCCCC; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Color picker
        colorPicker = new ColorPicker(colorManager.getCurrentColor());
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setOnAction(event -> applyColor(colorPicker.getValue()));

        // Hex
        Label hexLabel = new Label("Hex");
        hexLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");

        hexField = new TextField(toHexString(colorManager.getCurrentColor()));
        hexField.setPrefColumnCount(8);
        hexField.setPromptText("e.g. FF5733");
        hexField.setStyle(
                "-fx-background-color: #3C3F41; " +
                        "-fx-text-fill: #CCCCCC; " +
                        "-fx-border-color: #555555; " +
                        "-fx-border-radius: 3; " +
                        "-fx-background-radius: 3;"
        );

        hexField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitHexValue();
            }
        });

        hexField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitHexValue();
            }
        });

        VBox hexBox = new VBox(4, hexLabel, hexField);

        // Assemble
        setSpacing(10);
        setPadding(new Insets(4));
        getChildren().addAll(title, colorPicker, hexBox);
    }

    private void applyColor(Color color) {
        colorManager.setCurrentColor(color);
        colorPicker.setValue(color);
        hexField.setText(toHexString(color));
    }

    private void commitHexValue() {
        String text = hexField.getText().trim().replaceFirst("^#", "");
        if (text.matches("[0-9A-Fa-f]{6}")) {
            Color color = Color.web("#" + text);
            applyColor(color);
        }
    }

    private String toHexString(Color color) {
        return String.format("%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }
}