package com.martinpaint.selection;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;

// Selection model.
public final class Selection {

    private final Rectangle2D   originalBounds;
    private final Rectangle2D   floatBounds;
    private final WritableImage floatImage;

    public Selection(Rectangle2D originalBounds, WritableImage floatImage) {
        this(originalBounds, originalBounds, floatImage);
    }

    private Selection(Rectangle2D originalBounds, Rectangle2D floatBounds, WritableImage floatImage) {
        this.originalBounds = originalBounds;
        this.floatBounds    = floatBounds;
        this.floatImage     = floatImage;
    }

    public Rectangle2D getOriginalBounds() { return originalBounds; }

    public Rectangle2D getFloatBounds()    { return floatBounds; }

    public WritableImage getFloatImage()   { return floatImage; }

    public Selection withFloatBounds(Rectangle2D bounds) {
        return new Selection(originalBounds, bounds, floatImage);
    }
}
