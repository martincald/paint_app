package com.martinpaint.tools;

import com.martinpaint.io.ImageLoader;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

//Eraser tool

public class EraserTool extends SizedTool {

    public EraserTool() { super(20.0); }

    @Override
    protected Color strokeColor() {
        return Color.WHITE;
    }

    @Override
    public String getName() { return "Eraser"; }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/eraser.png");
    }
}
