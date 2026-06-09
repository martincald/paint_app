package com.martinpaint.canvas;

import javafx.scene.image.WritableImage;

public record LayerState(WritableImage image, String name, boolean visible, double opacity) {}
