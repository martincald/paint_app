package com.martinpaint.ui;

import com.martinpaint.tools.Tool;
import com.martinpaint.tools.ToolManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Shows the active tools settings.
public class ToolSettingsContainer extends VBox {

    private static final String PLACEHOLDER_TEXT =
            "Things like the size slider, opacity, etc.. will be here, "
                    + "any settings related to the tool chosen will be here.";

    private final StackPane contentArea;
    private final Label     placeholder;

    public ToolSettingsContainer(ToolManager toolManager) {
        setSpacing(10);
        setPadding(new Insets(4, 8, 4, 8));
        setAlignment(Pos.TOP_CENTER);
        setFillWidth(true);

        Label title = new Label("Tool settings");
        title.getStyleClass().add("section-title");

        placeholder = new Label(PLACEHOLDER_TEXT);
        placeholder.getStyleClass().add("placeholder-text");
        placeholder.setWrapText(true);
        placeholder.setMaxWidth(240);
        placeholder.setAlignment(Pos.CENTER);

        contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_CENTER);

        getChildren().addAll(title, contentArea);

        showSettingsFor(toolManager.getActiveTool());
        toolManager.activeToolProperty()
                .addListener((_, _, newT) -> showSettingsFor(newT));
    }

    private void showSettingsFor(Tool tool) {
        contentArea.getChildren().clear();
        Node panel = tool != null ? ToolSettingsView.create(tool) : null;
        contentArea.getChildren().add(panel != null ? panel : placeholder);
    }
}
