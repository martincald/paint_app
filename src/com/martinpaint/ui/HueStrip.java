package com.martinpaint.ui;

import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.function.DoubleConsumer;

/** Horizontal hue selector with a thumb. Emits hue in {@code [0, 360)}. */
final class HueStrip extends Pane {

    private final Region thumb = new Region();
    private double hue;
    private DoubleConsumer onChange = h -> {};

    HueStrip() {
        thumb.getStyleClass().add("hue-thumb");
        Panels.fixSize(thumb, 5, 16);
        thumb.setMouseTransparent(true);
        getChildren().add(thumb);

        getStyleClass().add("hue-strip");
        setPrefHeight(12);
        setMinHeight(12);
        setMaxHeight(12);
        setCursor(Cursor.H_RESIZE);

        widthProperty().addListener((_, _, _) -> positionThumb());
        setOnMousePressed(e -> emit(e.getX()));
        setOnMouseDragged(e -> emit(e.getX()));
    }

    void setOnChange(DoubleConsumer onChange) {
        this.onChange = onChange != null ? onChange : h -> {};
    }

    void setHue(double h) {
        this.hue = h;
        positionThumb();
    }

    private void emit(double x) {
        double w = getWidth();
        if (w < 1) return;
        onChange.accept(Math.clamp((x / w) * 360.0, 0.0, 359.99));
    }

    private void positionThumb() {
        double w = getWidth();
        if (w < 1) return;
        thumb.setLayoutX(hue / 360.0 * w - thumb.getPrefWidth() / 2.0);
        thumb.setLayoutY((getHeight() - thumb.getPrefHeight()) / 2.0);
    }
}
