package com.martinpaint.color;

import javafx.scene.paint.Color;

// Small color helpers shared across the app
public final class ColorUtils {

    private ColorUtils() {}

    public static String toWebHex(Color c) {
        return String.format("#%02X%02X%02X", to255(c.getRed()), to255(c.getGreen()), to255(c.getBlue()));
    }

    public static int to255(double channel) {
        return (int) Math.round(channel * 255);
    }

    public static int toArgb(Color c) {
        int a = to255(c.getOpacity());
        int r = to255(c.getRed());
        int g = to255(c.getGreen());
        int b = to255(c.getBlue());
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
