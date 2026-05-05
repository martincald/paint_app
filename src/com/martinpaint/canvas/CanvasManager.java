package com.martinpaint.canvas;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

// Drawing canvas and a small undo/redo stack of bitmap snapshots
public class CanvasManager {

    public static final double CANVAS_SIZE = 1024;
    private static final int   MAX_HISTORY = 50;

    private final Canvas canvas;
    private final GraphicsContext gc;

    private final Deque<BufferedImage> undoStack = new ArrayDeque<>();
    private final Deque<BufferedImage> redoStack = new ArrayDeque<>();

    public CanvasManager() {
        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        gc = canvas.getGraphicsContext2D();
        clear();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    public void clear() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void saveStateForUndo() {
        WritableImage snapshot = snapshotUnscaled();
        BufferedImage buf = SwingFXUtils.fromFXImage(snapshot, null);
        if (undoStack.size() >= MAX_HISTORY) {
            undoStack.removeLast();
        }
        undoStack.push(buf);
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        swap(undoStack, redoStack);
    }

    public void redo() {
        swap(redoStack, undoStack);
    }

    private void swap(Deque<BufferedImage> from, Deque<BufferedImage> to) {
        if (from.isEmpty()) return;
        WritableImage current = snapshotUnscaled();
        to.push(SwingFXUtils.fromFXImage(current, null));
        WritableImage restore = SwingFXUtils.toFXImage(from.pop(), null);
        gc.drawImage(restore, 0, 0);
    }

    // Snapshot at the canvas unscaled pixel size, ignoring any viewport scale.
    public WritableImage snapshotUnscaled() {
        return snapshotUnscaled(canvas);
    }

    public static WritableImage snapshotUnscaled(Canvas c) {
        WritableImage img = new WritableImage((int) c.getWidth(), (int) c.getHeight());
        SnapshotParameters params = new SnapshotParameters();
        Transform combined = Transform.scale(1, 1);
        for (Transform t : c.getTransforms()) {
            if (t instanceof Scale s) {
                double sx = s.getX() == 0 ? 1 : 1.0 / s.getX();
                double sy = s.getY() == 0 ? 1 : 1.0 / s.getY();
                combined = combined.createConcatenation(Transform.scale(sx, sy));
            }
        }
        params.setTransform(combined);
        c.snapshot(params, img);
        return img;
    }
}
