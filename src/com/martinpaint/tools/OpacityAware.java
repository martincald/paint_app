package com.martinpaint.tools;

// Tools that support a opacity setting implement this
public interface OpacityAware {

    // Returns the current opacity as a value between 0.0 and 1.0.
    double getOpacity();

    // Sets the opacity. Values are clamped to [0.01, 1.0].
    void setOpacity(double opacity);
}
