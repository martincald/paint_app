package com.martinpaint.tools;

import com.martinpaint.canvas.CanvasManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

// Base class for tools with a size setting
public abstract class SizedTool extends Tool {

    private double size;
    private double lastX;
    private double lastY;

    // Offscreen canvas used by tools that need uniform per-stroke opacity.
    private Canvas strokeBuffer;
    private GraphicsContext bufferGc;

    // Transparent canvas that sits above the drawing layer for live previews.
    private Canvas previewCanvas;

    protected SizedTool(double initialSize) {
        this.size = initialSize;
    }

    public double getSize() { return size; }
    public void setSize(double size) { this.size = size; }

    // Called by ToolManager to give the tool a reference to the shared preview canvas.
    public void setPreviewCanvas(Canvas canvas) {
        this.previewCanvas = canvas;
    }

    // Subclasses override to true when they need the opacity buffer
    protected boolean usesStrokeBuffer() { return false; }

    // The opacity to apply when flattening the buffer. Defaults to fully opaque.
    protected double currentOpacity() { return 1.0; }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        lastX = x;
        lastY = y;

        if (usesStrokeBuffer()) {
            double w = gc.getCanvas().getWidth();
            double h = gc.getCanvas().getHeight();

            // Create a fresh offscreen canvas that will accumulate this stroke, fully opaque.
            strokeBuffer = new Canvas(w, h);
            bufferGc = strokeBuffer.getGraphicsContext2D();
            configureStroke(bufferGc);

            // Clear any leftover preview from the previous stroke.
            if (previewCanvas != null) {
                previewCanvas.getGraphicsContext2D()
                        .clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
            }
        }
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        if (usesStrokeBuffer()) {
            // Add the new segment to the offscreen buffer (fully opaque accumulation).
            bufferGc.beginPath();
            bufferGc.moveTo(lastX, lastY);
            bufferGc.lineTo(x, y);
            bufferGc.stroke();

            // Paint the live preview onto the preview canvas so the user sees the stroke.
            if (previewCanvas != null) {
                GraphicsContext previewGc = previewCanvas.getGraphicsContext2D();
                previewGc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
                previewGc.save();
                previewGc.setGlobalAlpha(currentOpacity());
                previewGc.drawImage(CanvasManager.snapshotUnscaled(strokeBuffer), 0, 0);
                previewGc.restore();
            }
        } else {
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
        if (usesStrokeBuffer() && strokeBuffer != null) {
            // Clear the preview  the real stroke is about to be committed to the drawing layer.
            if (previewCanvas != null) {
                previewCanvas.getGraphicsContext2D()
                        .clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
            }

            // Flatten the full stroke onto the drawing layer exactly once at the chosen opacity.
            gc.save();
            gc.setGlobalAlpha(currentOpacity());
            gc.drawImage(CanvasManager.snapshotUnscaled(strokeBuffer), 0, 0);
            gc.restore();

            strokeBuffer = null;
            bufferGc     = null;
        }
    }

    // Applies the stroke style
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
