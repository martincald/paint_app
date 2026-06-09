package com.martinpaint.tools;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Base class for tools with a size setting (brush, eraser, etc.) */
public abstract class SizedTool extends Tool {

    private static final double MIN_SIZE = 1.0;
    private static final double MAX_SIZE = 50.0;

    // Minimum squared distance between recorded stroke points (1.5 px).
    // Filters near-duplicate points so the release-time replay stays lean.
    private static final double MIN_STROKE_DIST_SQ = 2.25;

    // Minimum squared movement to bother issuing a draw call (0.5 px).
    private static final double MIN_DRAW_DIST_SQ = 0.25;

    private double size;
    private double lastX;
    private double lastY;

    // Shared preview canvas provided by ToolManager.
    private Canvas previewCanvas;
    // Cached GC for the preview canvas — avoids getGraphicsContext2D() on every drag event.
    private GraphicsContext previewGc;

    // Start larger to avoid early reallocs on typical strokes.
    private double[] strokeXs = new double[512];
    private double[] strokeYs = new double[512];
    private int strokePointCount;

    protected SizedTool(ToolSpec spec) {
        super(spec);
        size = spec.defaultSize();
    }

    public double getSize() { return size; }
    public void setSize(double size) {
        double clamped = Math.clamp(size, MIN_SIZE, MAX_SIZE);
        if (Double.compare(this.size, clamped) != 0) {
            this.size = clamped;
            markSettingsChanged();
        }
    }

    @Override
    public void resetSettings() {
        setSize(getSpec().defaultSize());
    }

    // Called by ToolManager to give the tool a reference to the shared preview canvas.
    public void setPreviewCanvas(Canvas canvas) {
        this.previewCanvas = canvas;
        this.previewGc = canvas != null ? canvas.getGraphicsContext2D() : null;
    }

    // Subclasses override to true when they need per-stroke opacity (e.g. BrushTool).
    protected boolean usesOpacityPreview() { return false; }

    // The opacity to apply when committing the stroke. Defaults to fully opaque.
    protected double currentOpacity() { return 1.0; }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        lastX = x;
        lastY = y;

        if (usesOpacityPreview()) {
            clearStrokePoints();
            addStrokePoint(x, y);

            // Clear any leftover preview from the previous stroke.
            clearPreview();

            // Configure the preview canvas stroke style once at press time.
            if (previewGc != null) {
                configureStroke(previewGc);
            }
        } else {
            // Configure once per stroke — not on every drag event — saving 4 GC state
            // changes (color, width, cap, join) per mouse-moved event.
            configureStroke(gc);
        }
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        double dx = x - lastX;
        double dy = y - lastY;
        // Skip truly sub-pixel movements — no visible change, no need to issue draw calls.
        if (dx * dx + dy * dy < MIN_DRAW_DIST_SQ) return;

        if (usesOpacityPreview()) {
            // Record the point so we can replay the full stroke on release.
            addStrokePoint(x, y);

            // Draw only the new segment onto the preview canvas — no snapshot needed.
            if (previewGc != null) {
                previewGc.beginPath();
                previewGc.moveTo(lastX, lastY);
                previewGc.lineTo(x, y);
                previewGc.stroke();
            }
        } else {
            // Tools without opacity preview (eraser subclass overrides entirely, but
            // any future plain tool falls here) draw directly to the drawing canvas.
            // configureStroke was already called in onMousePressed; no repeat needed.
            gc.beginPath();
            gc.moveTo(lastX, lastY);
            gc.lineTo(x, y);
            gc.stroke();
        }

        lastX = x;
        lastY = y;
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {
        if (usesOpacityPreview() && strokePointCount > 1) {
            // Clear the preview — we are about to commit the real stroke.
            clearPreview();
            saveUndoState();

            // Replay the entire polyline as a single path so segment joins don't
            // accumulate alpha at low opacity. This is one draw call, no snapshot.
            gc.save();
            gc.setGlobalAlpha(currentOpacity());
            configureStroke(gc);
            gc.beginPath();
            gc.moveTo(strokeXs[0], strokeYs[0]);
            for (int i = 1; i < strokePointCount; i++) {
                gc.lineTo(strokeXs[i], strokeYs[i]);
            }
            gc.stroke();
            gc.restore();
            markDrawingChanged();

            clearStrokePoints();
        } else if (usesOpacityPreview()) {
            clearPreview();
            clearStrokePoints();
        }
    }

    @Override
    public void onDeactivated() {
        // If the tool is switched mid-stroke, clean up the preview.
        clearPreview();
        clearStrokePoints();
    }

    private void addStrokePoint(double x, double y) {
        // Skip points closer than 1.5 px to the last recorded point.
        // The preview is already drawn accurately via lastX/lastY; the replay path
        // benefits from fewer points without any visible quality loss.
        if (strokePointCount > 0) {
            double dx = x - strokeXs[strokePointCount - 1];
            double dy = y - strokeYs[strokePointCount - 1];
            if (dx * dx + dy * dy < MIN_STROKE_DIST_SQ) return;
        }
        if (strokePointCount >= strokeXs.length) {
            int newSize = strokeXs.length * 2;
            strokeXs = java.util.Arrays.copyOf(strokeXs, newSize);
            strokeYs = java.util.Arrays.copyOf(strokeYs, newSize);
        }
        strokeXs[strokePointCount] = x;
        strokeYs[strokePointCount] = y;
        strokePointCount++;
    }

    private void clearStrokePoints() {
        strokePointCount = 0;
    }

    // Clears the preview canvas.
    private void clearPreview() {
        if (previewGc != null) {
            previewGc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        }
    }

    // Applies the stroke style to the given GraphicsContext.
    private void configureStroke(GraphicsContext target) {
        target.setStroke(strokeColor());
        target.setLineWidth(getSize());
        target.setLineCap(StrokeLineCap.ROUND);
        target.setLineJoin(StrokeLineJoin.ROUND);
    }

    // Color for the tool stroke. Defaults to black.
    protected Color strokeColor() {
        return Color.BLACK;
    }
}
