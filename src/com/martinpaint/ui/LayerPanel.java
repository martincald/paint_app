package com.martinpaint.ui;

import com.martinpaint.app.HapticFeedback;
import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.canvas.Layer;
import com.martinpaint.canvas.LayerManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Photoshop-style layers panel.
 *
 * Display order: topmost layer first (reversed from LayerManager's bottom-to-top ordering).
 * LayerManager index 0 = bottom, N-1 = top.
 * Display index  0 = top of UI, N-1 = bottom of UI.
 */
public class LayerPanel extends VBox {

    private static final int    THUMB_SIZE  = 30;

    private final CanvasManager          canvasManager;
    private final LayerManager           layerManager;
    private final ObservableList<Layer>  displayList = FXCollections.observableArrayList();
    private final ListView<Layer>        listView;
    private final PauseTransition        thumbDebounce;
    private Layer draggedLayer;
    /** The cell currently showing the insertion indicator, so we can clear it on exit. */
    private LayerCell dropIndicatorCell = null;
    /** Layers repositioned in the last drop — these cells play an entry animation. */
    private final Set<Layer> animateCells = new HashSet<>();

    public LayerPanel(CanvasManager canvasManager) {
        this.canvasManager = canvasManager;
        this.layerManager  = canvasManager.getLayerManager();
        getStyleClass().add("panel-box");

        // ── Header ───────────────────────────────────────────────────────────
        HBox header = Panels.panelHeader("LAYERS");

        Button moveUpBtn   = Panels.headerBtn("▲");  // ▲
        Button moveDownBtn = Panels.headerBtn("▼");  // ▼
        Button addBtn      = Panels.headerBtn("+");
        Button deleteBtn   = Panels.headerBtn("−");  // −

        moveUpBtn  .setTooltip(new Tooltip("Move layer up"));
        moveDownBtn.setTooltip(new Tooltip("Move layer down"));
        addBtn     .setTooltip(new Tooltip("New layer"));
        deleteBtn  .setTooltip(new Tooltip("Delete layer"));

        Panels.addHeaderAction(header, moveUpBtn);
        Panels.addHeaderAction(header, moveDownBtn);
        Panels.addHeaderAction(header, addBtn);
        Panels.addHeaderAction(header, deleteBtn);

        deleteBtn.disableProperty().bind(
                javafx.beans.binding.Bindings.size(layerManager.getLayers()).lessThanOrEqualTo(1));

        // ── Layer list ────────────────────────────────────────────────────────
        listView = new ListView<>(displayList);
        listView.getStyleClass().add("layer-list");
        listView.setCellFactory(_ -> new LayerCell());
        listView.setFocusTraversable(false);

        // ── Button actions ────────────────────────────────────────────────────
        addBtn.setOnAction(_ -> canvasManager.runUndoable(() -> layerManager.addLayer()));

        deleteBtn.setOnAction(_ -> {
            if (layerManager.getLayers().size() <= 1) return;
            canvasManager.runUndoable(() -> layerManager.deleteLayer(layerManager.getActiveLayerIndex()));
        });

        moveUpBtn.setOnAction(_ ->
                canvasManager.runUndoableChange(() -> layerManager.moveLayerUp(layerManager.getActiveLayerIndex())));

        moveDownBtn.setOnAction(_ ->
                canvasManager.runUndoableChange(() -> layerManager.moveLayerDown(layerManager.getActiveLayerIndex())));

        // ── Sync display list ─────────────────────────────────────────────────
        rebuildDisplayList();
        layerManager.getLayers().addListener((ListChangeListener<Layer>) _ -> {
            rebuildDisplayList();
            listView.refresh();
        });
        layerManager.activeLayerIndexProperty().addListener((_, _, _) -> listView.refresh());

        // ── Thumbnail refresh (debounced 150 ms after stroke commit) ──────────
        thumbDebounce = new PauseTransition(Duration.millis(150));
        thumbDebounce.setOnFinished(_ -> listView.refresh());
        canvasManager.drawingStampProperty().addListener((_, _, _) -> thumbDebounce.playFromStart());

        // ── Footer (destructive, infrequent actions) ─────────────────────────
        Button clearLayerBtn = ClearAction.LAYER.button(this::window, canvasManager);
        Button clearAllBtn = ClearAction.ALL.button(this::window, canvasManager);

        HBox footer = new HBox(clearLayerBtn, clearAllBtn);
        footer.getStyleClass().add("layer-panel-footer");

        getChildren().addAll(header, listView, footer);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void rebuildDisplayList() {
        displayList.setAll(layerManager.getLayers().reversed());
    }

    /** Map display-list position (0 = top) → LayerManager index (0 = bottom). */
    private int toLayerIndex(int displayIndex) {
        return layerManager.getLayers().size() - 1 - displayIndex;
    }

    private void clearDropIndicator() {
        if (dropIndicatorCell != null) {
            dropIndicatorCell.hideInsertIndicator();
            dropIndicatorCell = null;
        }
    }

    private boolean dropDraggedLayerAboveOrBelow(Layer target, boolean insertAbove) {
        if (draggedLayer == null || target == null || target == draggedLayer) return false;

        int srcDisplay = displayList.indexOf(draggedLayer);
        int tgtDisplay = displayList.indexOf(target);
        if (srcDisplay < 0 || tgtDisplay < 0) return false;

        List<Layer> before = List.copyOf(displayList);

        int effectiveDisplayTarget = insertAbove ? tgtDisplay : Math.min(tgtDisplay + 1, displayList.size() - 1);
        int srcLayerIdx = toLayerIndex(srcDisplay);
        int tgtLayerIdx = toLayerIndex(effectiveDisplayTarget);

        if (canvasManager.runUndoableChange(() -> layerManager.moveLayerAbove(srcLayerIdx, tgtLayerIdx))) {
            // Populate animateCells with layers that changed display position
            List<Layer> after = layerManager.getLayers().reversed();
            for (int i = 0; i < before.size(); i++) {
                if (i < after.size() && before.get(i) != after.get(i)) {
                    animateCells.add(after.get(i));
                }
            }
            return true;
        }
        return false;
    }

    // ── Custom cell ───────────────────────────────────────────────────────────

    private class LayerCell extends ListCell<Layer> {

        private final HBox         row;
        private final ToggleButton eyeBtn;
        private final ImageView    thumb;
        private final StackPane    thumbContainer;
        private final Label        nameLabel;
        private final TextField    nameField;
        private final Label        opacityLabel;
        private final Slider       opacitySlider;

        private final SnapshotParameters thumbParams;

        private final StackPane    cellPane;
        private final Region       insertIndicator;

        /** Last drop direction seen by this cell; read if the drop event arrives quickly. */
        private boolean cellDropAbove = true;

        /** Guards slider valueProperty listener from firing during model→UI sync. */
        private boolean updatingFromModel = false;

        private final ChangeListener<Boolean> eyeListener = (_, _, selected) -> {
            if (getItem() != null) getItem().setVisible(selected);
        };

        LayerCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setPadding(Insets.EMPTY);

            // Eye toggle — ● visible, ○ hidden
            eyeBtn = new ToggleButton("●");
            eyeBtn.getStyleClass().add("layer-eye-btn");
            eyeBtn.setSelected(true);
            Panels.fixSize(eyeBtn, 20, 20);

            // Thumbnail
            thumb = new ImageView();
            thumb.setFitWidth(THUMB_SIZE);
            thumb.setFitHeight(THUMB_SIZE);
            thumb.setPreserveRatio(false);
            thumbContainer = new StackPane(thumb);
            thumbContainer.getStyleClass().add("layer-thumb");
            Panels.fixSize(thumbContainer, THUMB_SIZE + 2, THUMB_SIZE + 2);

            thumbParams = new SnapshotParameters();
            thumbParams.setFill(Color.TRANSPARENT);
            thumbParams.setTransform(new Scale(THUMB_SIZE / CanvasManager.CANVAS_SIZE, THUMB_SIZE / CanvasManager.CANVAS_SIZE));

            // Name label
            nameLabel = new Label();
            nameLabel.getStyleClass().add("layer-name");
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            nameLabel.setEllipsisString("…");
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            // In-place rename field (hidden by default)
            nameField = new TextField();
            nameField.getStyleClass().add("layer-name-field");
            nameField.setMaxWidth(Double.MAX_VALUE);
            nameField.setVisible(false);
            nameField.setManaged(false);
            HBox.setHgrow(nameField, Priority.ALWAYS);

            // Opacity label (shows percentage)
            opacityLabel = new Label("100%");
            opacityLabel.getStyleClass().add("layer-opacity-label");
            Panels.fixSize(opacityLabel, 38, 16);

            // Opacity slider (hidden until label is clicked)
            opacitySlider = new Slider(0, 100, 100);
            opacitySlider.getStyleClass().add("prop-slider");
            opacitySlider.setVisible(false);
            opacitySlider.setManaged(false);
            Panels.fixSize(opacitySlider, 60, 20);

            // ── Row ───────────────────────────────────────────────────────────
            row = new HBox(6, eyeBtn, thumbContainer, nameLabel, nameField, opacityLabel, opacitySlider);
            row.getStyleClass().add("layer-row");
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 8, 2, 8));
            row.setMinHeight(40);
            row.setPrefHeight(40);
            row.setMaxHeight(40);

            // ── Insert indicator + cell pane ──────────────────────────────────
            insertIndicator = new Region();
            insertIndicator.getStyleClass().add("layer-insert-indicator");
            insertIndicator.setVisible(false);
            insertIndicator.setMouseTransparent(true);
            insertIndicator.setManaged(false);
            insertIndicator.prefWidthProperty().bind(row.widthProperty());

            cellPane = new StackPane(row, insertIndicator);
            cellPane.setAlignment(Pos.TOP_LEFT);

            // ── Interactions ──────────────────────────────────────────────────

            // Click row → set active layer
            row.setOnMouseClicked(e -> {
                if (getItem() == null) return;
                int displayIdx = displayList.indexOf(getItem());
                if (displayIdx >= 0) layerManager.setActiveLayerIndex(toLayerIndex(displayIdx));
            });

            row.setOnDragDetected(e -> {
                if (getItem() == null || nameField.isVisible() || opacitySlider.isVisible()) return;

                draggedLayer = getItem();
                Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);

                ClipboardContent content = new ClipboardContent();
                content.putString(draggedLayer.getName());
                dragboard.setContent(content);

                // Custom ghost — snapshot the actual row (not thumbnail-sized thumbParams)
                SnapshotParameters ghostParams = new SnapshotParameters();
                ghostParams.setFill(Color.TRANSPARENT);
                WritableImage ghost = row.snapshot(ghostParams, null);
                double offsetX = Math.clamp(e.getX(), 0, ghost.getWidth());
                double offsetY = Math.clamp(e.getY(), 0, ghost.getHeight());
                dragboard.setDragView(ghost, offsetX, offsetY);

                e.consume();
            });

            row.setOnDragOver(e -> {
                if (!canDropOn(getItem(), e.getDragboard())) return;
                e.acceptTransferModes(TransferMode.MOVE);

                boolean above = e.getY() < row.getHeight() / 2.0;
                if (cellDropAbove != above || dropIndicatorCell != this) {
                    cellDropAbove = above;   // cell-local copy for onDragDropped
                    clearDropIndicator();
                    dropIndicatorCell = this;
                    showInsertIndicator(above);
                }
                e.consume();
            });

            row.setOnDragEntered(e -> e.consume());

            row.setOnDragExited(e -> {
                if (dropIndicatorCell == this) {
                    clearDropIndicator();
                }
                e.consume();
            });

            row.setOnDragDropped(e -> {
                clearDropIndicator();
                boolean moved = dropDraggedLayerAboveOrBelow(getItem(), cellDropAbove);
                e.setDropCompleted(moved);
                e.consume();
            });

            row.setOnDragDone(_ -> draggedLayer = null);

            // Double-click name → inline rename
            nameLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && getItem() != null) startRename();
            });
            nameField.setOnAction(_ -> commitRename());
            nameField.focusedProperty().addListener((_, _, focused) -> {
                if (!focused) commitRename();
            });

            // Click opacity label → show slider
            opacityLabel.setOnMouseClicked(_ -> {
                if (getItem() == null) return;
                opacityLabel.setVisible(false);
                opacityLabel.setManaged(false);
                opacitySlider.setVisible(true);
                opacitySlider.setManaged(true);
                opacitySlider.requestFocus();
            });
            opacitySlider.focusedProperty().addListener((_, _, focused) -> {
                if (!focused) hideOpacitySlider();
            });
            opacitySlider.valueProperty().addListener((_, oldVal, val) -> {
                if (!updatingFromModel && getItem() != null) {
                    getItem().setOpacity(val.doubleValue() / 100.0);
                    opacityLabel.setText(String.format("%.0f%%", val.doubleValue()));
                    if (oldVal.intValue() != val.intValue()) HapticFeedback.sliderTick();
                }
            });

            setGraphic(cellPane);
        }

        @Override
        protected void updateItem(Layer layer, boolean empty) {
            super.updateItem(layer, empty);
            if (empty || layer == null) {
                thumb.setImage(null);
                nameLabel.setText("");
                nameField.clear();
                hideInsertIndicator();
                cellPane.setOpacity(1.0);
                setGraphic(null);
                return;
            }
            setGraphic(cellPane);
            hideInsertIndicator();

            updatingFromModel = true;

            // Sync eye button without triggering listener
            eyeBtn.selectedProperty().removeListener(eyeListener);
            eyeBtn.setSelected(layer.isVisible());
            eyeBtn.selectedProperty().addListener(eyeListener);

            // Name
            nameLabel.setText(layer.getName());

            // Opacity
            double pct = layer.getOpacity() * 100.0;
            opacitySlider.setValue(pct);
            opacityLabel.setText(String.format("%.0f%%", pct));
            hideOpacitySlider();

            updatingFromModel = false;

            // Thumbnail
            refreshThumbnail(layer);

            // Active layer highlight
            boolean isActive = layerManager.getActiveLayer() == layer;
            Panels.setStyleClassActive(row, "layer-row-active", isActive);

            if (animateCells.remove(layer)) {
                playEntryAnimation();
            }
        }

        void showInsertIndicator(boolean above) {
            insertIndicator.setTranslateY(above ? 0 : row.getPrefHeight() - 2);
            insertIndicator.setVisible(true);
        }

        void hideInsertIndicator() {
            insertIndicator.setVisible(false);
        }

        private void playEntryAnimation() {
            cellPane.setOpacity(0.0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), cellPane);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.setOnFinished(_ -> cellPane.setOpacity(1.0));
            ft.play();
        }

        private void refreshThumbnail(Layer layer) {
            WritableImage mini = new WritableImage(THUMB_SIZE, THUMB_SIZE);
            layer.snapshot(thumbParams, mini);
            thumb.setImage(mini);
        }

        private void startRename() {
            nameLabel.setVisible(false);
            nameLabel.setManaged(false);
            nameField.setText(getItem().getName());
            nameField.setVisible(true);
            nameField.setManaged(true);
            nameField.requestFocus();
            nameField.selectAll();
        }

        private void commitRename() {
            if (getItem() == null || !nameField.isVisible()) return;
            String n = nameField.getText().trim();
            if (!n.isEmpty()) getItem().setName(n);
            nameLabel.setText(getItem().getName());
            nameField.setVisible(false);
            nameField.setManaged(false);
            nameLabel.setVisible(true);
            nameLabel.setManaged(true);
        }

        private void hideOpacitySlider() {
            opacitySlider.setVisible(false);
            opacitySlider.setManaged(false);
            opacityLabel.setVisible(true);
            opacityLabel.setManaged(true);
        }
    }

    private boolean canDropOn(Layer target, Dragboard dragboard) {
        return draggedLayer != null
                && target != null
                && target != draggedLayer
                && dragboard.hasString();
    }

    private Window window() {
        return getScene() == null ? null : getScene().getWindow();
    }
}
