package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.io.FileManager;
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
        // Use the macOS system menu bar; node is invisible there so don't block mouse events
        setUseSystemMenuBar(true);
        setPickOnBounds(false);

        FileManager   fileManager   = controller.getFileManager();
        CanvasManager canvasManager = controller.getCanvasManager();

        Menu fileMenu = new Menu("File");

        MenuItem newCanvas = new MenuItem("New Canvas");
        newCanvas.setOnAction(e -> {
            canvasManager.saveStateForUndo();
            canvasManager.clear();
        });

        MenuItem importItem = new MenuItem("Import PNG…");
        importItem.setOnAction(e -> fileManager.importPNG(stage, canvasManager));

        MenuItem exportItem = new MenuItem("Export PNG…");
        exportItem.setOnAction(e -> fileManager.exportPNG(stage, canvasManager));

        SeparatorMenuItem sep = new SeparatorMenuItem();

        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        undoItem.setOnAction(e -> canvasManager.undo());

        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN));
        redoItem.setOnAction(e -> canvasManager.redo());

        fileMenu.getItems().addAll(newCanvas, sep, importItem, exportItem,
                new SeparatorMenuItem(), undoItem, redoItem);

        getMenus().add(fileMenu);
    }
}
