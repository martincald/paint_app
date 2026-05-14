package com.martinpaint.selection;

import com.martinpaint.canvas.CanvasManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import java.util.function.DoubleSupplier;

// Orchestrates the full selection lifecycle: IDLE > DEFINING > FLOATING > (MOVING / RESIZING) > commit / cancel / delete.
public class SelectionController {

    private static final double MIN_SELECTION_SIZE = 2.0;

    private final CanvasManager    canvasManager;
    private final SelectionOverlay overlay;
    private final ClipboardService clipboard;
    // Supplies the current viewport zoom so pickup can snapshot at 1:1 pixel resolution
    private final DoubleSupplier   viewportScale;

    private SelectionInteractionMode mode = SelectionInteractionMode.IDLE;
    private Selection selection;

    // DEFINING state
    private double defineStartX, defineStartY;

    // MOVING state
    private boolean moveDragInitialized;
    private double  movePressX, movePressY;
    private double  moveOriginX, moveOriginY;

    // RESIZING state
    private ResizeHandle activeHandle;
    private double resizeAnchorX, resizeAnchorY;
    private double resizeOrigW, resizeOrigH;
    private double resizeMouseStartX, resizeMouseStartY;

    // Cached canvas size.
    private double cachedCanvasW, cachedCanvasH;

    public SelectionController(CanvasManager canvasManager, SelectionOverlay overlay,
                               ClipboardService clipboard, DoubleSupplier viewportScale) {
        this.canvasManager  = canvasManager;
        this.overlay        = overlay;
        this.clipboard      = clipboard;
        this.viewportScale  = viewportScale;

        overlay.wireInteraction(
                this::onResizeStarted,
                xy -> onCanvasDragged(xy[0], xy[1]),
                this::onResizeEnded
        );
    }

// Public state queries

    public boolean hasFloat() {
        return selection != null &&
               (mode == SelectionInteractionMode.FLOATING ||
                mode == SelectionInteractionMode.MOVING   ||
                mode == SelectionInteractionMode.RESIZING);
    }

// Canvas mouse events (forwarded from SelectionTool)

    public void onCanvasPressed(double x, double y) {
        switch (mode) {
            case IDLE -> startDefining(x, y);

            case FLOATING -> {
                Rectangle2D b = selection.getFloatBounds();
                if (b.contains(x, y)) {
                    // Press inside float > begin move
                    mode = SelectionInteractionMode.MOVING;
                    moveDragInitialized = false;
                } else {
                    commit();
                    // After commit we are IDLE; start a new selection on the same press
                    startDefining(x, y);
                }
            }

            case MOVING, RESIZING -> {
                // Already in a gesture.
            }

            case DEFINING -> { /* already defining */ }
        }
    }

    public void onCanvasDragged(double x, double y) {
        switch (mode) {
            case DEFINING -> updateMarquee(x, y);
            case MOVING   -> updateMove(x, y);
            case RESIZING -> updateResize(x, y);
            default       -> { }
        }
    }

    public void onCanvasReleased(double x, double y) {
        switch (mode) {
            case DEFINING -> finishDefining(x, y);
            case MOVING   -> endMove();
            case RESIZING -> onResizeEnded();
            default       -> { }
        }
    }

// Resize handle callbacks

    private void onResizeStarted(ResizeHandle handle) {
        if (mode != SelectionInteractionMode.FLOATING) return;
        mode             = SelectionInteractionMode.RESIZING;
        activeHandle     = handle;
        Rectangle2D b    = selection.getFloatBounds();
        resizeAnchorX    = handle.anchorX(b);
        resizeAnchorY    = handle.anchorY(b);
        resizeOrigW      = b.getWidth();
        resizeOrigH      = b.getHeight();
        resizeMouseStartX = handle.handleX(b);
        resizeMouseStartY = handle.handleY(b);
        cachedCanvasW    = canvasManager.getCanvas().getWidth();
        cachedCanvasH    = canvasManager.getCanvas().getHeight();
    }

    private void onResizeEnded() {
        if (mode == SelectionInteractionMode.RESIZING) {
            mode = SelectionInteractionMode.FLOATING;
        }
    }

// Defining the selection area

