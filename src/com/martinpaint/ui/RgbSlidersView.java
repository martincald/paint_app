package com.martinpaint.ui;

import com.martinpaint.color.ColorUtils;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.ObjIntConsumer;

/**
 * Three RGB channel sliders with corresponding numeric value fields.
 * Emits {@code (channelIndex, value0..255)} on any user interaction.
 */
final class RgbSlidersView extends VBox {

    private static final String[] LABELS = {"R", "G", "B"};

    private final Pane[]      tracks = new Pane[3];
    private final Region[]    thumbs = new Region[3];
    private final TextField[] fields = new TextField[3];

    private boolean updating;
    private ObjIntConsumer<Integer> onChange = (c, v) -> {};

    RgbSlidersView() {
        super(4);
        for (int i = 0; i < 3; i++) buildRow(i);
    }

    /** {@code (channelIndex, value)} accepts callbacks from user drag or field edit. */
    void setOnChange(ObjIntConsumer<Integer> onChange) {
        this.onChange = onChange != null ? onChange : (c, v) -> {};
    }

    /** Push the displayed color into thumbs, gradient backgrounds and value fields. */
    void setColor(Color c) {
        int r = ColorUtils.to255(c.getRed());
        int g = ColorUtils.to255(c.getGreen());
        int b = ColorUtils.to255(c.getBlue());
        applyGradients(r, g, b);
        applyThumb(0, r);
        applyThumb(1, g);
        applyThumb(2, b);
        applyField(0, r);
        applyField(1, g);
        applyField(2, b);
    }

    /** Refresh only thumbs/gradients (used when caller wants to keep field focus undisturbed). */
    void refreshThumbsAndGradients(Color c) {
        int r = ColorUtils.to255(c.getRed());
        int g = ColorUtils.to255(c.getGreen());
        int b = ColorUtils.to255(c.getBlue());
        applyGradients(r, g, b);
        applyThumb(0, r);
        applyThumb(1, g);
        applyThumb(2, b);
    }

    // ── Construction ────────────────────────────────────────────

    private void buildRow(int i) {
        tracks[i] = new Pane();
        tracks[i].getStyleClass().add("slider-track");
        tracks[i].setPrefHeight(8);
        tracks[i].setMinHeight(8);
        tracks[i].setMaxHeight(8);
        tracks[i].setCursor(Cursor.H_RESIZE);

        thumbs[i] = new Region();
        thumbs[i].getStyleClass().add("slider-thumb");
        Panels.fixSize(thumbs[i], 5, 14);
        thumbs[i].setLayoutY(-3);
        thumbs[i].setMouseTransparent(true);
        tracks[i].getChildren().add(thumbs[i]);

        fields[i] = new TextField("0");
        fields[i].getStyleClass().add("val");
        fields[i].setPrefWidth(38);
        fields[i].setMinWidth(38);
        fields[i].setMaxWidth(38);

        final int channel = i;
        tracks[i].widthProperty().addListener((_, _, _) -> repositionThumb(channel));
        tracks[i].setOnMousePressed(e -> emitFromDrag(channel, e.getX()));
        tracks[i].setOnMouseDragged(e -> emitFromDrag(channel, e.getX()));

        fields[i].textProperty().addListener((_, _, text) -> {
            if (updating) return;
            try {
                int clamped = Math.clamp(Integer.parseInt(text.trim()), 0, 255);
                onChange.accept(channel, clamped);
            } catch (NumberFormatException ignored) {}
        });

        Label chanLabel = new Label(LABELS[i]);
        chanLabel.getStyleClass().add("slider-ch");
        chanLabel.setPrefWidth(12);
        chanLabel.setMinWidth(12);

        HBox row = new HBox(6, chanLabel, tracks[i], fields[i]);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tracks[i], Priority.ALWAYS);
        getChildren().add(row);
    }

    // ── Event helpers ───────────────────────────────────────────

    private void emitFromDrag(int channel, double x) {
        double w = tracks[channel].getWidth();
        if (w < 1) return;
        int value = (int) Math.clamp((x / w) * 255.0, 0.0, 255.0);
        onChange.accept(channel, value);
    }

    private int currentFieldValue(int channel) {
        try {
            return Math.clamp(Integer.parseInt(fields[channel].getText().trim()), 0, 255);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ── View update helpers ─────────────────────────────────────

    private void applyGradients(int r, int g, int b) {
        tracks[0].setStyle(String.format(
                "-fx-background-color: linear-gradient(to right, rgb(0,%d,%d), rgb(255,%d,%d));"
                        + " -fx-background-radius: 1;", g, b, g, b));
        tracks[1].setStyle(String.format(
                "-fx-background-color: linear-gradient(to right, rgb(%d,0,%d), rgb(%d,255,%d));"
                        + " -fx-background-radius: 1;", r, b, r, b));
        tracks[2].setStyle(String.format(
                "-fx-background-color: linear-gradient(to right, rgb(%d,%d,0), rgb(%d,%d,255));"
                        + " -fx-background-radius: 1;", r, g, r, g));
    }

    private void applyThumb(int channel, int value) {
        double w = tracks[channel].getWidth();
        if (w < 1) return;
        thumbs[channel].setLayoutX((value / 255.0) * w - thumbs[channel].getPrefWidth() / 2.0);
    }

    private void repositionThumb(int channel) {
        applyThumb(channel, currentFieldValue(channel));
    }

    private void applyField(int channel, int value) {
        updating = true;
        fields[channel].setText(String.valueOf(value));
        updating = false;
    }
}
