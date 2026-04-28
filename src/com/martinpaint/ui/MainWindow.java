package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.io.FileManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainWindow {

    private final AppController controller;
    private final Stage stage;
    private final BorderPane root;

    public MainWindow(AppController controller) {
        this.controller = controller;
        this.stage = controller.getStage();
        this.root = new BorderPane();

        buildUI();
    }

    private void buildUI() {
        CanvasManager canvasManager = controller.getCanvasManager();
        FileManager fileManager = controller.getFileManager();

        // Scrollable, zoomable canvas viewport in the center
        CanvasViewport viewport = new CanvasViewport(canvasManager);
        root.setCenter(viewport);

        //Panel on the left
        SidePanel sidePanel = new SidePanel(controller);
        root.setLeft(sidePanel);

        //Import / export buttons
        Button exportButton = new Button("Export PNG");
        exportButton.setOnAction(event -> fileManager.exportPNG(stage, canvasManager));

        Button importButton = new Button("Import PNG");
        importButton.setOnAction(event -> fileManager.importPNG(stage, canvasManager));

        HBox buttonBar = new HBox(10, importButton, exportButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(8));
        root.setTop(buttonBar);
    }

    public void show() {
        Scene scene = new Scene(root, 1400, 900);
        stage.setTitle("PaintApp");
        stage.setScene(scene);
        stage.show();
    }
}