    private void startDefining(double x, double y) {
        defineStartX = x;
        defineStartY = y;
        mode = SelectionInteractionMode.DEFINING;
        overlay.showMarquee(x, y, 0, 0);
    }

    private void updateMarquee(double x, double y) {
        double[] r = normalizeRect(defineStartX, defineStartY, x, y);
        overlay.showMarquee(r[0], r[1], r[2], r[3]);
    }

    private void finishDefining(double x, double y) {
        double[] r = normalizeRect(defineStartX, defineStartY, x, y);

        if (r[2] < MIN_SELECTION_SIZE || r[3] < MIN_SELECTION_SIZE) {
            overlay.hide();
            mode = SelectionInteractionMode.IDLE;
            return;
        }

        double cw = canvasManager.getCanvas().getWidth();
        double ch = canvasManager.getCanvas().getHeight();
        double rx = clamp(r[0], 0, cw);
        double ry = clamp(r[1], 0, ch);
        double rw = clamp(r[2], 0, cw - rx);
        double rh = clamp(r[3], 0, ch - ry);

        if (rw < MIN_SELECTION_SIZE || rh < MIN_SELECTION_SIZE) {
            overlay.hide();
            mode = SelectionInteractionMode.IDLE;
            return;
        }

        pickup(rx, ry, rw, rh);
    }

// Lift pixels from canvas to floating layer

    private void pickup(double x, double y, double w, double h) {
        canvasManager.saveStateForUndo();

        // The canvas node has a Scale transform applied by the viewport.
        // snapshot() would capture scaled output, making the region appear smaller and offset.
        // We invert the canvas scale in SnapshotParameters so the snapshot reads true canvas pixels.
        double s = viewportScale.getAsDouble();
        SnapshotParameters params = new SnapshotParameters();
        // Transparent fill so untouched (un-painted) regions stay transparent in the float image.
        params.setFill(Color.TRANSPARENT);
        params.setTransform(new Scale(1.0 / s, 1.0 / s));
        params.setViewport(new Rectangle2D(x, y, w, h));
        int iw = Math.max(1, (int) Math.round(w));
        int ih = Math.max(1, (int) Math.round(h));
        WritableImage floatImg = canvasManager.getCanvas().snapshot(params, new WritableImage(iw, ih));

        // Erase the source area on the drawing layer.
        GraphicsContext gc = canvasManager.getGraphicsContext();
        gc.clearRect(x, y, w, h);

        Rectangle2D bounds = new Rectangle2D(x, y, w, h);
        selection = new Selection(bounds, floatImg);
        cachedCanvasW = canvasManager.getCanvas().getWidth();
        cachedCanvasH = canvasManager.getCanvas().getHeight();
        mode = SelectionInteractionMode.FLOATING;
        overlay.showFloat(selection);
    }

// Moving the selection

    private void updateMove(double x, double y) {
        if (!moveDragInitialized) {
            movePressX = x;
            movePressY = y;
            moveOriginX = selection.getFloatBounds().getMinX();
            moveOriginY = selection.getFloatBounds().getMinY();
            moveDragInitialized = true;
        }

        double dx = x - movePressX;
        double dy = y - movePressY;
        Rectangle2D old = selection.getFloatBounds();
        double newX = clamp(moveOriginX + dx, 0, cachedCanvasW - old.getWidth());
        double newY = clamp(moveOriginY + dy, 0, cachedCanvasH - old.getHeight());

        selection = selection.withFloatBounds(new Rectangle2D(newX, newY, old.getWidth(), old.getHeight()));
        overlay.updateFloatBounds(selection.getFloatBounds());
    }

    private void endMove() {
        if (mode == SelectionInteractionMode.MOVING) {
            moveDragInitialized = false;
            mode = SelectionInteractionMode.FLOATING;
        }
    }

// Resizing the selection

