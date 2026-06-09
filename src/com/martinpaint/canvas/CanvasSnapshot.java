package com.martinpaint.canvas;

import java.util.List;

public record CanvasSnapshot(List<LayerState> layers, int activeLayerIndex) {}
