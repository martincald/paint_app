package com.martinpaint.color;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

//class that keeps track of recently used colors

public class ColorHistory {

    private static final int MAX_CAPACITY = 5;

    private final ObservableList<Color> colors = FXCollections.observableArrayList();

    public ColorHistory() {
        // Pre-fill with white so UI slots are never empty.
        for (int i = 0; i < MAX_CAPACITY; i++) {
            colors.add(Color.WHITE);
        }
    }

    // pushes a color to the history
    public void push(Color color) {
        if (color == null) {
            return;
        }
        colors.remove(color);
        colors.add(0, color);
        if (colors.size() > MAX_CAPACITY) {
            colors.remove(colors.size() - 1);
        }
    }

    //returns the list of colors
    public ObservableList<Color> getColors() {
        return colors;
    }
}
