package com.martinpaint.canvas;

import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BooleanSupplier;

public class CanvasManager {

    public static final double CANVAS_SIZE = 1024;
    private static final int   MAX_HISTORY = 50;
    private static final long  MAX_HISTORY_BYTES = 256L * 1024L * 1024L;
    private static final SnapshotParameters TRANSPARENT_SNAPSHOT_PARAMS = new SnapshotParameters();

    static {
        TRANSPARENT_SNAPSHOT_PARAMS.setFill(Color.TRANSPARENT);
    }

    // White paper layer.
    private final Canvas backgroundCanvas;
    // Stable, transparent canvas that is the sole mouse-event target.
    // Its position in the Z-order is always just above all layer canvases and
    // just below the preview canvas.  Tools wire listeners to this canvas via
    // getCanvas(); its reference never changes as layers are added or removed.
    private final Canvas interactionCanvas;
    // Temporary overlay used by tools to preview strokes without touching any layer.
    private final Canvas previewCanvas;

    private final LayerManager layerManager;

    private final Deque<CanvasSnapshot> undoStack = new ArrayDeque<>();
    private final Deque<CanvasSnapshot> redoStack = new ArrayDeque<>();
    private long undoStackBytes;
    private long redoStackBytes;
    private boolean hasDrawingContent;

    // Increments whenever layer pixels change.
    // LayerPanel listens to this to know when to refresh the active layer thumbnail.
    private final ReadOnlyLongWrapper drawingStamp = new ReadOnlyLongWrapper(0);
    public ReadOnlyLongProperty drawingStampProperty() { return drawingStamp.getReadOnlyProperty(); }

    public CanvasManager() {
        backgroundCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        GraphicsContext bgGc = backgroundCanvas.getGraphicsContext2D();
        bgGc.setFill(Color.WHITE);
        bgGc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        backgroundCanvas.setMouseTransparent(true);

        layerManager = new LayerManager();

        // Interaction canvas: transparent, receives mouse events, never drawn on.
        interactionCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        interactionCanvas.setMouseTransparent(false);

        previewCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        previewCanvas.setMouseTransparent(true);
    }

    // ── Canvas accessors ─────────────────────────────────────────────────────

    /** Stable mouse-event target canvas. ToolManager wires all listeners here. */
    public Canvas getCanvas() {
        return interactionCanvas;
    }

    /**
     * Active layer's canvas.  Use this when you need to read pixels from the
     * current drawing surface (e.g. SelectionController.pickup()).
     */
    public Canvas getActiveLayerCanvas() {
        return layerManager.getActiveLayer().getCanvas();
    }

    /** Preview (stroke-overlay) canvas. Always above all layer canvases. */
    public Canvas getPreviewCanvas() {
        return previewCanvas;
    }

    /** White background canvas. Always the bottom-most canvas. */
    public Canvas getBackgroundCanvas() {
        return backgroundCanvas;
    }

    /**
     * Dynamically returns the active layer's GraphicsContext.
     * This value changes whenever the user switches the active layer.
     * ToolManager must NOT capture this at construction time.
     */
    public GraphicsContext getGraphicsContext() {
        return layerManager.getActiveLayer().getGc();
    }

    public LayerManager getLayerManager() {
        return layerManager;
    }

    // ── Drawing state ────────────────────────────────────────────────────────

    /** Clears all layers to transparent. */
    public void clear() {
        for (Layer layer : layerManager.getLayers()) {
            layer.clear();
        }
        publishDrawingChange(false);
    }

    /** Clears only the active layer to transparent, leaving other layers untouched. */
    public void clearActiveLayer() {
        layerManager.getActiveLayer().clear();
        publishDrawingChange(hasDrawingContent && anyCurrentLayerHasContent());
    }

    /** Resets the project to a single blank "Layer 1", discarding all other layers. */
    public void resetProject() {
        layerManager.resetToSingleBlankLayer();
        publishDrawingChange(false);
    }

    public boolean hasDrawingContent() {
        return hasDrawingContent;
    }

    public void markDrawingChanged() {
        publishDrawingChange(true);
    }

    // ── Undo / redo ──────────────────────────────────────────────────────────

    public void saveStateForUndo() {
        CanvasSnapshot snapshot = captureSnapshot();
        clearRedoStack();
        pushUndoSnapshot(snapshot);
    }

    public void discardLastUndoState() {
        if (!undoStack.isEmpty()) {
            popUndoSnapshot();
        }
    }

    public void runUndoable(Runnable action) {
        saveStateForUndo();
        action.run();
    }

    public boolean runUndoableChange(BooleanSupplier action) {
        saveStateForUndo();
        if (action.getAsBoolean()) return true;
        discardLastUndoState();
        return false;
    }

    public void undo() { swap(true); }
    public void redo() { swap(false); }

    private void swap(boolean undoing) {
        Deque<CanvasSnapshot> from = undoing ? undoStack : redoStack;
        if (from.isEmpty()) return;

        CanvasSnapshot current = captureSnapshot();
        CanvasSnapshot restore;
        if (undoing) {
            redoStack.push(current);
            redoStackBytes += snapshotBytes(current);
            restore = popUndoSnapshot();
            trimRedoStack();
        } else {
            undoStack.push(current);
            undoStackBytes += snapshotBytes(current);
            restore = popRedoSnapshot();
            trimUndoStack();
        }
        trimCombinedHistory();

        layerManager.restoreFromSnapshot(restore.layers(), restore.activeLayerIndex());
        publishDrawingChange(anyLayerHasContent(restore.layers()));
    }

