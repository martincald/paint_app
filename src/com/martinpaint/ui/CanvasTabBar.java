package com.martinpaint.ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

// Single-tab document strip above the canvas, showing name and live zoom %.
public class CanvasTabBar extends HBox {

    public CanvasTabBar(CanvasViewport viewport) {
        getStyleClass().add("canvas-tab-bar");
        setAlignment(Pos.CENTER_LEFT);

        Label docName = new Label("Untitled-1");
        docName.getStyleClass().add("canvas-tab-name");

        Label zoomMeta = new Label();
        zoomMeta.getStyleClass().add("canvas-tab-meta");
        zoomMeta.textProperty().bind(
            Bindings.createStringBinding(
                () -> String.format("@ %.0f%%", viewport.zoomProperty().get() * 100),
                viewport.zoomProperty()
            )
        );

        HBox tab = new HBox(6, docName, zoomMeta);
        tab.getStyleClass().add("canvas-tab-active");
        tab.setAlignment(Pos.CENTER_LEFT);

        Region filler = new Region();
        filler.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(filler, javafx.scene.layout.Priority.ALWAYS);

        getChildren().addAll(tab, filler);
    }
}
