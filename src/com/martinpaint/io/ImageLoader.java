package com.martinpaint.io;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.File;

//image loading utility, all images should be in the resources/images folder
public final class ImageLoader {

    private static final String IMAGES_DIR = "Resources/Images/";

    private ImageLoader() {
    }


    public static Image load(String path) {
        String filename = path;
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            filename = path.substring(lastSeparator + 1);
        }

        File file = new File(IMAGES_DIR + filename);
        if (!file.exists()) {
            System.err.println("img not found" + file.getAbsolutePath());
            return new WritableImage(16, 16);
        }

        try {
            return new Image(file.toURI().toString());
        } catch (Exception e) {
            System.err.println("failed to get img: " + file.getAbsolutePath()
                    + " — " + e.getMessage());
            return new WritableImage(16, 16);
        }
    }
}