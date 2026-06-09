package com.martinpaint.selection;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.canvas.Layer;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Orchestrates the full selection lifecycle: IDLE > DEFINING > FLOATING > (MOVING / RESIZING) > commit / cancel / delete. */
public class SelectionController {

    private static final double MIN_SELECTION_SIZE = 2.0;

    private final CanvasManager    canvasManager;
    private final SelectionOverlay overlay;
    private final ClipboardService clipboard = new ClipboardService();

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

    public SelectionController(CanvasManager canvasManager, SelectionOverlay overlay) {
        this.canvasManager  = canvasManager;
        this.overlay        = overlay;
    }

    public void attachOverlayInteraction() {
        overlay.wireInteraction(
                this::onResizeStarted,
                this::onCanvasDragged,
                this::onResizeEnded
        );
    }

    // ── Public state queries ──────────────────────────────────────────────────

    public boolean hasFloat() {
        return selection != null &&
               (mode == SelectionInteractionMode.FLOATING ||
                mode == SelectionInteractionMode.MOVING   ||
                mode == SelectionInteractionMode.RESIZING);
    }

    // ── Canvas mouse events (forwarded from SelectionTool) ───────────────────

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

    // ── Resize handle callbacks ───────────────────────────────────────────────

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

    // ── Defining the selection area ───────────────────────────────────────────

    private void startDefining(double x, double y) {
        defineStartX = x;
        defineStartY = y;
        mode = SelectionInteractionMode.DEFINING;
        overlay.showMarquee(x, y, 0, 0);
    }

    private void updateMarquee(double x, double y) {
        overlay.showMarquee(
                Math.min(defineStartX, x),
                Math.min(defineStartY, y),
                Math.abs(x - defineStartX),
                Math.abs(y - defineStartY));
    }

    private void finishDefining(double x, double y) {
        double minX = Math.min(defineStartX, x);
        double minY = Math.min(defineStartY, y);
        double width = Math.abs(x - defineStartX);
        double height = Math.abs(y - defineStartY);

        if (width < MIN_SELECTION_SIZE || height < MIN_SELECTION_SIZE) {
            overlay.hide();
            mode = SelectionInteractionMode.IDLE;
            return;
        }

        double cw = canvasManager.getCanvas().getWidth();
        double ch = canvasManager.getCanvas().getHeight();
        double rx = Math.clamp(minX, 0, cw);
        double ry = Math.clamp(minY, 0, ch);
        double rw = Math.clamp(width, 0, cw - rx);
        double rh = Math.clamp(height, 0, ch - ry);

        if (rw < MIN_SELECTION_SIZE || rh < MIN_SELECTION_SIZE) {
            overlay.hide();
            mode = SelectionInteractionMode.IDLE;
            return;
        }

        pickup(rx, ry, rw, rh);
    }

    // ── Lift pixels from canvas to floating layer ─────────────────────────────

    private void pickup(double x, double y, double w, double h) {
        canvasManager.saveStateForUndo();

        SnapshotParameters params = new SnapshotParameters();
        // Transparent fill so untouched (un-painted) regions stay transparent in the float image.
        params.setFill(Color.TRANSPARENT);
        params.setViewport(new Rectangle2D(x, y, w, h));
        int iw = Math.max(1, (int) Math.round(w));
        int ih = Math.max(1, (int) Math.round(h));
        Layer sourceLayer = canvasManager.getLayerManager().getActiveLayer();
        WritableImage floatImg = sourceLayer.snapshot(params, new WritableImage(iw, ih));

        // Erase the source area on the drawing layer.
        GraphicsContext gc = sourceLayer.getGc();
        gc.clearRect(x, y, w, h);
        canvasManager.markDrawingChanged();

        Rectangle2D bounds = new Rectangle2D(x, y, w, h);
        selection = new Selection(bounds, floatImg, SelectionOrigin.LIFTED, sourceLayer);
        cachedCanvasW = canvasManager.getCanvas().getWidth();
        cachedCanvasH = canvasManager.getCanvas().getHeight();
        mode = SelectionInteractionMode.FLOATING;
        overlay.showFloat(selection);
    }

