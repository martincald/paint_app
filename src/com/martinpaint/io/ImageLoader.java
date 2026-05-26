package com.martinpaint.io;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Loads images from classpath. Results are cached so each resource is decoded only once.
public final class ImageLoader {

    private static final Image FALLBACK = new WritableImage(16, 16);
    private static final Map<String, Image> CACHE = new HashMap<>();

    private ImageLoader() {}

    public static Image load(String path) {
        Image cached = CACHE.get(path);
        if (cached != null) return cached;

        String resourcePath = path.startsWith("/") ? path : "/" + path;

        try (InputStream is = ImageLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[Error] Resource not found: " + resourcePath);
                return FALLBACK;
            }
            Image img = new Image(is);
            CACHE.put(path, img);
            return img;
        } catch (Exception e) {
            System.err.println("[Error] Failed to load image: " + resourcePath + ": " + e.getMessage());
            return FALLBACK;
        }
    }
}
