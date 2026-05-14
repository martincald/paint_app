package com.martinpaint.io;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.InputStream;

// Loads images from classpath.
public final class ImageLoader {

    private static final Image FALLBACK = new WritableImage(16, 16);

    private ImageLoader() {}

    public static Image load(String path) {
        String resourcePath = path.startsWith("/") ? path : "/" + path;

        try (InputStream is = ImageLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[Error] Resource not found: " + resourcePath);
                return FALLBACK;
            }
            return new Image(is);
        } catch (Exception e) {
            System.err.println("[Error] Failed to load image: " + resourcePath + ": " + e.getMessage());
            return FALLBACK;
        }
    }
}