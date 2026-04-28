package com.martinpaint.ui;

import com.martinpaint.app.AppController;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SidePanel extends StackPane {

    private static final double PANEL_WIDTH = 300;
    private static final Duration SLIDE_DURATION = Duration.millis(300);

    private final ScrollPane scrollPane;
    private final Button toggleButton;
    private boolean collapsed;

    public SidePanel(AppController controller) {
        this.collapsed = false;

        //Sub panels
        ToolPanel toolPanel = new ToolPanel(controller.getToolManager());
        ToolSettingsContainer toolSettingsContainer = new ToolSettingsContainer(controller.getToolManager());
        ColorHistoryPanel colorHistoryPanel = new ColorHistoryPanel(controller.getColorManager());

        //menu box
        VBox menuBox = new VBox(10,
                toolPanel,
                createDivider(),
                toolSettingsContainer,
                createDivider(),
                colorHistoryPanel
        );
        menuBox.setPadding(new Insets(12));
        menuBox.setStyle("-fx-background-color: #2B2B2B;");

        //Scrollable wrapper
        scrollPane = new ScrollPane(menuBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #2B2B2B; -fx-background-color: #2B2B2B;");
        scrollPane.setPrefWidth(PANEL_WIDTH);
        scrollPane.setMinWidth(PANEL_WIDTH);
        scrollPane.setMaxWidth(PANEL_WIDTH);

        //toggle sidebar button
        toggleButton = new Button("<");
        toggleButton.setStyle(
                "-fx-background-color: #3C3F41; " +
                        "-fx-text-fill: #CCCCCC; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 6 3 6 3; " +
                        "-fx-background-radius: 0 6 6 0;"
        );
        toggleButton.setOnAction(event -> toggle());

        //Layour of menu box
        HBox container = new HBox(scrollPane, toggleButton);
        container.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(scrollPane, Priority.NEVER);

        getChildren().add(container);
    }

    public void toggle() {
        TranslateTransition transition = new TranslateTransition(SLIDE_DURATION, scrollPane);

        if (collapsed) {
            //show
            transition.setToX(0);
            toggleButton.setText("<");
            collapsed = false;
        } else {
            //hide
            transition.setToX(-PANEL_WIDTH);
            toggleButton.setText(">");
            collapsed = true;
        }

        transition.play();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    private Separator createDivider() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #555555;");
        return separator;
    }
}