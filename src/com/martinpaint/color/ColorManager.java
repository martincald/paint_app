package com.martinpaint.color;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

// Manages the current color and history.
public class ColorManager {

    private final ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.BLACK);
    private final ColorHistory colorHistory = new ColorHistory();

    public Color getCurrentColor() {
        return currentColor.get();
    }

    public void setCurrentColor(Color color) {
        currentColor.set(color);
        if (color != null) {
            colorHistory.add(color);
        }
    }

    public ColorHistory getColorHistory() {
        return colorHistory;
    }
}