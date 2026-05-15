package com.martinpaint.tools;

import com.martinpaint.io.ImageLoader;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

// Standard paint brush
public class BrushTool extends SizedTool implements OpacityAware {

    private double opacity = 1.0;

    public BrushTool() { super(5.0); }

    // Tell SizedTool to route strokes through the offscreen buffer
    @Override
    protected boolean usesStrokeBuffer() { return true; }

    // Provides the current opacity to SizedTool when flattening the buffer
    @Override
    protected double currentOpacity() { return opacity; }

    @Override
    public double getOpacity() { return opacity; }

    @Override
    public void setOpacity(double opacity) {
        // Clamp to 1%–100% so the brush is never invisible
        this.opacity = Math.max(0.01, Math.min(1.0, opacity));
    }

    @Override
    protected Color strokeColor() {
        return colorManager.getCurrentColor();
    }

    @Override
    public String getName() { return "Brush"; }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/paint-brush.png");
    }
}
