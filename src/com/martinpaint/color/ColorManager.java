package com.martinpaint.color;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

// Manages the current color and history.
public class ColorManager {

    private final ObjectProperty<Color> currentColor    = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.WHITE);
    private final ColorHistory colorHistory = new ColorHistory();

    public ObjectProperty<Color> currentColorProperty() { return currentColor; }
    public Color getCurrentColor() { return currentColor.get(); }
    public void setCurrentColor(Color color) {
        currentColor.set(color);
        if (color != null) colorHistory.add(color);
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColor; }
    public Color getBackgroundColor() { return backgroundColor.get(); }
    public void setBackgroundColor(Color color) { backgroundColor.set(color); }

    public void swapColors() {
        Color fg = currentColor.get();
        Color bg = backgroundColor.get();
        currentColor.set(bg);
        backgroundColor.set(fg);
        if (bg != null) colorHistory.add(bg);
    }

    public void resetColors() {
        setCurrentColor(Color.BLACK);
        backgroundColor.set(Color.WHITE);
    }

    public ColorHistory getColorHistory() { return colorHistory; }
}