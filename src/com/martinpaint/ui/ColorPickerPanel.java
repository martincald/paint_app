package com.martinpaint.ui;

import com.martinpaint.app.HapticFeedback;
import com.martinpaint.color.ColorManager;
import com.martinpaint.color.ColorUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * HSV color picker panel with FG/BG chips, picker square, hue strip,
 * RGB sliders, and hex input — the design's full ColorPanel component.
 */
public class ColorPickerPanel extends VBox {

    private enum Active { FG, BG }

    private final ColorManager cm;
    private Active editing = Active.FG;
    private boolean updating = false;

    // Current HSV (always derived from the active color)
    private double h = 0, s = 0, v = 0;

    // FG/BG chips
    private final Region fgChip = new Region();
    private final Region bgChip = new Region();

    // Picker square
    private final Pane   pickerPane  = new Pane();
    private final Region pickerRing  = new Region();

    // Hue strip
    private final Pane   huePane  = new Pane();
    private final Region hueThumb = new Region();

    // RGB channel sliders [0=R, 1=G, 2=B]
    private final Pane[]      trackPanes = new Pane[3];
    private final Region[]    thumbs     = new Region[3];
    private final TextField[] valFields  = new TextField[3];

    // Hex input
    private final TextField hexField = new TextField();

    // ── Constructor ───────────────────────────────────────────────

    public ColorPickerPanel(ColorManager cm) {
        this.cm = cm;
        getStyleClass().add("panel-box");
        buildUI();
        syncFromColor(cm.getCurrentColor());

        // Track external color changes (e.g. swatch clicks).
        cm.currentColorProperty().addListener((_, _, c) -> {
            if (!updating && editing == Active.FG && c != null) syncFromColor(c);
        });
        cm.backgroundColorProperty().addListener((_, _, c) -> {
            if (!updating && editing == Active.BG && c != null) syncFromColor(c);
        });
    }

    // ── UI construction ───────────────────────────────────────────

    private void buildUI() {
        HBox header = ColorHistoryPanel.panelHeader("COLOR");

        // FG/BG chip pane (64×64 logical, fits 48px rail width concern-free in 280px panel)
        Pane chipPane = buildChipPane();

        // Picker square + hue strip in a VBox (right side of the top row)
        pickerRing.getStyleClass().add("picker-ring");
        pickerRing.setPrefSize(10, 10);
        pickerRing.setMinSize(10, 10);
        pickerRing.setMaxSize(10, 10);
        pickerRing.setMouseTransparent(true);
        pickerPane.getChildren().add(pickerRing);
        pickerPane.getStyleClass().add("picker");
        pickerPane.setPrefHeight(130);
        pickerPane.setMinHeight(100);
        pickerPane.setCursor(Cursor.CROSSHAIR);
        pickerPane.widthProperty().addListener((_, _, _) -> refreshPickerRing());
        pickerPane.heightProperty().addListener((_, _, _) -> refreshPickerRing());
        pickerPane.setOnMousePressed(e -> handlePickerDrag(e.getX(), e.getY()));
        pickerPane.setOnMouseDragged(e -> handlePickerDrag(e.getX(), e.getY()));

        hueThumb.getStyleClass().add("hue-thumb");
        hueThumb.setPrefSize(5, 16);
        hueThumb.setMinSize(5, 16);
        hueThumb.setMaxSize(5, 16);
        hueThumb.setMouseTransparent(true);
        huePane.getChildren().add(hueThumb);
        huePane.getStyleClass().add("hue-strip");
        huePane.setPrefHeight(12);
        huePane.setMinHeight(12);
        huePane.setMaxHeight(12);
        huePane.setCursor(Cursor.H_RESIZE);
        huePane.widthProperty().addListener((_, _, _) -> refreshHueThumb());
        huePane.setOnMousePressed(e -> handleHueDrag(e.getX()));
        huePane.setOnMouseDragged(e -> handleHueDrag(e.getX()));

        VBox pickerGroup = new VBox(6, pickerPane, huePane);
        HBox.setHgrow(pickerGroup, Priority.ALWAYS);

        HBox topRow = new HBox(10, chipPane, pickerGroup);
        topRow.setPadding(new Insets(10, 10, 8, 10));
        topRow.setAlignment(Pos.TOP_LEFT);

        // RGB sliders
        VBox slidersBox = buildSliders();
        slidersBox.setPadding(new Insets(0, 10, 8, 10));

        // Hex row
        HBox hexRow = buildHexRow();
        hexRow.setPadding(new Insets(0, 10, 10, 10));

        getChildren().addAll(header, topRow, slidersBox, hexRow);

        // Initial ring/thumb positioning after layout
        Platform.runLater(this::refreshUI);
    }

