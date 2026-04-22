package com.martinpaint.canvas;

import javafx.scene.image.WritableImage;

// To be used later for undo/redo

public class CanvasState {

    private final WritableImage image;
    private final long timestamp;

    public CanvasState(WritableImage image) {
        this.image = image;
        this.timestamp = System.currentTimeMillis();
    }

    public WritableImage getImage() {
        return image;
    }

    public long getTimestamp() {
        return timestamp;
    }
}