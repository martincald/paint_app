package com.martinpaint.ui;

import com.martinpaint.color.ColorManager;
import com.martinpaint.color.ColorUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Coordinator for the COLOR panel. Owns the HSV state and the active (FG/BG)
 * editing target, and wires the subviews ({@link HsvPickerSquare},
 * {@link HueStrip}, {@link RgbSlidersView}) to a {@link ColorManager}.
 */
public class ColorPickerPanel extends VBox {

    private enum Active { FG, BG }

    private final ColorManager cm;
    private Active editing = Active.FG;
    private boolean syncing;

    // HSV state (always derived from the active color).
    private double h, s, v;

    // Sub-views.
    private final HsvPickerSquare picker  = new HsvPickerSquare();
    private final HueStrip        hue     = new HueStrip();
    private final RgbSlidersView  sliders = new RgbSlidersView();
    private final Region          fgChip  = Panels.colorChip("color-chip-fg", 32);
    private final Region          bgChip  = Panels.colorChip("color-chip-bg", 32);
    private final TextField       hexField = new TextField();

    public ColorPickerPanel(ColorManager cm) {
        this.cm = cm;
        getStyleClass().add("panel-box");
        buildUI();
        wireCallbacks();
        syncFromColor(cm.getCurrentColor());

        cm.currentColorProperty().addListener((_, _, c) -> {
            if (!syncing && editing == Active.FG && c != null) syncFromColor(c);
        });
        cm.backgroundColorProperty().addListener((_, _, c) -> {
            if (!syncing && editing == Active.BG && c != null) syncFromColor(c);
        });
    }

    // ── UI assembly ──────────────────────────────────────────────

    private void buildUI() {
        HBox header = Panels.panelHeader("COLOR");

        Pane chipPane = buildChipPane();

        VBox pickerGroup = new VBox(6, picker, hue);
        HBox.setHgrow(pickerGroup, Priority.ALWAYS);

        HBox topRow = new HBox(10, chipPane, pickerGroup);
        topRow.setPadding(new Insets(10, 10, 8, 10));
        topRow.setAlignment(Pos.TOP_LEFT);

        sliders.setPadding(new Insets(0, 10, 8, 10));

        HBox hexRow = buildHexRow();
        hexRow.setPadding(new Insets(0, 10, 10, 10));

        getChildren().addAll(header, topRow, sliders, hexRow);
    }

    private void wireCallbacks() {
        picker.setOnChange((ns, nv) -> applyHsv(h, ns, nv));
        hue.setOnChange(nh -> applyHsv(nh, s, v));
        sliders.setOnChange(this::applyChannelValue);
    }

    private Pane buildChipPane() {
        configureChip(bgChip, "color-chip-bg", 14, 14, Active.BG);
        configureChip(fgChip, "color-chip-fg",  0,  0, Active.FG);

        Label swapBtn = Panels.actionLabel("⇄", "color-chip-action");
        swapBtn.setLayoutX(30);
        swapBtn.setLayoutY(-2);
        swapBtn.setOnMouseClicked(_ -> {
            cm.swapColors();
            syncFromColor(activeColor());
        });

        Pane resetOverlay = new Pane(
                miniResetChip("white", -2, 36),
                miniResetChip("black",  2, 32));
        resetOverlay.setPrefSize(14, 14);
        resetOverlay.setLayoutX(-3);
        resetOverlay.setLayoutY(30);
        resetOverlay.setCursor(Cursor.HAND);
        resetOverlay.setOnMouseClicked(_ -> {
            cm.resetColors();
            syncFromColor(activeColor());
        });

        Pane chipPane = new Pane(bgChip, fgChip, swapBtn, resetOverlay);
        Panels.fixSize(chipPane, 50, 50);
        return chipPane;
    }

    private void configureChip(Region chip, String styleClass, double x, double y, Active target) {
        chip.setLayoutX(x);
        chip.setLayoutY(y);
        chip.setOnMouseClicked(_ -> {
            editing = target;
            syncFromColor(activeColor());
        });
    }

