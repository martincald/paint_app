package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.selection.ClipboardService;
import com.martinpaint.selection.SelectionController;
import com.martinpaint.selection.SelectionOverlay;
import com.martinpaint.tools.SelectionTool;
import com.martinpaint.tools.ToolManager;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.Taskbar;
import java.io.InputStream;

// Main window of the application.

public class MainWindow {

    private final AppController controller;
    private final Stage stage;
    private final StackPane root;

    public MainWindow(AppController controller) {
        this.controller = controller;
        this.stage = controller.getStage();
        this.root = new StackPane();

        buildUI();
    }

    private CanvasViewport viewport;
    private SelectionController selectionController;

    private void buildUI() {
        viewport = new CanvasViewport(controller.getCanvasManager());

        // Wire the selection overlay into the viewport's canvas group.
        ToolManager toolManager = controller.getToolManager();
        SelectionTool selectionTool = toolManager.getSelectionTool();

        SelectionOverlay overlay = new SelectionOverlay();
        viewport.addCanvasOverlay(overlay);

        selectionController = new SelectionController(
                controller.getCanvasManager(), overlay, new ClipboardService(),
                viewport::getScaleValue);
        selectionTool.setController(selectionController);

        // Route keyboard events from the viewport to the selection controller.
        viewport.setSelectionKeyHandler(this::handleSelectionKey);

        SidePanel sidePanel = new SidePanel(controller);
        StackPane.setAlignment(viewport, Pos.TOP_LEFT);
        StackPane.setAlignment(sidePanel, Pos.TOP_LEFT);
        root.getChildren().addAll(viewport, sidePanel);
    }

    // Handles keyboard shortcuts for selection operations.
    // Active when the selection tool has a floating selection.
    private void handleSelectionKey(KeyEvent event) {
        if (selectionController == null) return;
        if (event.getEventType() != KeyEvent.KEY_PRESSED) return;

        boolean hasFloat  = selectionController.hasFloat();
        boolean shortcut  = event.isShortcutDown();
        boolean shift     = event.isShiftDown();
        KeyCode code      = event.getCode();

        if (shortcut) {
            switch (code) {
                case C -> { if (hasFloat) { selectionController.copy();  event.consume(); } }
                case X -> { if (hasFloat) { selectionController.cut();   event.consume(); } }
                case V -> { selectionController.paste(); event.consume(); }
                default -> { }
            }
            return;
        }

        if (!hasFloat) return;

        double nudge = shift ? 10.0 : 1.0;
        switch (code) {
            case ESCAPE -> { selectionController.cancel(); event.consume(); }
            case ENTER  -> { selectionController.commit(); event.consume(); }
            case DELETE, BACK_SPACE -> { selectionController.delete(); event.consume(); }
            case LEFT   -> { selectionController.nudge(-nudge, 0);    event.consume(); }
            case RIGHT  -> { selectionController.nudge( nudge, 0);    event.consume(); }
            case UP     -> { selectionController.nudge(0, -nudge);    event.consume(); }
            case DOWN   -> { selectionController.nudge(0,  nudge);    event.consume(); }
            default -> { }
        }
    }

    public void show() {
        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.web("#3A3A3A"));
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        CanvasManager canvasManager = controller.getCanvasManager();

        stage.setTitle("Paint App");
        loadAppIcon();

        AppMenuBar menuBar = new AppMenuBar(controller, stage);
        root.getChildren().add(menuBar);
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        stage.setScene(scene);
        stage.show();
    }

    private void loadAppIcon() {
        InputStream iconStream = getClass().getResourceAsStream("/resources/images/app_icon.png");
        if (iconStream == null) {
            System.err.println("[Warning] app_icon.png not found in classpath resources.");
            return;
        }
        Image appIcon = new Image(iconStream);
        stage.getIcons().add(appIcon);
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                taskbar.setIconImage(SwingFXUtils.fromFXImage(appIcon, null));
            }
        }
    }
}
