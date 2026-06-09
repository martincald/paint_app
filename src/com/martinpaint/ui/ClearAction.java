package com.martinpaint.ui;

import com.martinpaint.canvas.CanvasManager;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

import java.util.function.Supplier;

enum ClearAction {
    LAYER("Clear Layer", "Clear this layer? This cannot be undone.", false, "clear-layer-btn") {
        @Override void apply(CanvasManager canvasManager) { canvasManager.clearActiveLayer(); }
    },
    ALL("Clear All", "Delete all layers and clear the project? This cannot be undone.", true, "clear-all-btn") {
        @Override void apply(CanvasManager canvasManager) { canvasManager.resetProject(); }
    };

    private final String text;
    private final String message;
    private final boolean destructive;
    private final String buttonStyle;

    ClearAction(String text, String message, boolean destructive, String buttonStyle) {
        this.text = text;
        this.message = message;
        this.destructive = destructive;
        this.buttonStyle = buttonStyle;
    }

    MenuItem menuItem(Window owner, CanvasManager canvasManager) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(_ -> run(owner, canvasManager));
        return item;
    }

    Button button(Supplier<Window> owner, CanvasManager canvasManager) {
        Button button = new Button(text);
        button.getStyleClass().add(buttonStyle);
        button.setOnAction(_ -> run(owner.get(), canvasManager));
        return button;
    }

    private void run(Window owner, CanvasManager canvasManager) {
        if (ConfirmDialog.show(owner, message, text, destructive)) {
            canvasManager.runUndoable(() -> apply(canvasManager));
        }
    }

    abstract void apply(CanvasManager canvasManager);
}