    private Pane buildChipPane() {
        // bg chip (bottom-right, behind fg)
        bgChip.getStyleClass().add("color-chip-bg");
        bgChip.setPrefSize(32, 32);
        bgChip.setMinSize(32, 32);
        bgChip.setMaxSize(32, 32);
        bgChip.setLayoutX(14);
        bgChip.setLayoutY(14);
        bgChip.setCursor(Cursor.HAND);
        bgChip.setOnMouseClicked(_ -> {
            editing = Active.BG;
            syncFromColor(cm.getBackgroundColor());
            updateChipBorders();
        });

        // fg chip (top-left, above bg)
        fgChip.getStyleClass().add("color-chip-fg");
        fgChip.setPrefSize(32, 32);
        fgChip.setMinSize(32, 32);
        fgChip.setMaxSize(32, 32);
        fgChip.setLayoutX(0);
        fgChip.setLayoutY(0);
        fgChip.setCursor(Cursor.HAND);
        fgChip.setOnMouseClicked(_ -> {
            editing = Active.FG;
            syncFromColor(cm.getCurrentColor());
            updateChipBorders();
        });

        // Swap button
        Label swapBtn = new Label("⇄");
        swapBtn.setStyle("-fx-text-fill: #8a8a8a; -fx-font-size: 11px; -fx-cursor: hand;");
        swapBtn.setLayoutX(30);
        swapBtn.setLayoutY(-2);
        swapBtn.setOnMouseClicked(_ -> {
            cm.swapColors();
            if (editing == Active.FG) syncFromColor(cm.getCurrentColor());
            else syncFromColor(cm.getBackgroundColor());
        });

        // Reset mini chips (black over white, bottom-left corner)
        Region miniWhite = new Region();
        miniWhite.setStyle("-fx-background-color: white; -fx-border-color: #555; -fx-border-width: 0.5;");
        miniWhite.setPrefSize(10, 10);
        miniWhite.setMinSize(10, 10);
        miniWhite.setMaxSize(10, 10);
        miniWhite.setLayoutX(-2);
        miniWhite.setLayoutY(36);

        Region miniBlack = new Region();
        miniBlack.setStyle("-fx-background-color: black; -fx-border-color: #555; -fx-border-width: 0.5;");
        miniBlack.setPrefSize(10, 10);
        miniBlack.setMinSize(10, 10);
        miniBlack.setMaxSize(10, 10);
        miniBlack.setLayoutX(2);
        miniBlack.setLayoutY(32);

        Pane resetOverlay = new Pane(miniWhite, miniBlack);
        resetOverlay.setPrefSize(14, 14);
        resetOverlay.setLayoutX(-3);
        resetOverlay.setLayoutY(30);
        resetOverlay.setCursor(Cursor.HAND);
        resetOverlay.setOnMouseClicked(_ -> {
            cm.resetColors();
            if (editing == Active.FG) syncFromColor(cm.getCurrentColor());
            else syncFromColor(cm.getBackgroundColor());
        });

        Pane chipPane = new Pane(bgChip, fgChip, swapBtn, resetOverlay);
        chipPane.setPrefSize(50, 50);
        chipPane.setMinSize(50, 50);
        chipPane.setMaxSize(50, 50);
        return chipPane;
    }

    private VBox buildSliders() {
        String[] labels = {"R", "G", "B"};
        VBox box = new VBox(4);
        for (int i = 0; i < 3; i++) {
            trackPanes[i] = new Pane();
            trackPanes[i].getStyleClass().add("slider-track");
            trackPanes[i].setPrefHeight(8);
            trackPanes[i].setMinHeight(8);
            trackPanes[i].setMaxHeight(8);
            trackPanes[i].setCursor(Cursor.H_RESIZE);

            thumbs[i] = new Region();
            thumbs[i].getStyleClass().add("slider-thumb");
            thumbs[i].setPrefSize(5, 14);
            thumbs[i].setMinSize(5, 14);
            thumbs[i].setMaxSize(5, 14);
            thumbs[i].setLayoutY(-3);
            thumbs[i].setMouseTransparent(true);
            trackPanes[i].getChildren().add(thumbs[i]);

            valFields[i] = new TextField("0");
            valFields[i].getStyleClass().add("val");
            valFields[i].setPrefWidth(38);
            valFields[i].setMinWidth(38);
            valFields[i].setMaxWidth(38);

            final int channel = i;
            trackPanes[i].widthProperty().addListener((_, _, _) -> refreshChannelThumb(channel));
            trackPanes[i].setOnMousePressed(e -> handleChannelDrag(channel, e.getX()));
            trackPanes[i].setOnMouseDragged(e -> handleChannelDrag(channel, e.getX()));

            valFields[i].textProperty().addListener((_, _, text) -> {
                if (updating) return;
                try {
                    int parsed = Integer.parseInt(text.trim());
                    int clamped = Math.max(0, Math.min(255, parsed));
                    applyChannelValue(channel, clamped);
                } catch (NumberFormatException ignored) {}
            });

            Label chanLabel = new Label(labels[i]);
            chanLabel.getStyleClass().add("slider-ch");
            chanLabel.setPrefWidth(12);
            chanLabel.setMinWidth(12);

            HBox row = new HBox(6, chanLabel, trackPanes[i], valFields[i]);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(trackPanes[i], Priority.ALWAYS);
            box.getChildren().add(row);
        }
        return box;
    }

