package com.martinpaint.io;

import com.martinpaint.canvas.CanvasManager;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/** Handles PNG import and export via a JavaFX file chooser. */
public class FileManager {

    public void exportPNG(Stage stage, CanvasManager canvasManager) {
        File file = chooseFile(stage, "Export PNG", "painting.png", true);
        if (file == null) return;

        try {
            WritableImage snapshot = canvasManager.snapshotUnscaled();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(bufferedImage, "png", file);
        } catch (Exception e) {
            System.err.println("Failed to export PNG: " + e.getMessage());
        }
    }

    public void importPNG(Stage stage, CanvasManager canvasManager) {
        File file = chooseFile(stage, "Import PNG", null, false);
        if (file == null) return;

        try {
            Image image = new Image(file.toURI().toString());
            if (image.isError()) {
                throw new IllegalArgumentException("Unable to load image.");
            }
            Canvas canvas = canvasManager.getActiveLayerCanvas();
            GraphicsContext gc = canvasManager.getGraphicsContext();
            canvasManager.saveStateForUndo();
            gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
            canvasManager.markDrawingChanged();
        } catch (Exception e) {
            System.err.println("Failed to import PNG: " + e.getMessage());
        }
    }

    private File chooseFile(Stage stage, String title, String defaultName, boolean save) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        if (defaultName != null) {
            chooser.setInitialFileName(defaultName);
        }
        return save ? chooser.showSaveDialog(stage) : chooser.showOpenDialog(stage);
    }
}
