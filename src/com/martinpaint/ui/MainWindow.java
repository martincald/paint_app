package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import com.martinpaint.color.ColorManager;
import com.martinpaint.selection.SelectionController;
import com.martinpaint.selection.SelectionOverlay;
import com.martinpaint.tools.SelectionTool;
import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import com.martinpaint.tools.ToolSpec;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.InputStream;

/** Main window of the application. */
public class MainWindow {

    private final AppController controller;
    private final Stage stage;
    private final BorderPane root;

    private CanvasViewport viewport;
    private SelectionController selectionController;

    public MainWindow(AppController controller) {
        this.controller = controller;
        this.stage = controller.getStage();
        this.root = new BorderPane();
        buildUI();
    }

    private void buildUI() {
        viewport = new CanvasViewport(controller.getCanvasManager());

        ToolManager toolManager = controller.getToolManager();
        ColorManager colorManager = controller.getColorManager();
        SelectionTool selectionTool = toolManager.getSelectionTool();

        SelectionOverlay overlay = new SelectionOverlay();
        viewport.addCanvasOverlay(overlay);

        selectionController = new SelectionController(controller.getCanvasManager(), overlay);
        selectionController.attachOverlayInteraction();
        selectionTool.setController(selectionController);
        viewport.setSelectionKeyHandler(this::handleSelectionKey);

        // Wire Hand/Zoom tools to viewport navigation mode.
        toolManager.activeToolProperty().addListener((_, _, tool) -> {
            viewport.setNavMode(toViewportNavMode(tool));
        });

        ToolPanel      toolRail     = new ToolPanel(toolManager, colorManager);
        SidePanel      rightPanels  = new SidePanel(controller);
        StatusBar      statusBar    = new StatusBar(controller, viewport);
        CanvasTabBar   tabBar       = new CanvasTabBar(viewport);

        VBox canvasArea = new VBox(tabBar, viewport);
        VBox.setVgrow(viewport, Priority.ALWAYS);

        root.setLeft(toolRail);
        root.setCenter(canvasArea);
        root.setRight(rightPanels);
        root.setBottom(statusBar);
    }

    private void handleSelectionKey(KeyEvent event) {
        if (selectionController == null) return;
        if (event.getEventType() != KeyEvent.KEY_PRESSED) return;

        boolean hasFloat = selectionController.hasFloat();
        boolean shortcut = event.isShortcutDown();
        boolean shift    = event.isShiftDown();
        KeyCode code     = event.getCode();

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
            case LEFT   -> { selectionController.nudge(-nudge, 0);  event.consume(); }
            case RIGHT  -> { selectionController.nudge( nudge, 0);  event.consume(); }
            case UP     -> { selectionController.nudge(0, -nudge);  event.consume(); }
            case DOWN   -> { selectionController.nudge(0,  nudge);  event.consume(); }
            default -> { }
        }
    }

    public void show() {
        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.web("#1c1c1c"));
        var css = getClass().getResource("styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle("Paint App — Untitled-1");
        loadAppIcon();

        AppMenuBar menuBar    = new AppMenuBar(controller, stage, viewport);
        OptionsBar optionsBar = new OptionsBar(controller.getToolManager());
        VBox topArea = new VBox(menuBar, optionsBar);
        root.setTop(topArea);

        // Global tool keyboard shortcuts — guard against TextField focus.
        ToolManager toolManager = controller.getToolManager();
        ColorManager colorManager = controller.getColorManager();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Don't fire shortcuts when typing in text inputs.
            if (event.getTarget() instanceof TextField) return;
            if (event.isShortcutDown() || event.isAltDown()) return;

            KeyCode code = event.getCode();

            // Tool activation
            ToolSpec toolSpec = ToolSpec.fromShortcut(code);
            if (toolSpec != null) {
                toolManager.setActiveTool(toolSpec);
                event.consume();
                return;
            }

            // Color swap / reset
            if (code == KeyCode.X) { colorManager.swapColors(); event.consume(); }
            else if (code == KeyCode.D) { colorManager.resetColors(); event.consume(); }
        });

        stage.setScene(scene);
        stage.show();
    }

    private CanvasViewport.NavMode toViewportNavMode(Tool tool) {
        if (tool == null) return CanvasViewport.NavMode.NONE;
        return switch (tool.getSpec().navigationMode()) {
            case PAN -> CanvasViewport.NavMode.PAN;
            case ZOOM -> CanvasViewport.NavMode.ZOOM;
            case NONE -> CanvasViewport.NavMode.NONE;
        };
    }

    private void loadAppIcon() {
        try (InputStream iconStream = getClass().getResourceAsStream("/resources/images/app_icon.png")) {
            if (iconStream == null) {
                System.err.println("[Warning] app_icon.png not found in classpath resources.");
                return;
            }
            Image appIcon = new Image(iconStream);
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("[Warning] failed to load app icon: " + e.getMessage());
        }
    }
}
