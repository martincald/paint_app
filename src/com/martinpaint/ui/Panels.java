package com.martinpaint.ui;

import com.martinpaint.color.ColorUtils;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Small reusable UI helpers shared by panel-style components
 * (panel header, action button, fixed-size region).
 */
public final class Panels {

    private Panels() {}

    /** Header row with a left-aligned title and a right-aligned actions HBox. */
    public static HBox panelHeader(String title) {
        Label lbl = new Label(title);
        lbl.getStyleClass().add("panel-title");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        HBox actions = new HBox(2);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(lbl, actions);
        header.getStyleClass().add("panel-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /** Add the given node to the right-aligned actions area of a header built by {@link #panelHeader}. */
    public static void addHeaderAction(HBox header, Node action) {
        ((HBox) header.getChildren().get(header.getChildren().size() - 1))
                .getChildren().add(action);
    }

    /** Small icon-style button for panel headers. */
    public static Button headerBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("panel-action-btn");
        return btn;
    }

    public static Label actionLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setCursor(Cursor.HAND);
        return label;
    }

    public static Region colorChip(String styleClass, double size) {
        Region chip = new Region();
        chip.getStyleClass().add(styleClass);
        chip.setCursor(Cursor.HAND);
        fixSize(chip, size, size);
        return chip;
    }

    /** Pin the region's preferred, minimum, and maximum size in one call. */
    public static void fixSize(Region r, double w, double h) {
        r.setPrefSize(w, h);
        r.setMinSize(w, h);
        r.setMaxSize(w, h);
    }

    public static void setColorFill(Region r, Color color) {
        r.setStyle("-fx-background-color: " + ColorUtils.toWebHex(color) + ";");
    }

    public static void setStyleClassActive(Node node, String styleClass, boolean active) {
        boolean present = node.getStyleClass().contains(styleClass);
        if (active && !present) {
            node.getStyleClass().add(styleClass);
        } else if (!active && present) {
            node.getStyleClass().remove(styleClass);
        }
    }
}
