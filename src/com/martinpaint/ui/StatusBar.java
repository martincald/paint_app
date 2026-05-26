package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import com.martinpaint.canvas.CanvasManager;
import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// Bottom status bar: zoom controls, canvas info, active tool, status indicator.
public class StatusBar extends HBox {

    public StatusBar(AppController controller, CanvasViewport viewport) {
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);

        ToolManager toolManager = controller.getToolManager();
        int size = (int) CanvasManager.CANVAS_SIZE;

        // ── Zoom controls ─────────────────────────────────────────
        Button zoomOut = new Button("−");
        zoomOut.getStyleClass().add("zoom-btn");
        zoomOut.setOnAction(_ -> viewport.zoomOut());

        Label zoomLabel = new Label();
        zoomLabel.getStyleClass().add("zoom-label");
        zoomLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> String.format("%.0f%%", viewport.zoomProperty().get() * 100),
                viewport.zoomProperty()
            )
        );

        Button zoomIn = new Button("+");
        zoomIn.getStyleClass().add("zoom-btn");
        zoomIn.setOnAction(_ -> viewport.zoomIn());

        HBox zoomControl = new HBox(zoomOut, zoomLabel, zoomIn);
        zoomControl.getStyleClass().add("zoom-control");
        zoomControl.setAlignment(Pos.CENTER);

        Button zoom100 = new Button("100%");
        zoom100.getStyleClass().add("zoom-btn");
        zoom100.setStyle("-fx-pref-width: 40; -fx-text-fill: #8a8a8a;");
        zoom100.setOnAction(_ -> viewport.zoomTo(1.0));

        // ── Separators and info labels ─────────────────────────────
        Label canvasSize = new Label(size + " × " + size);
        canvasSize.getStyleClass().add("status-label");

        Label colorMode = new Label("RGB / 8");
        colorMode.getStyleClass().add("status-label");

        // ── Spacer ─────────────────────────────────────────────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Right side: tool name + ready indicator ────────────────
        Label toolLabel = new Label("Brush");
        toolLabel.getStyleClass().add("status-label");
        toolManager.activeToolProperty().addListener((_, _, tool) -> {
            toolLabel.setText(tool != null ? tool.getName() : "—");
        });
        Tool active = toolManager.getActiveTool();
        if (active != null) toolLabel.setText(active.getName());

        Label ready = new Label("● Ready");
        ready.getStyleClass().add("status-label-accent");

        getChildren().addAll(
            zoomControl, zoom100,
            sep(), canvasSize,
            sep(), colorMode,
            spacer,
            toolLabel,
            sep(), ready
        );
    }

    private Region sep() {
        Region r = new Region();
        r.getStyleClass().add("status-sep");
        return r;
    }
}
