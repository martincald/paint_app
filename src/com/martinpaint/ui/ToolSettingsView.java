package com.martinpaint.ui;

import com.martinpaint.app.HapticFeedback;
import com.martinpaint.tools.FillTool;
import com.martinpaint.tools.OpacityAware;
import com.martinpaint.tools.SizedTool;
import com.martinpaint.tools.Tool;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Builds the tool settings panel content. Uses the design's prop-row layout.
final class ToolSettingsView {

    private ToolSettingsView() {}

    static Node create(Tool tool) {
        VBox grid = new VBox(6);
        grid.getStyleClass().add("prop-grid");
        grid.setPadding(new Insets(8, 10, 8, 10));

        if (tool instanceof SizedTool sized) {
            grid.getChildren().add(propRow("Size", sizeSlider(sized)));
        }
        if (tool instanceof OpacityAware op) {
            grid.getChildren().add(propRow("Opacity", opacitySlider(op)));
        }
        if (tool instanceof FillTool fill) {
            grid.getChildren().add(propRow("Tolerance", toleranceSlider(fill)));
        }

        return grid.getChildren().isEmpty() ? null : grid;
    }

    // One prop-row: label (80px) + slider + value box
    private static HBox propRow(String labelText, Node control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("prop-row-label");
        lbl.setPrefWidth(72);
        lbl.setMinWidth(72);
        HBox row = new HBox(8, lbl, control);
        row.getStyleClass().add("prop-row");
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(control, Priority.ALWAYS);
        return row;
    }

    private static HBox sizeSlider(SizedTool tool) {
        Slider slider = styledSlider(1, 50, tool.getSize());
        TextField valBox = valueBox(String.valueOf((int) tool.getSize()));
        slider.valueProperty().addListener((_, oldVal, newVal) -> {
            tool.setSize(newVal.doubleValue());
            valBox.setText(String.valueOf((int) tool.getSize()));
            if ((int) oldVal.doubleValue() != (int) newVal.doubleValue()) HapticFeedback.sliderTick();
        });
        valBox.setOnAction(_ -> parseAndApply(valBox, 1, 50, slider));
        valBox.focusedProperty().addListener((_, _, focused) -> {
            if (!focused) parseAndApply(valBox, 1, 50, slider);
        });
        return sliderRow(slider, valBox);
    }

    private static HBox opacitySlider(OpacityAware tool) {
        int initPct = (int) Math.round(tool.getOpacity() * 100);
        Slider slider = styledSlider(1, 100, initPct);
        TextField valBox = valueBox(initPct + "%");
        slider.valueProperty().addListener((_, oldVal, newVal) -> {
            int pct = (int) newVal.doubleValue();
            tool.setOpacity(pct / 100.0);
            valBox.setText(pct + "%");
            if ((int) oldVal.doubleValue() != pct) HapticFeedback.sliderTick();
        });
        return sliderRow(slider, valBox);
    }

    private static HBox toleranceSlider(FillTool tool) {
        Slider slider = styledSlider(0, 100, tool.getTolerance());
        TextField valBox = valueBox(String.valueOf(tool.getTolerance()));
        slider.valueProperty().addListener((_, _, newVal) -> {
            int v = newVal.intValue();
            if (v != tool.getTolerance()) HapticFeedback.sliderTick();
            tool.setTolerance(v);
            valBox.setText(String.valueOf(v));
        });
        valBox.setOnAction(_ -> parseAndApply(valBox, 0, 100, slider));
        valBox.focusedProperty().addListener((_, _, focused) -> {
            if (!focused) parseAndApply(valBox, 0, 100, slider);
        });
        return sliderRow(slider, valBox);
    }

    private static Slider styledSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val);
        s.getStyleClass().add("prop-slider");
        HBox.setHgrow(s, Priority.ALWAYS);
        return s;
    }

    private static TextField valueBox(String text) {
        TextField tf = new TextField(text);
        tf.getStyleClass().add("prop-number");
        tf.setPrefWidth(44);
        tf.setMinWidth(44);
        tf.setMaxWidth(44);
        return tf;
    }

    private static HBox sliderRow(Slider slider, TextField valBox) {
        HBox row = new HBox(6, slider, valBox);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(slider, Priority.ALWAYS);
        return row;
    }

    private static void parseAndApply(TextField tf, double min, double max, Slider slider) {
        try {
            String raw = tf.getText().replaceAll("[^0-9.]", "");
            double v = Double.parseDouble(raw);
            slider.setValue(Math.max(min, Math.min(max, v)));
        } catch (NumberFormatException ignored) {}
    }
}
