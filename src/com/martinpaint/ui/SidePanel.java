package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// Side panel for tools and settings.
public class SidePanel extends StackPane {

    private static final double   PANEL_WIDTH    = 300;
    private static final Duration SLIDE_DURATION = Duration.millis(280);

    private final HBox    container;
    private final Button  toggleButton;
    private boolean       collapsed;

    public SidePanel(AppController controller) {
        getStyleClass().add("side-panel-root");

        ToolPanel             toolPanel  = new ToolPanel(controller.getToolManager());
        ToolSettingsContainer settings   = new ToolSettingsContainer(controller.getToolManager());
        ColorHistoryPanel     colorPanel = new ColorHistoryPanel(controller.getColorManager());

        VBox menuBox = new VBox(
                20,
                toolPanel,
                divider(),
                settings,
                divider(),
                colorPanel
        );
        menuBox.setPadding(new Insets(20, 16, 20, 16));
        menuBox.getStyleClass().add("side-panel-menu");
        menuBox.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(menuBox);
        scrollPane.getStyleClass().add("side-panel-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(PANEL_WIDTH);
        scrollPane.setMinWidth(PANEL_WIDTH);
        scrollPane.setMaxWidth(PANEL_WIDTH);

        toggleButton = new Button("‹");
        toggleButton.getStyleClass().add("panel-toggle");
        toggleButton.setFocusTraversable(false);
        toggleButton.setOnAction(_ -> toggle());

        container = new HBox(scrollPane, toggleButton);
        container.setAlignment(Pos.TOP_LEFT);
        container.setMaxWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(scrollPane, Priority.NEVER);

        StackPane.setAlignment(container, Pos.TOP_LEFT);
        getChildren().add(container);

        // Occupy only the width of visible content.
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Double.MAX_VALUE);
        setMinWidth(0);
        setPickOnBounds(false);
    }

    public void toggle() {
        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, container);
        if (collapsed) {
            slide.setToX(0);
            toggleButton.setText("‹");
            collapsed = false;
        } else {
            slide.setToX(-PANEL_WIDTH);
            toggleButton.setText("›");
            collapsed = true;
        }
        slide.play();
    }

    private static Region divider() {
        Region r = new Region();
        r.getStyleClass().add("divider");
        return r;
    }
}
