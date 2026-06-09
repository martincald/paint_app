package com.martinpaint.canvas;

import javafx.beans.property.*;
import javafx.collections.*;

import java.util.ArrayList;
import java.util.List;

public class LayerManager {

    private final ObservableList<Layer> layers = FXCollections.observableArrayList();
    private final IntegerProperty activeLayerIndex = new SimpleIntegerProperty(0);

    public LayerManager() {
        layers.add(new Layer("Layer 1"));
    }

    public ObservableList<Layer> getLayers() {
        return layers;
    }

    public IntegerProperty activeLayerIndexProperty() {
        return activeLayerIndex;
    }

    public int getActiveLayerIndex() {
        return activeLayerIndex.get();
    }

    public void setActiveLayerIndex(int index) {
        if (layers.isEmpty()) return;
        activeLayerIndex.set(Math.clamp(index, 0, layers.size() - 1));
    }

    public Layer getActiveLayer() {
        return layers.get(activeLayerIndex.get());
    }

    /** Adds a new blank layer above the current active layer and makes it active. */
    public Layer addLayer() {
        int insertAt = Math.min(activeLayerIndex.get() + 1, layers.size());
        String name  = "Layer " + (layers.size() + 1);
        Layer layer  = new Layer(name);
        layers.add(insertAt, layer);
        activeLayerIndex.set(insertAt);
        return layer;
    }

    /**
     * Deletes the layer at the given index.
     * No-op if only one layer remains (minimum 1 layer invariant).
     */
    public void deleteLayer(int index) {
        if (layers.size() <= 1) return;
        if (!isValidIndex(index)) return;

        List<Layer> nextLayers = new ArrayList<>(layers);
        Layer active = getActiveLayer();
        nextLayers.remove(index);

        int nextActiveIndex = nextLayers.indexOf(active);
        activeLayerIndex.set(nextActiveIndex >= 0
                ? nextActiveIndex
                : Math.clamp(index, 0, nextLayers.size() - 1));
        layers.setAll(nextLayers);
    }

    /** Moves the layer at index one step up (toward top/front). */
    public boolean moveLayerUp(int index) {
        if (index >= layers.size() - 1) return false;
        return moveLayerTo(index, index + 1);
    }

    /** Moves the layer at index one step down (toward bottom/back). */
    public boolean moveLayerDown(int index) {
        if (index <= 0) return false;
        return moveLayerTo(index, index - 1);
    }

    /** Moves source so it sits directly above target in the visual stack. */
    public boolean moveLayerAbove(int sourceIndex, int targetIndex) {
        if (!isValidIndex(sourceIndex) || !isValidIndex(targetIndex)) return false;
        if (sourceIndex == targetIndex) return false;

        int insertAt = sourceIndex < targetIndex ? targetIndex : targetIndex + 1;
        return moveLayerTo(sourceIndex, insertAt);
    }

    private boolean moveLayerTo(int sourceIndex, int insertIndex) {
        if (!isValidIndex(sourceIndex)) return false;

        List<Layer> reordered = new ArrayList<>(layers);
        Layer active = getActiveLayer();
        Layer layer = reordered.remove(sourceIndex);
        reordered.add(Math.clamp(insertIndex, 0, reordered.size()), layer);

        if (reordered.equals(layers)) return false;
        layers.setAll(reordered);
        activeLayerIndex.set(reordered.indexOf(active));
        return true;
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < layers.size();
    }

    /**
     * Replaces the entire layer stack with a single new blank layer named
     * "Layer 1" and makes it active. Used to reset the project (e.g. "Clear All").
     */
    public void resetToSingleBlankLayer() {
        replaceLayers(List.of(new Layer("Layer 1")), 0);
    }

    /**
     * Rebuilds the layer list entirely from a list of LayerState records.
     * Used exclusively by CanvasManager.undo/redo to restore a full snapshot.
     * Package-private: callers outside canvas package should not call this directly.
     */
    void restoreFromSnapshot(List<LayerState> layerStates, int newActiveIndex) {
        List<Layer> restored = new ArrayList<>(Math.max(1, layerStates.size()));
        for (LayerState state : layerStates) {
            Layer layer = new Layer(state.name());
            layer.setVisible(state.visible());
            layer.setOpacity(state.opacity());
            layer.getGc().drawImage(state.image(), 0, 0);
            restored.add(layer);
        }
        if (restored.isEmpty()) restored.add(new Layer("Layer 1"));

        replaceLayers(restored, newActiveIndex);
    }

    private void replaceLayers(List<Layer> replacement, int newActiveIndex) {
        List<Layer> nextLayers = replacement.isEmpty()
                ? List.of(new Layer("Layer 1"))
                : replacement;
        int nextActiveIndex = Math.clamp(newActiveIndex, 0, nextLayers.size() - 1);
        int parkedIndex = Math.min(nextActiveIndex, Math.max(0, layers.size() - 1));

        activeLayerIndex.set(parkedIndex);
        layers.setAll(nextLayers);
        activeLayerIndex.set(nextActiveIndex);
    }
}