    private static Region miniResetChip(String color, double x, double y) {
        Region r = Panels.colorChip("mini-reset-chip", 10);
        r.setStyle("-fx-background-color: " + color + ";");
        r.setLayoutX(x);
        r.setLayoutY(y);
        return r;
    }

    private HBox buildHexRow() {
        Label hexLabel = new Label("HEX");
        hexLabel.getStyleClass().add("hex-label");

        hexField.getStyleClass().add("hex");
        HBox.setHgrow(hexField, Priority.ALWAYS);
        hexField.textProperty().addListener((_, _, text) -> {
            if (syncing) return;
            String t = text.startsWith("#") ? text : "#" + text;
            if (t.matches("#[0-9A-Fa-f]{6}")) {
                try { applyColor(Color.web(t)); } catch (Exception ignored) {}
            }
        });

        HBox row = new HBox(8, hexLabel, hexField);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("hex-row");

        Region divider = new Region();
        divider.getStyleClass().add("hex-divider");
        return new HBox(new VBox(divider, row));
    }

    // ── Color model ──────────────────────────────────────────────

    private Color activeColor() {
        return editing == Active.FG ? cm.getCurrentColor() : cm.getBackgroundColor();
    }

    private void setActiveColor(Color c) {
        if (editing == Active.FG) cm.setCurrentColor(c);
        else                       cm.setBackgroundColor(c);
    }

    private void syncFromColor(Color c) {
        if (c == null) return;
        h = c.getHue();
        s = c.getSaturation();
        v = c.getBrightness();
        refreshAllViews();
    }

    private void applyHsv(double nh, double ns, double nv) {
        h = nh; s = ns; v = nv;
        syncing = true;
        setActiveColor(Color.hsb(h, s, v));
        syncing = false;
        refreshAllViews();
    }

    private void applyColor(Color c) {
        h = c.getHue();
        s = c.getSaturation();
        v = c.getBrightness();
        syncing = true;
        setActiveColor(c);
        syncing = false;
        refreshAllViews();
    }

    private void applyChannelValue(int channel, int value) {
        Color current = Color.hsb(h, s, v);
        int r = ColorUtils.to255(current.getRed());
        int g = ColorUtils.to255(current.getGreen());
        int b = ColorUtils.to255(current.getBlue());
        switch (channel) { // 0 = R, 1 = G, 2 = B — matches RgbSlidersView.setOnChange
            case 0 -> r = value;
            case 1 -> g = value;
            case 2 -> b = value;
        }
        Color newColor = Color.rgb(r, g, b);
        h = newColor.getHue();
        s = newColor.getSaturation();
        v = newColor.getBrightness();
        syncing = true;
        setActiveColor(newColor);
        syncing = false;
        // Don't reset the value field text — only refresh other views.
        picker.setHsv(h, s, v);
        hue.setHue(h);
        sliders.refreshThumbsAndGradients(newColor);
        refreshHexField();
        updateChips();
    }

    // ── View refresh ─────────────────────────────────────────────

    private void refreshAllViews() {
        Color c = Color.hsb(h, s, v);
        picker.setHsv(h, s, v);
        hue.setHue(h);
        sliders.setColor(c);
        refreshHexField();
        updateChips();
    }

    private void refreshHexField() {
        syncing = true;
        hexField.setText(ColorUtils.toWebHex(Color.hsb(h, s, v)));
        syncing = false;
    }

    private void updateChips() {
        Color fg = cm.getCurrentColor();
        Color bg = cm.getBackgroundColor();
        if (fg != null) Panels.setColorFill(fgChip, fg);
        if (bg != null) Panels.setColorFill(bgChip, bg);
        Panels.setStyleClassActive(fgChip, "active-color-chip", editing == Active.FG);
        Panels.setStyleClassActive(bgChip, "active-color-chip", editing == Active.BG);
    }
}
