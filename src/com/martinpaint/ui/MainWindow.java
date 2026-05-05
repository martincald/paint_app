package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import com.martinpaint.canvas.CanvasManager;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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

    private void buildUI() {
        CanvasViewport viewport = new CanvasViewport(controller.getCanvasManager());
        SidePanel sidePanel = new SidePanel(controller);
        StackPane.setAlignment(viewport, Pos.TOP_LEFT);
        StackPane.setAlignment(sidePanel, Pos.TOP_LEFT);
        root.getChildren().addAll(viewport, sidePanel);
    }

    public void show() {
        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.web("#3A3A3A"));
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        CanvasManager canvasManager = controller.getCanvasManager();

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                canvasManager::undo
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                canvasManager::redo
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
                canvasManager::redo
        );

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
