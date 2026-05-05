package com.martinpaint.tools;

import com.martinpaint.io.ImageLoader;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

//Brush tool

public class BrushTool extends SizedTool {

    public BrushTool() { super(5.0); }

    @Override
    protected Color strokeColor() {
        return colorManager.getCurrentColor();
    }

    @Override
    public String getName() { return "Brush"; }

    @Override
    public Image getIcon() {
        return ImageLoader.load("resources/images/paint-brush.png");
    }
}
