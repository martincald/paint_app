package com.martinpaint.ui;

import com.martinpaint.app.HapticFeedback;
import com.martinpaint.tools.FillTool;
import com.martinpaint.tools.OpacityAware;
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
        VBox container = new VBox(8);
        container.setPadding(new Insets(4));

        // Size slider shown for any SizedTool
        if (tool instanceof SizedTool sized) {
            container.getChildren().add(sizeSlider(sized));
        }

        // Opacity slider shown only for tools that support it
        if (tool instanceof OpacityAware op) {
            container.getChildren().add(opacitySlider(op));
        }

        // Tolerance slider shown for FillTool.
        if (tool instanceof FillTool fill) {
            container.getChildren().add(toleranceSlider(fill));
        }

        // Return null if there are no sliders so the placeholder text shows instead.
        return container.getChildren().isEmpty() ? null : container;
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

    private static VBox opacitySlider(OpacityAware tool) {
        int initialPercent = (int) Math.round(tool.getOpacity() * 100);
        Label label = new Label("Opacity: " + initialPercent + "%");
        label.getStyleClass().add("tool-slider-label");

        Slider slider = new Slider(1, 100, initialPercent);
        slider.getStyleClass().add("tool-slider");
        slider.valueProperty().addListener((_, oldVal, newVal) -> {
            int oldPct = (int) oldVal.doubleValue();
            int newPct = (int) newVal.doubleValue();
            tool.setOpacity(newPct / 100.0);
            label.setText("Opacity: " + newPct + "%");
            if (oldPct != newPct) {
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
