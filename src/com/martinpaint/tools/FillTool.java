package com.martinpaint.tools;

import com.martinpaint.io.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;

public class FillTool extends Tool {

    private static final int TOLERANCE = 5;

    @Override
    public void onMousePressed(double x, double y, GraphicsContext gc) {
        int startX = (int) x;
        int startY = (int) y;

        int width = (int) gc.getCanvas().getWidth();
        int height = (int) gc.getCanvas().getHeight();

        if (startX < 0 || startX >= width || startY < 0 || startY >= height) {
            return;
        }

        WritableImage snapshot = gc.getCanvas().snapshot(null, null);
        PixelReader reader = snapshot.getPixelReader();
        PixelWriter writer = snapshot.getPixelWriter();

        Color targetColor = reader.getColor(startX, startY);
        Color fillColor = colorManager.getCurrentColor();

        if (colorsMatch(targetColor, fillColor)) {
            return;
        }

        boolean[][] visited = new boolean[width][height];
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] pixel = stack.pop();
            int px = pixel[0];
            int py = pixel[1];

            if (px < 0 || px >= width || py < 0 || py >= height) {
                continue;
            }
            if (visited[px][py]) {
                continue;
            }
            visited[px][py] = true;

            Color currentColor = reader.getColor(px, py);
            if (!colorsMatch(currentColor, targetColor)) {
                continue;
            }

            writer.setColor(px, py, fillColor);

            stack.push(new int[]{px + 1, py});
            stack.push(new int[]{px - 1, py});
            stack.push(new int[]{px, py + 1});
            stack.push(new int[]{px, py - 1});
        }

        gc.drawImage(snapshot, 0, 0);
    }

    @Override
    public void onMouseDragged(double x, double y, GraphicsContext gc) {
        //Placeholder for future fill functionality
    }

    @Override
    public void onMouseReleased(double x, double y, GraphicsContext gc) {
        //Placeholder for future fill functionality
    }

    @Override
    public String getName() {
        return "Fill";
    }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/bucket.png");
    }

    private boolean colorsMatch(Color a, Color b) {
        return Math.abs(to255(a.getRed()) - to255(b.getRed())) <= TOLERANCE
                && Math.abs(to255(a.getGreen()) - to255(b.getGreen())) <= TOLERANCE
                && Math.abs(to255(a.getBlue()) - to255(b.getBlue())) <= TOLERANCE
                && Math.abs(to255(a.getOpacity()) - to255(b.getOpacity())) <= TOLERANCE;
    }

    private int to255(double channel) {
        return (int) Math.round(channel * 255);
    }
}