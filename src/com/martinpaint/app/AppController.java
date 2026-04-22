package com.martinpaint.app;

import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.color.ColorManager;
import com.martinpaint.io.FileManager;
import com.martinpaint.tools.ToolManager;
import com.martinpaint.ui.MainWindow;
import javafx.stage.Stage;

public class AppController {

    private final Stage stage;

    private ColorManager colorManager;
    private CanvasManager canvasManager;
    private ToolManager toolManager;
    private FileManager fileManager;
    private MainWindow mainWindow;

    public AppController(Stage stage) {
        this.stage = stage;
    }

    public void launch() {
        colorManager = new ColorManager();
        canvasManager = new CanvasManager();
        toolManager = new ToolManager(canvasManager, colorManager);
        fileManager = new FileManager();

        mainWindow = new MainWindow(this);
        mainWindow.show();
    }

    public Stage getStage() {
        return stage;
    }

    public ColorManager getColorManager() {
        return colorManager;
    }

    public CanvasManager getCanvasManager() {
        return canvasManager;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }
}
