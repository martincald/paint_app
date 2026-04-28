package com.martinpaint.tools;

import com.martinpaint.io.ImageLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class EraserTool extends Tool {

    private double size = 20.0;

    private double lastX;
    private double lastY;

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        lastX = x;
        lastY = y;
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(this.size);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        gc.beginPath();
        gc.moveTo(lastX, lastY);
        gc.lineTo(x, y);
        gc.stroke();

        lastX = x;
        lastY = y;
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {
        //Placeholder for future eraser functionality
    }

    @Override
    public String getName() {
        return "Eraser";
    }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/eraser.png");
    }

    @Override
    public Node getSettingsPanel() {
        Label sizeLabel = new Label("Size: " + (int) this.size + "px");
        sizeLabel.setStyle("-fx-text-fill: #AAAAAA;");

        Slider sizeSlider = new Slider(1, 50, this.size);
        sizeSlider.setStyle(
                "-fx-control-inner-background: #3C3F41;" +
                "-fx-accent: #5294E2;"
        );
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            this.size = newVal.doubleValue();
            sizeLabel.setText("Size: " + (int) this.size + "px");
        });

        VBox panel = new VBox(8, sizeLabel, sizeSlider);
        panel.setPadding(new Insets(4));
        return panel;
    }
}