    private void pushUndoSnapshot(CanvasSnapshot snapshot) {
        undoStack.push(snapshot);
        undoStackBytes += snapshotBytes(snapshot);
        trimUndoStack();
        trimCombinedHistory();
    }

    private CanvasSnapshot popUndoSnapshot() {
        CanvasSnapshot snapshot = undoStack.pop();
        undoStackBytes -= snapshotBytes(snapshot);
        return snapshot;
    }

    private CanvasSnapshot popRedoSnapshot() {
        CanvasSnapshot snapshot = redoStack.pop();
        redoStackBytes -= snapshotBytes(snapshot);
        return snapshot;
    }

    private void clearRedoStack() {
        redoStack.clear();
        redoStackBytes = 0;
    }

    private void trimUndoStack() {
        while (undoStack.size() > MAX_HISTORY) removeOldestUndoSnapshot();
    }

    private void trimRedoStack() {
        while (redoStack.size() > MAX_HISTORY) removeOldestRedoSnapshot();
    }

    private void trimCombinedHistory() {
        while (undoStackBytes + redoStackBytes > MAX_HISTORY_BYTES
                && undoStack.size() + redoStack.size() > 1) {
            if (redoStack.isEmpty()) {
                if (undoStack.size() <= 1) break;
                removeOldestUndoSnapshot();
            } else if (undoStack.isEmpty()) {
                if (redoStack.size() <= 1) break;
                removeOldestRedoSnapshot();
            } else if (undoStackBytes >= redoStackBytes && undoStack.size() > 1) {
                removeOldestUndoSnapshot();
            } else if (redoStack.size() > 1) {
                removeOldestRedoSnapshot();
            } else if (undoStack.size() > 1) {
                removeOldestUndoSnapshot();
            } else {
                break;
            }
        }
    }

    private void removeOldestUndoSnapshot() {
        CanvasSnapshot removed = undoStack.removeLast();
        undoStackBytes -= snapshotBytes(removed);
    }

    private void removeOldestRedoSnapshot() {
        CanvasSnapshot removed = redoStack.removeLast();
        redoStackBytes -= snapshotBytes(removed);
    }

    private static long snapshotBytes(CanvasSnapshot snapshot) {
        long bytes = 0;
        for (LayerState state : snapshot.layers()) {
            WritableImage image = state.image();
            bytes += (long) image.getWidth() * (long) image.getHeight() * Integer.BYTES;
        }
        return bytes;
    }

    /** Builds a full snapshot of all layers plus the active-layer index. */
    private CanvasSnapshot captureSnapshot() {
        return new CanvasSnapshot(captureLayerStates(), layerManager.getActiveLayerIndex());
    }

    /** Captures the current pixel/state data for every layer (used for snapshots and content checks). */
    private List<LayerState> captureLayerStates() {
        List<LayerState> states = new ArrayList<>();
        for (Layer layer : layerManager.getLayers()) {
            states.add(new LayerState(
                    layer.snapshot(),
                    layer.getName(),
                    layer.isVisible(),
                    layer.getOpacity()
            ));
        }
        return List.copyOf(states);
    }

    private static boolean anyLayerHasContent(List<LayerState> states) {
        for (LayerState state : states) {
            if (imageHasContent(state.image())) return true;
        }
        return false;
    }

    private boolean anyCurrentLayerHasContent() {
        for (Layer layer : layerManager.getLayers()) {
            if (imageHasContent(layer.snapshot())) return true;
        }
        return false;
    }

    private void publishDrawingChange(boolean hasContent) {
        hasDrawingContent = hasContent;
        drawingStamp.set(drawingStamp.get() + 1);
    }

    // ── Snapshot / export ────────────────────────────────────────────────────

    /**
     * Snapshot of the active drawing layer only.
     * Kept for backward compatibility; prefer snapshotUnscaled() for export.
     */
    public WritableImage snapshotDrawingLayer() {
        return snapshotUnscaled(layerManager.getActiveLayer().getCanvas());
    }

    /** Final export image: visible layers composited over white. */
    public WritableImage snapshotUnscaled() {
        return renderVisibleLayers(true);
    }

    /** Visible-layer composite retaining alpha, for eyedropper sampling. */
    public WritableImage snapshotComposite() {
        return renderVisibleLayers(false);
    }

    private WritableImage renderVisibleLayers(boolean whiteBackground) {
        Canvas output = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        GraphicsContext gc = output.getGraphicsContext2D();
        if (whiteBackground) {
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        }
        for (Layer layer : layerManager.getLayers()) {
            double opacity = layer.getOpacity();
            if (!layer.isVisible() || opacity <= 0.0) continue;
            gc.setGlobalAlpha(opacity);
            gc.drawImage(layer.snapshot(), 0, 0);
        }
        return snapshotUnscaled(output);
    }

    /** Snapshot a canvas at its logical pixel size with a transparent fill. */
    public static WritableImage snapshotUnscaled(Canvas c) {
        WritableImage img = new WritableImage((int) c.getWidth(), (int) c.getHeight());
        c.snapshot(transparentSnapshotParameters(), img);
        return img;
    }

    static SnapshotParameters transparentSnapshotParameters() {
        return TRANSPARENT_SNAPSHOT_PARAMS;
    }

    private static boolean imageHasContent(WritableImage image) {
        var reader = image.getPixelReader();
        if (reader == null) return false;
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        int[] row = new int[w];
        for (int y = 0; y < h; y++) {
            reader.getPixels(0, y, w, 1, PixelFormat.getIntArgbInstance(), row, 0, w);
            for (int pixel : row) {
                if ((pixel >>> 24) != 0) return true;
            }
        }
        return false;
    }
}
