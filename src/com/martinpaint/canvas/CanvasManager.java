package com.martinpaint.canvas;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

import java.util.ArrayDeque;
import java.util.Deque;

// Background and drawing layers.
public class CanvasManager {

    public static final double CANVAS_SIZE = 1024;
    private static final int   MAX_HISTORY = 50;

    // White paper layer.
    private final Canvas backgroundCanvas;
    // Drawing layer.
    private final Canvas canvas;
    private final GraphicsContext gc;
    // Temporary overlay used by tools to preview strokes without touching the drawing layer.
    private final Canvas previewCanvas;

    private final Deque<WritableImage> undoStack = new ArrayDeque<>();
    private final Deque<WritableImage> redoStack = new ArrayDeque<>();

    public CanvasManager() {
        backgroundCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        GraphicsContext bgGc = backgroundCanvas.getGraphicsContext2D();
        bgGc.setFill(Color.WHITE);
        bgGc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        // Background ignores mouse events
        backgroundCanvas.setMouseTransparent(true);
        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        gc = canvas.getGraphicsContext2D();
        // Preview canvas sits above the drawing layer; tools paint the live preview here.
        previewCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        previewCanvas.setMouseTransparent(true);
    }

    // Preview canvas, used for live stroke preview. Must be added as an overlay in the viewport.
    public Canvas getPreviewCanvas() {
        return previewCanvas;
    }

    // Drawing canvas.
    public Canvas getCanvas() {
        return canvas;
    }

    // Background canvas.
    public Canvas getBackgroundCanvas() {
        return backgroundCanvas;
    }

    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    // Clears only drawings.
    public void clear() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void saveStateForUndo() {
        WritableImage snapshot = snapshotDrawingLayer();
        if (undoStack.size() >= MAX_HISTORY) {
            undoStack.removeLast();
        }
        undoStack.push(snapshot);
        redoStack.clear();
    }

    public void undo() {
        swap(undoStack, redoStack);
    }

    public void redo() {
        swap(redoStack, undoStack);
    }

    private void swap(Deque<WritableImage> from, Deque<WritableImage> to) {
        if (from.isEmpty()) return;
        to.push(snapshotDrawingLayer());
        WritableImage restore = from.pop();
        // Replace drawing layer.
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(restore, 0, 0);
    }

    // Snapshot of the drawing layer.
    public WritableImage snapshotDrawingLayer() {
        return snapshotUnscaled(canvas);
    }

    // Final image. Used for export.
    public WritableImage snapshotUnscaled() {
        int w = (int) canvas.getWidth();
        int h = (int) canvas.getHeight();
        WritableImage out = new WritableImage(w, h);

        // Draw background first.
        SnapshotParameters bgParams = new SnapshotParameters();
        bgParams.setTransform(identityIgnoringScale(backgroundCanvas));
        backgroundCanvas.snapshot(bgParams, out);

        // Composite drawing layer on top.
        WritableImage drawing = snapshotDrawingLayer();
        var reader = drawing.getPixelReader();
        var outReader = out.getPixelReader();
        var writer = out.getPixelWriter();
        if (reader == null || outReader == null) return out;

        int[] srcPixels = new int[w * h];
        int[] dstPixels = new int[w * h];
        reader.getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), srcPixels, 0, w);
        outReader.getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), dstPixels, 0, w);

        for (int i = 0; i < srcPixels.length; i++) {
            int argb = srcPixels[i];
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) continue;
            if (a == 255) {
                dstPixels[i] = argb;
            } else {
                dstPixels[i] = blend(argb, dstPixels[i], a);
            }
        }
        writer.setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), dstPixels, 0, w);
        return out;
    }

    // Alpha compositing.
    private static int blend(int src, int dst, int srcA) {
        int sa = srcA;
        int da = (dst >>> 24) & 0xFF;
        int outA = sa + da * (255 - sa) / 255;
        if (outA == 0) return 0;
        int sr = (src >> 16) & 0xFF, sg = (src >> 8) & 0xFF, sb = src & 0xFF;
        int dr = (dst >> 16) & 0xFF, dg = (dst >> 8) & 0xFF, db = dst & 0xFF;
        int or = (sr * sa + dr * da * (255 - sa) / 255) / outA;
        int og = (sg * sa + dg * da * (255 - sa) / 255) / outA;
        int ob = (sb * sa + db * da * (255 - sa) / 255) / outA;
        return (outA << 24) | (or << 16) | (og << 8) | ob;
    }

    // Snapshot at unscaled size.
    public static WritableImage snapshotUnscaled(Canvas c) {
        WritableImage img = new WritableImage((int) c.getWidth(), (int) c.getHeight());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setTransform(identityIgnoringScale(c));
        c.snapshot(params, img);
        return img;
    }

    private static Transform identityIgnoringScale(Canvas c) {
        Transform combined = Transform.scale(1, 1);
        for (Transform t : c.getTransforms()) {
            if (t instanceof Scale s) {
                double sx = s.getX() == 0 ? 1 : 1.0 / s.getX();
                double sy = s.getY() == 0 ? 1 : 1.0 / s.getY();
                combined = combined.createConcatenation(Transform.scale(sx, sy));
            }
        }
        return combined;
    }
}
