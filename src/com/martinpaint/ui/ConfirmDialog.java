package com.martinpaint.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Custom dark-themed modal confirmation dialog, styled entirely via styles.css
 * (classes prefixed {@code confirm-dialog-}). Replaces the stock JavaFX
 * {@code Alert} chrome with something that feels native to this app.
 *
 * Usage is synchronous: {@link #show} blocks (showAndWait-style) and returns
 * {@code true} only if the user explicitly clicks the confirm button.
 */
public final class ConfirmDialog {

    private static final Duration ENTRY_DURATION = Duration.millis(200);
    private static final Interpolator ENTRY_EASE = Interpolator.SPLINE(0.23, 1, 0.32, 1);

    private ConfirmDialog() {}

    /**
     * Shows a modal confirmation dialog and blocks until the user responds.
     *
     * @param owner       owner window (e.g. node.getScene().getWindow()) — used for
     *                    modality and centering over the parent
     * @param message     body copy, e.g. "Clear this layer? This cannot be undone."
     * @param confirmText label for the confirm button, e.g. "Clear Layer" / "Clear All"
     * @param destructive true → heightened destructive styling (more saturated red,
     *                    warning glyph, heavier weight) so the action feels scarier
     *                    at a glance than the non-destructive variant
     * @return true if the user confirmed, false if cancelled or dismissed
     *         (Cancel button, Escape, or clicking outside the card)
     */
    public static boolean show(Window owner, String message, String confirmText, boolean destructive) {
        boolean[] confirmed = { false };

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        if (owner != null) stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        // ── Header: warning glyph + title ─────────────────────────────────────
        Region icon = new Region();
        icon.getStyleClass().add("confirm-dialog-icon");
        if (destructive) icon.getStyleClass().add("confirm-dialog-icon-destructive");

        Label title = new Label(destructive ? "This is permanent" : "Are you sure?");
        title.getStyleClass().add("confirm-dialog-title");

        HBox titleRow = new HBox(10, icon, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // ── Message ───────────────────────────────────────────────────────────
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("confirm-dialog-message");
        messageLabel.setMaxWidth(300);

        // ── Actions ───────────────────────────────────────────────────────────
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("confirm-dialog-cancel-btn");

        Button confirmBtn = new Button(confirmText);
        confirmBtn.getStyleClass().add(destructive
                ? "confirm-dialog-confirm-btn-destructive"
                : "confirm-dialog-confirm-btn");

        HBox actions = new HBox(cancelBtn, confirmBtn);
        actions.getStyleClass().add("confirm-dialog-actions");

        VBox card = new VBox(14, titleRow, messageLabel, actions);
        card.getStyleClass().add("confirm-dialog");
        card.setMaxWidth(360);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("confirm-dialog-root");
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        var css = ConfirmDialog.class.getResource("styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);

        // ── Wiring ────────────────────────────────────────────────────────────
        Runnable confirm = () -> { confirmed[0] = true; stage.close(); };
        Runnable cancel  = () -> { confirmed[0] = false; stage.close(); };

        confirmBtn.setOnAction(_ -> confirm.run());
        cancelBtn.setOnAction(_ -> cancel.run());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) cancel.run();
            else if (e.getCode() == KeyCode.ENTER) confirm.run();
        });

        // Clicking the dimmed area outside the card dismisses — never confirms.
        root.setOnMouseClicked(e -> {
            if (e.getTarget() == root) cancel.run();
        });

        // ── Entry animation: scale(0.95) + opacity 0 → 1, ease-out, ~200ms ────
        card.setOpacity(0.0);
        card.setScaleX(0.95);
        card.setScaleY(0.95);

        stage.setOnShown(_ -> {
            playEntryAnimation(card);
            confirmBtn.requestFocus();
        });

        stage.showAndWait();
        return confirmed[0];
    }

    private static void playEntryAnimation(VBox card) {
        FadeTransition fade = new FadeTransition(ENTRY_DURATION, card);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(ENTRY_DURATION, card);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition entry = new ParallelTransition(fade, scale);
        entry.setInterpolator(ENTRY_EASE);
        entry.play();
    }
}
