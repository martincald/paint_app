package com.martinpaint.selection;

import com.martinpaint.canvas.Layer;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;

// Selection model.
final class Selection {

    private final Rectangle2D   originalBounds;
    private final Rectangle2D   floatBounds;
    private final WritableImage floatImage;
    private final SelectionOrigin origin;
    private final Layer sourceLayer;

    Selection(Rectangle2D originalBounds, WritableImage floatImage, SelectionOrigin origin) {
        this(originalBounds, originalBounds, floatImage, origin, null);
    }

    Selection(Rectangle2D originalBounds, WritableImage floatImage,
              SelectionOrigin origin, Layer sourceLayer) {
        this(originalBounds, originalBounds, floatImage, origin, sourceLayer);
    }

    private Selection(Rectangle2D originalBounds, Rectangle2D floatBounds,
                      WritableImage floatImage, SelectionOrigin origin, Layer sourceLayer) {
        this.originalBounds = originalBounds;
        this.floatBounds    = floatBounds;
        this.floatImage     = floatImage;
        this.origin         = origin;
        this.sourceLayer    = sourceLayer;
    }

    public Rectangle2D getOriginalBounds() { return originalBounds; }

    public Rectangle2D getFloatBounds()    { return floatBounds; }

    public WritableImage getFloatImage()   { return floatImage; }

    public SelectionOrigin getOrigin()     { return origin; }

    public Layer getSourceLayer()          { return sourceLayer; }

    public Selection withFloatBounds(Rectangle2D bounds) {
        return new Selection(originalBounds, bounds, floatImage, origin, sourceLayer);
    }
}
