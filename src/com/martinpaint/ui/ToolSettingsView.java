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

import java.util.function.IntConsumer;

/** Builds the active tool settings panel content. */
final class ToolSettingsView {

    private ToolSettingsView() {}

    static Node create(Tool tool) {
        VBox grid = new VBox(6);
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

    // One property row: label + slider + value box.
    private static HBox propRow(String labelText, Node control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("prop-row-label");
        lbl.setPrefWidth(72);
        lbl.setMinWidth(72);
        HBox row = new HBox(8, lbl, control);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(control, Priority.ALWAYS);
        return row;
    }

    private static HBox sizeSlider(SizedTool tool) {
        return numericSlider(1, 50, (int) tool.getSize(), "", tool::setSize);
    }

    private static HBox opacitySlider(OpacityAware tool) {
        int initPct = (int) Math.round(tool.getOpacity() * 100);
        return numericSlider(1, 100, initPct, "%", pct -> tool.setOpacity(pct / 100.0));
    }

    private static HBox toleranceSlider(FillTool tool) {
        return numericSlider(0, 100, tool.getTolerance(), "", tool::setTolerance);
    }

    private static HBox numericSlider(int min, int max, int initial, String suffix, IntConsumer apply) {
        Slider slider = styledSlider(min, max, initial);
        TextField valBox = valueBox(formatValue(initial, suffix));
        slider.valueProperty().addListener((_, oldVal, newVal) -> {
            int v = newVal.intValue();
            apply.accept(v);
            valBox.setText(formatValue(v, suffix));
            if (oldVal.intValue() != v) HapticFeedback.sliderTick();
        });
        valBox.setOnAction(_ -> parseAndApply(valBox, min, max, slider));
        valBox.focusedProperty().addListener((_, _, focused) -> {
            if (!focused) parseAndApply(valBox, min, max, slider);
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
            slider.setValue(Math.clamp(v, min, max));
        } catch (NumberFormatException ignored) {}
    }

    private static String formatValue(int value, String suffix) {
        return value + suffix;
    }
}