    private HBox buildHexRow() {
        Label hexLabel = new Label("HEX");
        hexLabel.getStyleClass().add("hex-label");

        hexField.getStyleClass().add("hex");
        HBox.setHgrow(hexField, Priority.ALWAYS);
        hexField.textProperty().addListener((_, _, text) -> {
            if (updating) return;
            String t = text.startsWith("#") ? text : "#" + text;
            if (t.matches("#[0-9A-Fa-f]{6}")) {
                try {
                    applyColor(Color.web(t));
                } catch (Exception ignored) {}
            }
        });

        HBox row = new HBox(8, hexLabel, hexField);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent transparent transparent; " +
                     "-fx-border-width: 1 0 0 0; -fx-padding: 6 0 0 0;");
        // Use a region as top divider
        Region divider = new Region();
        divider.setStyle("-fx-background-color: #1e1e1e; -fx-pref-height: 1; -fx-max-height: 1;");
        VBox wrapper = new VBox(divider, row);
        return new HBox(wrapper);
    }

    // ── Color sync ────────────────────────────────────────────────

    private void syncFromColor(Color c) {
        if (c == null) return;
        h = c.getHue();
        s = c.getSaturation();
        v = c.getBrightness();
        refreshUI();
    }

    private void applyHsv(double newH, double newS, double newV) {
        h = newH; s = newS; v = newV;
        Color c = Color.hsb(h, s, v);
        updating = true;
        setActiveColor(c);
        updating = false;
        refreshUI();
    }

    private void applyColor(Color c) {
        h = c.getHue();
        s = c.getSaturation();
        v = c.getBrightness();
        updating = true;
        setActiveColor(c);
        updating = false;
        refreshUI();
    }

    private void applyChannelValue(int channel, int value) {
        Color current = Color.hsb(h, s, v);
        int r = ColorUtils.to255(current.getRed());
        int g = ColorUtils.to255(current.getGreen());
        int b = ColorUtils.to255(current.getBlue());
        switch (channel) {
            case 0 -> r = value;
            case 1 -> g = value;
            case 2 -> b = value;
        }
        Color newColor = Color.rgb(r, g, b);
        h = newColor.getHue();
        s = newColor.getSaturation();
        v = newColor.getBrightness();
        updating = true;
        setActiveColor(newColor);
        updating = false;
        // Only refresh non-active-channel UI to avoid disrupting the focused field
        refreshPickerBackground();
        refreshPickerRing();
        refreshHueThumb();
        refreshSliderGradients();
        refreshChannelThumb(0);
        refreshChannelThumb(1);
        refreshChannelThumb(2);
        refreshHexField();
        updateChips();
    }

    private void setActiveColor(Color c) {
        if (editing == Active.FG) cm.setCurrentColor(c);
        else cm.setBackgroundColor(c);
    }

    private Color getActiveColor() {
        return editing == Active.FG ? cm.getCurrentColor() : cm.getBackgroundColor();
    }

    // ── Drag handlers ─────────────────────────────────────────────

    private void handlePickerDrag(double x, double y) {
        double w = pickerPane.getWidth();
        double ph = pickerPane.getHeight();
        if (w < 1 || ph < 1) return;
        double ns = Math.max(0, Math.min(1, x / w));
        double nv = Math.max(0, Math.min(1, 1.0 - y / ph));
        applyHsv(h, ns, nv);
    }

    private void handleHueDrag(double x) {
        double w = huePane.getWidth();
        if (w < 1) return;
        double nh = Math.max(0, Math.min(359.99, (x / w) * 360));
        applyHsv(nh, s, v);
    }

    private void handleChannelDrag(int channel, double x) {
        double w = trackPanes[channel].getWidth();
        if (w < 1) return;
        int value = (int) Math.max(0, Math.min(255, (x / w) * 255));
        int prev = getChannelValue(channel);
        if (value != prev) HapticFeedback.sliderTick();
        applyChannelValue(channel, value);
    }

    private int getChannelValue(int channel) {
        Color c = Color.hsb(h, s, v);
        return switch (channel) {
            case 0 -> ColorUtils.to255(c.getRed());
            case 1 -> ColorUtils.to255(c.getGreen());
            case 2 -> ColorUtils.to255(c.getBlue());
            default -> 0;
        };
    }

    // ── Refresh helpers ───────────────────────────────────────────

    private void refreshUI() {
        refreshPickerBackground();
        refreshPickerRing();
        refreshHueThumb();
        refreshSliderGradients();
        refreshChannelThumb(0);
        refreshChannelThumb(1);
        refreshChannelThumb(2);
        refreshValFields();
        refreshHexField();
        updateChips();
    }

    private void refreshPickerBackground() {
        int hi = (int) Math.round(h);
        pickerPane.setStyle(String.format(
            "-fx-background-color: linear-gradient(to top, black, transparent)," +
            " linear-gradient(to right, white, hsb(%d, 100%%, 100%%)); " +
            "-fx-background-radius: 2;", hi));
    }

    private void refreshPickerRing() {
        double pw = pickerPane.getWidth();
        double ph = pickerPane.getHeight();
        if (pw < 1 || ph < 1) return;
        double rx = s * pw - pickerRing.getPrefWidth() / 2.0;
        double ry = (1.0 - v) * ph - pickerRing.getPrefHeight() / 2.0;
        pickerRing.setLayoutX(rx);
        pickerRing.setLayoutY(ry);
    }

    private void refreshHueThumb() {
        double sw = huePane.getWidth();
        if (sw < 1) return;
        hueThumb.setLayoutX(h / 360.0 * sw - hueThumb.getPrefWidth() / 2.0);
        hueThumb.setLayoutY((huePane.getHeight() - hueThumb.getPrefHeight()) / 2.0);
    }

    private void refreshSliderGradients() {
        Color c = Color.hsb(h, s, v);
        int r = ColorUtils.to255(c.getRed());
        int g = ColorUtils.to255(c.getGreen());
        int b = ColorUtils.to255(c.getBlue());
        trackPanes[0].setStyle(String.format(
            "-fx-background-color: linear-gradient(to right, rgb(0,%d,%d), rgb(255,%d,%d)); -fx-background-radius: 1;", g, b, g, b));
        trackPanes[1].setStyle(String.format(
            "-fx-background-color: linear-gradient(to right, rgb(%d,0,%d), rgb(%d,255,%d)); -fx-background-radius: 1;", r, b, r, b));
        trackPanes[2].setStyle(String.format(
            "-fx-background-color: linear-gradient(to right, rgb(%d,%d,0), rgb(%d,%d,255)); -fx-background-radius: 1;", r, g, r, g));
    }

    private void refreshChannelThumb(int i) {
        double w = trackPanes[i].getWidth();
        if (w < 1) return;
        int val = getChannelValue(i);
        thumbs[i].setLayoutX((val / 255.0) * w - thumbs[i].getPrefWidth() / 2.0);
    }

    private void refreshValFields() {
        updating = true;
        Color c = Color.hsb(h, s, v);
        valFields[0].setText(String.valueOf(ColorUtils.to255(c.getRed())));
        valFields[1].setText(String.valueOf(ColorUtils.to255(c.getGreen())));
        valFields[2].setText(String.valueOf(ColorUtils.to255(c.getBlue())));
        updating = false;
    }

    private void refreshHexField() {
        updating = true;
        hexField.setText(ColorUtils.toWebHex(Color.hsb(h, s, v)));
        updating = false;
    }

    private void updateChips() {
        Color fg = cm.getCurrentColor();
        Color bg = cm.getBackgroundColor();
        if (fg != null) fgChip.setStyle("-fx-background-color: " + ColorUtils.toWebHex(fg) +
            "; -fx-background-radius: 2;" + (editing == Active.FG ? " -fx-border-color: #2f8af7; -fx-border-width: 1.5; -fx-border-radius: 2;" : " -fx-border-color: #555; -fx-border-width: 1; -fx-border-radius: 2;") +
            " -fx-effect: dropshadow(gaussian,#000,3,0,0,1);");
        if (bg != null) bgChip.setStyle("-fx-background-color: " + ColorUtils.toWebHex(bg) +
            "; -fx-background-radius: 2;" + (editing == Active.BG ? " -fx-border-color: #2f8af7; -fx-border-width: 1.5; -fx-border-radius: 2;" : " -fx-border-color: #555; -fx-border-width: 1; -fx-border-radius: 2;") +
            " -fx-effect: dropshadow(gaussian,#000,3,0,0,1);");
    }

    private void updateChipBorders() {
        updateChips();
    }
}
