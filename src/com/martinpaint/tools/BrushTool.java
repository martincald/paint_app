package com.martinpaint.tools;

import javafx.scene.paint.Color;

/** Standard paint brush */
public class BrushTool extends SizedTool implements OpacityAware {

    private static final double DEFAULT_OPACITY = 1.0;

    private double opacity = DEFAULT_OPACITY;

    public BrushTool() {
        super(ToolSpec.BRUSH);
    }

    // Tell SizedTool to use the preview canvas and replay the stroke at opacity on release.
    @Override
    protected boolean usesOpacityPreview() { return true; }

    // Provides the current opacity to SizedTool when flattening the buffer
    @Override
    protected double currentOpacity() { return getOpacity(); }

    @Override
    public double getOpacity() { return opacity; }

    @Override
    public void setOpacity(double opacity) {
        // Clamp to 1%–100% so the brush is never invisible
        double clamped = Math.clamp(opacity, 0.01, 1.0);
        if (Double.compare(this.opacity, clamped) != 0) {
            this.opacity = clamped;
            markSettingsChanged();
        }
    }

    @Override
    public void resetSettings() {
        super.resetSettings();
        setOpacity(DEFAULT_OPACITY);
    }

    @Override
    protected Color strokeColor() {
        return colorManager.getCurrentColor();
    }
}
