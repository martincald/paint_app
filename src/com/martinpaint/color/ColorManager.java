package com.martinpaint.color;

import javafx.scene.paint.Color;

public class ColorManager {

    private Color currentColor;

    public ColorManager() {
        this.currentColor = Color.BLACK;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }
}