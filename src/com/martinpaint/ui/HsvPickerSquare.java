package com.martinpaint.ui;

import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.function.BiConsumer;

/**
 * Saturation/value pad: 2D square with a hue-driven background and a small ring marker.
 * Emits normalized {@code (s, v)} pairs as the user drags.
 */
final class HsvPickerSquare extends Pane {

    private final Region ring = new Region();
    private double sat, val;
    private BiConsumer<Double, Double> onChange = (s, v) -> {};

    HsvPickerSquare() {
        ring.getStyleClass().add("picker-ring");
        Panels.fixSize(ring, 10, 10);
        ring.setMouseTransparent(true);
        getChildren().add(ring);

        getStyleClass().add("picker");
        setPrefHeight(130);
        setMinHeight(100);
        setCursor(Cursor.CROSSHAIR);

        widthProperty().addListener((_, _, _) -> positionRing(sat, val));
        heightProperty().addListener((_, _, _) -> positionRing(sat, val));
        setOnMousePressed(e -> emit(e.getX(), e.getY()));
        setOnMouseDragged(e -> emit(e.getX(), e.getY()));
    }

    /** Subscribes to drag/click events. */
    void setOnChange(BiConsumer<Double, Double> onChange) {
        this.onChange = onChange != null ? onChange : (s, v) -> {};
    }

    /** Repaints background and ring for the new HSV state. */
    void setHsv(double h, double s, double v) {
        this.sat = s; this.val = v;
        setStyle(String.format(
                "-fx-background-color: linear-gradient(to top, black, transparent),"
                        + " linear-gradient(to right, white, hsb(%d, 100%%, 100%%));"
                        + " -fx-background-radius: 2;",
                (int) Math.round(h)));
        positionRing(s, v);
    }

    private void emit(double x, double y) {
        double w = getWidth(), ph = getHeight();
        if (w < 1 || ph < 1) return;
        double s = Math.clamp(x / w,         0.0, 1.0);
        double v = Math.clamp(1.0 - y / ph,  0.0, 1.0);
        onChange.accept(s, v);
    }

    private void positionRing(double s, double v) {
        double pw = getWidth(), ph = getHeight();
        if (pw < 1 || ph < 1) return;
        ring.setLayoutX(s * pw - ring.getPrefWidth() / 2.0);
        ring.setLayoutY((1.0 - v) * ph - ring.getPrefHeight() / 2.0);
    }

}