    private void updateResize(double x, double y) {
        if (activeHandle == null || selection == null) return;

        double dx = x - resizeMouseStartX;
        double dy = y - resizeMouseStartY;
        double aspect = resizeOrigW / resizeOrigH;

        double newW, newH;
        if (Math.abs(dx) >= Math.abs(dy)) {
            newW = Math.max(1, resizeOrigW + dx * activeHandle.xSign());
            newH = newW / aspect;
        } else {
            newH = Math.max(1, resizeOrigH + dy * activeHandle.ySign());
            newW = newH * aspect;
        }

        double newMinX, newMinY;
        switch (activeHandle) {
            case SE -> { newMinX = resizeAnchorX;        newMinY = resizeAnchorY; }
            case SW -> { newMinX = resizeAnchorX - newW; newMinY = resizeAnchorY; }
            case NE -> { newMinX = resizeAnchorX;        newMinY = resizeAnchorY - newH; }
            case NW -> { newMinX = resizeAnchorX - newW; newMinY = resizeAnchorY - newH; }
            default -> { newMinX = resizeAnchorX;        newMinY = resizeAnchorY; }
        }

        // Clamp to canvas bounds
        newMinX = clamp(newMinX, 0, cachedCanvasW);
        newMinY = clamp(newMinY, 0, cachedCanvasH);
        newW    = clamp(newW, 1, cachedCanvasW - newMinX);
        newH    = clamp(newH, 1, cachedCanvasH - newMinY);

        selection = selection.withFloatBounds(new Rectangle2D(newMinX, newMinY, newW, newH));
        overlay.updateFloatBounds(selection.getFloatBounds());
    }

// Commit / cancel / delete operations

    //Stamps the float onto the canvas at its current position.
    public void commit() {
        if (selection == null) { reset(); return; }
        Rectangle2D b = selection.getFloatBounds();
        canvasManager.getGraphicsContext()
                     .drawImage(selection.getFloatImage(),
                                b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        reset();
    }

    // Puts original pixels back exactly where they were lifted, then resets.
    public void cancel() {
        if (selection == null) { reset(); return; }
        Rectangle2D ob = selection.getOriginalBounds();
        canvasManager.getGraphicsContext()
                     .drawImage(selection.getFloatImage(),
                                ob.getMinX(), ob.getMinY(),
                                ob.getWidth(), ob.getHeight());
        reset();
    }

    // Discards the float (canvas already has the white hole from pickup).
    public void delete() {
        if (selection == null) { reset(); return; }
        reset();
    }

// Clipboard operations

    public void copy() {
        if (selection != null) clipboard.copy(selection.getFloatImage());
    }

    public void cut() {
        if (selection == null) return;
        copy();
        delete();
    }

    // Pastes from clipboard as a new floating selection centered on the canvas.
    public void paste() {
        if (!clipboard.hasImage()) return;
        if (hasFloat()) commit();

        WritableImage img = clipboard.paste();
        if (img == null) return;

        double cw = canvasManager.getCanvas().getWidth();
        double ch = canvasManager.getCanvas().getHeight();
        double pw = img.getWidth(), ph = img.getHeight();
        double px = clamp((cw - pw) / 2.0, 0, cw - pw);
        double py = clamp((ch - ph) / 2.0, 0, ch - ph);

        Rectangle2D bounds = new Rectangle2D(px, py, pw, ph);
        selection = new Selection(bounds, img);
        cachedCanvasW = cw;
        cachedCanvasH = ch;
        mode = SelectionInteractionMode.FLOATING;
        overlay.showFloat(selection);
    }

// Keyboard nudge movement

    public void nudge(double dx, double dy) {
        if (!hasFloat()) return;
        Rectangle2D b = selection.getFloatBounds();
        double newX = clamp(b.getMinX() + dx, 0, cachedCanvasW - b.getWidth());
        double newY = clamp(b.getMinY() + dy, 0, cachedCanvasH - b.getHeight());
        selection = selection.withFloatBounds(new Rectangle2D(newX, newY, b.getWidth(), b.getHeight()));
        overlay.updateFloatBounds(selection.getFloatBounds());
    }

// Reset state

    public void reset() {
        selection           = null;
        mode                = SelectionInteractionMode.IDLE;
        moveDragInitialized = false;
        activeHandle        = null;
        overlay.hide();
    }

// Utilities

    private static double[] normalizeRect(double x1, double y1, double x2, double y2) {
        return new double[]{
            Math.min(x1, x2), Math.min(y1, y2),
            Math.abs(x2 - x1), Math.abs(y2 - y1)
        };
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
