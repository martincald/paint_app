package com.martinpaint.app;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//JNI wrapper around macOS haptic feedback.
public final class HapticFeedback {

    // Haptic patterns matching NSHapticFeedbackPattern values.
    public static final int GENERIC      = 0;
    public static final int ALIGNMENT    = 1;

    private static volatile boolean enabled   = true;
    private static volatile boolean loaded    = false;
    private static volatile boolean available = false;

    private static final String LIB_NAME     = "HapticFeedback";
    private static final String LIB_FILE     = "libHapticFeedback.dylib";
    private static final String LIB_RESOURCE = "/native/libHapticFeedback.dylib";

    private static native void performHaptic(int pattern);

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    // Haptics when switching tools
    public static void toolSwitch() {
        perform(GENERIC);
    }

    // haptics when sliding the slider
    public static void sliderTick() {
        perform(ALIGNMENT);
    }

    public static void perform(int pattern) {
        if (!enabled) return;
        ensureLoaded();
        if (!available) return;
        try {
            performHaptic(pattern);
        } catch (Throwable t) {
            // not crash app because of haptic feedback
        }
    }

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (!isMacOS()) return;

        // check java.library.path first.
        try {
            System.loadLibrary(LIB_NAME);
            available = true;
            return;
        } catch (UnsatisfiedLinkError ignored) { }

        // check for common project locations
        for (Path candidate : candidatePaths()) {
            try {
                if (candidate != null && Files.isRegularFile(candidate)) {
                    System.load(candidate.toAbsolutePath().toString());
                    available = true;
                    return;
                }
            } catch (Throwable ignored) { }
        }

        // else extract from jar from bundle
        try {
            InputStream in = HapticFeedback.class.getResourceAsStream(LIB_RESOURCE);
            if (in == null) return;

            Path tmp = Files.createTempFile("libHapticFeedback", ".dylib");
            tmp.toFile().deleteOnExit();

            try (InputStream src = in;
                 OutputStream dst = Files.newOutputStream(tmp)) {
                src.transferTo(dst);
            }

            System.load(tmp.toAbsolutePath().toString());
            available = true;
        } catch (Throwable ignored) { }
    }

    // Working dir, native/, the running jar/classes dir, and a few parents up.
    private static Path[] candidatePaths() {
        java.util.List<Path> list = new java.util.ArrayList<>();

        Path cwd = Paths.get("").toAbsolutePath();
        list.add(cwd.resolve(LIB_FILE));
        list.add(cwd.resolve("native").resolve(LIB_FILE));

        try {
            File codeSource = new File(HapticFeedback.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI());
            File baseDir = codeSource.isDirectory() ? codeSource : codeSource.getParentFile();
            if (baseDir != null) {
                Path base = baseDir.toPath();
                list.add(base.resolve(LIB_FILE));
                list.add(base.resolve("native").resolve(LIB_FILE));
                if (base.getParent() != null) {
                    list.add(base.getParent().resolve(LIB_FILE));
                    list.add(base.getParent().resolve("native").resolve(LIB_FILE));
                    Path up = base.getParent();
                    for (int i = 0; i < 3 && up.getParent() != null; i++) {
                        up = up.getParent();
                        list.add(up.resolve(LIB_FILE));
                        list.add(up.resolve("native").resolve(LIB_FILE));
                    }
                }
            }
        } catch (Throwable ignored) { }

        return list.toArray(new Path[0]);
    }

    private static boolean isMacOS() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("mac") || os.contains("darwin");
    }

    private HapticFeedback() {}
}
