package com.martinpaint.ui;

import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class BrushSettingsPanel extends VBox {

    public BrushSettingsPanel(ToolManager toolManager) {
        Label title = new Label("Brush Settings");
        title.setStyle("-fx-text-fill: #CCCCCC; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label sizeLabel = new Label("Size: 5px");
        sizeLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");

        Slider sizeSlider = new Slider(1, 50, toolManager.getBrushSize());
        sizeSlider.setBlockIncrement(1);
        sizeSlider.setStyle(
                "-fx-control-inner-background: #3C3F41; " +
                        "-fx-accent: #5294E2;"
        );

        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double rounded = Math.round(newVal.doubleValue());
            toolManager.setBrushSize(rounded);
            sizeLabel.setText("Size: " + (int) rounded + "px");
        });

        setSpacing(8);
        setPadding(new Insets(4));
        getChildren().addAll(title, sizeLabel, sizeSlider);
    }
}