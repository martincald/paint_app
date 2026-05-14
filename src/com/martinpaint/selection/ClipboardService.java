package com.martinpaint.selection;

import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

// Wraps system clipboard.
public class ClipboardService {

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
        WritableImage out = new WritableImage((int) raw.getWidth(), (int) raw.getHeight());
        out.getPixelWriter().setPixels(
                0, 0,
                (int) raw.getWidth(), (int) raw.getHeight(),
                raw.getPixelReader(),
                0, 0
        );
        return out;
    }
}
