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

public class FileManager {

    public void exportPNG(Stage stage, CanvasManager canvasManager) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export PNG");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );
        fileChooser.setInitialFileName("painting.png");

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            WritableImage snapshot = canvasManager.getSnapshot();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(bufferedImage, "png", file);
        } catch (Exception e) {
            System.err.println("Failed to export PNG: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void importPNG(Stage stage, CanvasManager canvasManager) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import PNG");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            Image image = new Image(file.toURI().toString());
            Canvas canvas = canvasManager.getCanvas();
            GraphicsContext gc = canvasManager.getGraphicsContext();
            gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        } catch (Exception e) {
            System.err.println("Failed to import PNG: " + e.getMessage());
            e.printStackTrace();
        }
    }
}