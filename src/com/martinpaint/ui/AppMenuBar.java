package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.io.FileManager;
import com.martinpaint.selection.SelectionController;
import com.martinpaint.tools.SelectionTool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class AppMenuBar extends MenuBar {

    public AppMenuBar(AppController controller, Stage stage) {
        this(controller, stage, null);
    }

    public AppMenuBar(AppController controller, Stage stage, CanvasViewport viewport) {
        // Use the macOS system menu bar
        setUseSystemMenuBar(true);
        setPickOnBounds(false);

        CanvasManager canvasManager = controller.getCanvasManager();
        FileManager   fileManager   = controller.getFileManager();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newCanvas = item("Clear all", null, _ -> {
            canvasManager.saveStateForUndo();
            canvasManager.clear();
        });
        MenuItem importItem = item("Import PNG…", null, _ -> fileManager.importPNG(stage, canvasManager));
        MenuItem exportItem = item("Export PNG…", null, _ -> fileManager.exportPNG(stage, canvasManager));

        MenuItem undoItem = item("Undo", new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), _ -> canvasManager.undo());
        MenuItem redoItem = item("Redo", new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN), _ -> canvasManager.redo());

        fileMenu.getItems().addAll(newCanvas, new SeparatorMenuItem(),
                importItem, exportItem,
                new SeparatorMenuItem(), undoItem, redoItem);

        // Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem cutItem   = item("Cut",   new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN), _ -> selectionAction(controller, SelectionController::cut));
        MenuItem copyItem  = item("Copy",  new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN), _ -> selectionAction(controller, SelectionController::copy));
        MenuItem pasteItem = item("Paste", new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN), _ -> selectionAction(controller, SelectionController::paste));
        MenuItem deleteItem = item("Delete", new KeyCodeCombination(KeyCode.DELETE), _ -> selectionAction(controller, c -> { if (c.hasFloat()) c.delete(); }));

        editMenu.getItems().addAll(cutItem, copyItem, pasteItem, new SeparatorMenuItem(), deleteItem);

        // Disable cut/copy/delete when there is no floating selection.
        editMenu.setOnShowing(_ -> {
            SelectionController sc = getSelectionController(controller);
            boolean active = sc != null && sc.hasFloat();
            cutItem.setDisable(!active);
            copyItem.setDisable(!active);
            deleteItem.setDisable(!active);
            pasteItem.setDisable(sc == null);
        });

        // View menu — wired to the canvas viewport when available
        Menu viewMenu = new Menu("View");
        if (viewport != null) {
            MenuItem zoomIn  = item("Zoom In",  new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN), _ -> viewport.zoomIn());
            MenuItem zoomOut = item("Zoom Out", new KeyCodeCombination(KeyCode.MINUS,  KeyCombination.SHORTCUT_DOWN), _ -> viewport.zoomOut());
            MenuItem zoom100 = item("Actual Size", new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN), _ -> viewport.zoomTo(1.0));
            viewMenu.getItems().addAll(zoomIn, zoomOut, new SeparatorMenuItem(), zoom100);
        } else {
            viewMenu.setDisable(true);
        }

        // Select menu
        Menu selectMenu = new Menu("Select");
        MenuItem deselectItem = item("Deselect", null,
                _ -> selectionAction(controller, c -> { if (c.hasFloat()) c.cancel(); }));
        selectMenu.setOnShowing(_ -> {
            SelectionController sc = getSelectionController(controller);
            deselectItem.setDisable(sc == null || !sc.hasFloat());
        });
        selectMenu.getItems().add(deselectItem);

        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = item("About Paint App", null, _ -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About Paint App");
            alert.setHeaderText("Paint App  v1.3");
            alert.setContentText("A dark-theme paint application built with JavaFX.");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);

        getMenus().addAll(fileMenu, editMenu, viewMenu, selectMenu, helpMenu);
    }

    private static MenuItem item(String text, KeyCombination accel, EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
        if (accel != null) item.setAccelerator(accel);
        item.setOnAction(action);
        return item;
    }

    // Helpers

    private static SelectionController getSelectionController(AppController controller) {
        SelectionTool st = controller.getToolManager().getSelectionTool();
        return st.getController();
    }

    @FunctionalInterface
    private interface SelectionAction {
        void run(SelectionController controller);
    }

    private static void selectionAction(AppController controller, SelectionAction action) {
        SelectionController sc = getSelectionController(controller);
        if (sc != null) action.run(sc);
    }
}
