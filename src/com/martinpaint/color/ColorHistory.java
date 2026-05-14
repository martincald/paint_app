package com.martinpaint.color;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

// Recently used colors.
public class ColorHistory {

    private static final int MAX_CAPACITY = 5;

    private final ObservableList<Color> colors = FXCollections.observableArrayList();

    public void add(Color color) {
        if (color == null || colors.contains(color)) return;
        colors.add(0, color);
        if (colors.size() > MAX_CAPACITY) {
            colors.remove(colors.size() - 1);
        }
    }

    public ObservableList<Color> getColors() {
        return colors;
    }
}
