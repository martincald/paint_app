package com.martinpaint.tools;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.ArrayList;
import java.util.List;

// Base class for tools with a size setting (brush, eraser, etc.)
public abstract class SizedTool extends Tool {

    private double size;
    private double lastX;
    private double lastY;

    // Shared preview canvas provided by ToolManager.
    private Canvas previewCanvas;

    // Stroke points collected during a drag, used to replay the stroke on release.
    // Each entry is [x, y].
    private final List<double[]> strokePoints = new ArrayList<>();

    protected SizedTool(double initialSize) {
        this.size = initialSize;
    }

    public double getSize() { return size; }
    public void setSize(double size) { this.size = size; }

    // Called by ToolManager to give the tool a reference to the shared preview canvas.
    public void setPreviewCanvas(Canvas canvas) {
        this.previewCanvas = canvas;
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
            strokePoints.clear();
            strokePoints.add(new double[]{x, y});

            // Clear any leftover preview from the previous stroke.
            clearPreview();

            // Configure the preview canvas stroke style once at press time.
            if (previewCanvas != null) {
                configureStroke(previewCanvas.getGraphicsContext2D());
            }
        }
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        if (usesOpacityPreview()) {
            // Record the point so we can replay the full stroke on release.
            strokePoints.add(new double[]{x, y});

            // Draw only the new segment onto the preview canvas — no snapshot needed.
            if (previewCanvas != null) {
                GraphicsContext previewGc = previewCanvas.getGraphicsContext2D();
                previewGc.beginPath();
                previewGc.moveTo(lastX, lastY);
                previewGc.lineTo(x, y);
                previewGc.stroke();
            }
        } else {
            // Tools without opacity preview (eraser subclass overrides entirely, but
            // any future plain tool falls here) draw directly to the drawing canvas.
            configureStroke(gc);
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
        if (usesOpacityPreview() && !strokePoints.isEmpty()) {
            // Clear the preview — we are about to commit the real stroke.
            clearPreview();

            // Replay the entire polyline as a single path so segment joins don't
            // accumulate alpha at low opacity. This is one draw call, no snapshot.
            gc.save();
            gc.setGlobalAlpha(currentOpacity());
            configureStroke(gc);
            gc.beginPath();
            double[] first = strokePoints.get(0);
            gc.moveTo(first[0], first[1]);
            for (int i = 1; i < strokePoints.size(); i++) {
                double[] pt = strokePoints.get(i);
                gc.lineTo(pt[0], pt[1]);
            }
            gc.stroke();
            gc.restore();

            strokePoints.clear();
        }
    }

    @Override
    public void onDeactivated() {
        // If the tool is switched mid-stroke, clean up the preview.
        clearPreview();
        strokePoints.clear();
    }

    // Clears the preview canvas.
    private void clearPreview() {
        if (previewCanvas != null) {
            previewCanvas.getGraphicsContext2D()
                    .clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        }
    }

    // Applies the stroke style to the given GraphicsContext.
    private void configureStroke(GraphicsContext target) {
        target.setStroke(strokeColor());
        target.setLineWidth(size);
        target.setLineCap(StrokeLineCap.ROUND);
        target.setLineJoin(StrokeLineJoin.ROUND);
    }

    // Color for the tool stroke. Defaults to black.
    protected Color strokeColor() {
        return Color.BLACK;
    }
}
