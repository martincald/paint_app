package com.martinpaint.selection;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;

// Resize handles for corners.
public enum ResizeHandle {

    NW(Cursor.NW_RESIZE, 0, 0, 1, 1),
    NE(Cursor.NE_RESIZE, 1, 0, 0, 1),
    SE(Cursor.SE_RESIZE, 1, 1, 0, 0),
    SW(Cursor.SW_RESIZE, 0, 1, 1, 0);

    private final Cursor cursor;
    private final int hx, hy, ax, ay;

    ResizeHandle(Cursor cursor, int hx, int hy, int ax, int ay) {
        this.cursor = cursor;
        this.hx = hx; this.hy = hy;
        this.ax = ax; this.ay = ay;
    }

    public Cursor getCursor() { return cursor; }

    public double handleX(Rectangle2D r) { return hx == 0 ? r.getMinX() : r.getMaxX(); }
    public double handleY(Rectangle2D r) { return hy == 0 ? r.getMinY() : r.getMaxY(); }
    public double anchorX(Rectangle2D r) { return ax == 0 ? r.getMinX() : r.getMaxX(); }
    public double anchorY(Rectangle2D r) { return ay == 0 ? r.getMinY() : r.getMaxY(); }

    public double xSign() { return hx == 0 ? -1.0 : 1.0; }
    public double ySign() { return hy == 0 ? -1.0 : 1.0; }
}
