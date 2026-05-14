package com.martinpaint.ui;

import com.martinpaint.app.HapticFeedback;
import com.martinpaint.tools.FillTool;
import com.martinpaint.tools.SizedTool;
import com.martinpaint.tools.Tool;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

// Builds the JavaFX settings panel for a given Tool.
final class ToolSettingsView {

    private ToolSettingsView() {}

    static Node create(Tool tool) {
        if (tool instanceof SizedTool sized) return sizeSlider(sized);
        if (tool instanceof FillTool fill)   return toleranceSlider(fill);
        return null;
    }

    private static VBox sizeSlider(SizedTool tool) {
        Label label = new Label("Size: " + (int) tool.getSize() + "px");
        label.getStyleClass().add("tool-slider-label");

        Slider slider = new Slider(1, 50, tool.getSize());
        slider.getStyleClass().add("tool-slider");
        slider.valueProperty().addListener((_, oldVal, newVal) -> {
            tool.setSize(newVal.doubleValue());
            label.setText("Size: " + (int) tool.getSize() + "px");
            if ((int) oldVal.doubleValue() != (int) newVal.doubleValue()) {
                HapticFeedback.sliderTick();
            }
        });
        return panel(label, slider);
    }

    private static VBox toleranceSlider(FillTool tool) {
        Label label = new Label("Tolerance: " + tool.getTolerance());
        label.getStyleClass().add("tool-slider-label");

        Slider slider = new Slider(0, 100, tool.getTolerance());
        slider.getStyleClass().add("tool-slider");
        slider.valueProperty().addListener((_, _, newVal) -> {
            int value = newVal.intValue();
            if (value != tool.getTolerance()) HapticFeedback.sliderTick();
            tool.setTolerance(value);
            label.setText("Tolerance: " + value);
        });
        return panel(label, slider);
    }

    private static VBox panel(Node... children) {
        VBox box = new VBox(8, children);
        box.setPadding(new Insets(4));
        return box;
    }
}
