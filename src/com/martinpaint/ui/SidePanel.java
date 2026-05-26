package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// Right-side panel column: Color picker, Swatches, and Properties panels.
public class SidePanel extends VBox {

    public SidePanel(AppController controller) {
        getStyleClass().add("panel-column");

        ColorPickerPanel   colorPickerPanel = new ColorPickerPanel(controller.getColorManager());
        ColorHistoryPanel  swatchesPanel    = new ColorHistoryPanel(controller.getColorManager());
        ToolSettingsContainer settingsPanel = new ToolSettingsContainer(controller.getToolManager());

        Region filler = new Region();
        filler.getStyleClass().add("panel-column");
        VBox.setVgrow(filler, Priority.ALWAYS);

        VBox content = new VBox(colorPickerPanel, swatchesPanel, settingsPanel, filler);
        content.getStyleClass().add("panel-column");

        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("side-panel-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setMaxHeight(Double.MAX_VALUE);

        getChildren().add(scroll);
        setMaxHeight(Double.MAX_VALUE);
    }
}
