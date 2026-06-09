package com.martinpaint.selection;

import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

// Wraps system clipboard.
class ClipboardService {

    private static final Clipboard CLIPBOARD = Clipboard.getSystemClipboard();

    // Copies image to clipboard.
    public void copy(WritableImage image) {
        ClipboardContent content = new ClipboardContent();
        content.putImage(image);
        CLIPBOARD.setContent(content);
    }

    // Returns true if clipboard has an image.
    public boolean hasImage() {
        return CLIPBOARD.hasImage();
    }

    // Returns image from clipboard.
    public WritableImage paste() {
        if (!CLIPBOARD.hasImage()) return null;
        javafx.scene.image.Image raw = CLIPBOARD.getImage();
        int width = (int) raw.getWidth();
        int height = (int) raw.getHeight();
        var reader = raw.getPixelReader();
        if (width <= 0 || height <= 0 || reader == null) return null;

        WritableImage out = new WritableImage(width, height);
        out.getPixelWriter().setPixels(
                0, 0,
                width, height,
                reader,
                0, 0
        );
        return out;
    }
}