    // ── Moving the selection ──────────────────────────────────────────────────

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
        double newX = clampPosition(moveOriginX + dx, old.getWidth(), cachedCanvasW);
        double newY = clampPosition(moveOriginY + dy, old.getHeight(), cachedCanvasH);

        selection = selection.withFloatBounds(new Rectangle2D(newX, newY, old.getWidth(), old.getHeight()));
        overlay.updateFloatBounds(selection.getFloatBounds());
    }

    private void endMove() {
        if (mode == SelectionInteractionMode.MOVING) {
            moveDragInitialized = false;
            mode = SelectionInteractionMode.FLOATING;
        }
    }

    // ── Resizing the selection ────────────────────────────────────────────────

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
        newMinX = Math.clamp(newMinX, 0, cachedCanvasW);
        newMinY = Math.clamp(newMinY, 0, cachedCanvasH);
        newW    = Math.clamp(newW, 1, cachedCanvasW - newMinX);
        newH    = Math.clamp(newH, 1, cachedCanvasH - newMinY);

        selection = selection.withFloatBounds(new Rectangle2D(newMinX, newMinY, newW, newH));
        overlay.updateFloatBounds(selection.getFloatBounds());
    }

    // ── Commit / cancel / delete operations ──────────────────────────────────

    //Stamps the float onto the canvas at its current position.
    public void commit() {
        if (selection == null) { reset(); return; }
        if (selection.getOrigin() == SelectionOrigin.PASTED) {
            canvasManager.saveStateForUndo();
        }
        Rectangle2D b = selection.getFloatBounds();
        selectionGraphicsContext()
                .drawImage(selection.getFloatImage(),
                        b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        canvasManager.markDrawingChanged();
        reset();
    }

    // Puts original pixels back exactly where they were lifted, then resets.
    public void cancel() {
        if (selection == null) { reset(); return; }
        if (selection.getOrigin() == SelectionOrigin.LIFTED) {
            Rectangle2D ob = selection.getOriginalBounds();
            selectionGraphicsContext()
                    .drawImage(selection.getFloatImage(),
                            ob.getMinX(), ob.getMinY(),
                            ob.getWidth(), ob.getHeight());
            canvasManager.discardLastUndoState();
        }
        reset();
    }

    // Discards the float (canvas already has the white hole from pickup).
    public void delete() {
        if (selection == null) { reset(); return; }
        reset();
    }

    // ── Clipboard operations ──────────────────────────────────────────────────

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
        double px = clampPosition((cw - pw) / 2.0, pw, cw);
        double py = clampPosition((ch - ph) / 2.0, ph, ch);

        Rectangle2D bounds = new Rectangle2D(px, py, pw, ph);
        selection = new Selection(bounds, img, SelectionOrigin.PASTED);
        cachedCanvasW = cw;
        cachedCanvasH = ch;
        mode = SelectionInteractionMode.FLOATING;
        overlay.showFloat(selection);
    }

    // ── Keyboard nudge movement ───────────────────────────────────────────────

    public void nudge(double dx, double dy) {
        if (!hasFloat()) return;
        Rectangle2D b = selection.getFloatBounds();
        double newX = clampPosition(b.getMinX() + dx, b.getWidth(), cachedCanvasW);
        double newY = clampPosition(b.getMinY() + dy, b.getHeight(), cachedCanvasH);
        selection = selection.withFloatBounds(new Rectangle2D(newX, newY, b.getWidth(), b.getHeight()));
        overlay.updateFloatBounds(selection.getFloatBounds());
    }

    // ── Reset state ───────────────────────────────────────────────────────────

    public void reset() {
        selection           = null;
        mode                = SelectionInteractionMode.IDLE;
        moveDragInitialized = false;
        activeHandle        = null;
        overlay.hide();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private double clampPosition(double value, double size, double limit) {
        return Math.clamp(value, 0, Math.max(0, limit - size));
    }

    private GraphicsContext selectionGraphicsContext() {
        Layer sourceLayer = selection.getSourceLayer();
        return sourceLayer == null ? canvasManager.getGraphicsContext() : sourceLayer.getGc();
    }
}
