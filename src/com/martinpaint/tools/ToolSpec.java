package com.martinpaint.tools;

import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum ToolSpec {
    BRUSH("Brush", KeyCode.B, false, NavigationMode.NONE, 5.0),
    PENCIL("Pencil", KeyCode.N, false, NavigationMode.NONE, 2.0),
    ERASER("Eraser", KeyCode.E, true, NavigationMode.NONE, 20.0),
    FILL("Bucket Fill", KeyCode.G, true, NavigationMode.NONE, 0.0),
    EYEDROPPER("Eyedropper", KeyCode.I, false, NavigationMode.NONE, 0.0),
    SELECTION("Selection", KeyCode.M, true, NavigationMode.NONE, 0.0),
    HAND("Hand", KeyCode.H, true, NavigationMode.PAN, 0.0),
    ZOOM("Zoom", KeyCode.Z, false, NavigationMode.ZOOM, 0.0);

    public enum NavigationMode {
        NONE,
        PAN,
        ZOOM
    }

    private static final List<ToolSpec> TOOLBAR_ORDER = List.of(
            BRUSH, PENCIL, ERASER, FILL, EYEDROPPER, SELECTION, HAND, ZOOM
    );
    private static final Map<KeyCode, ToolSpec> BY_SHORTCUT = TOOLBAR_ORDER.stream()
            .collect(Collectors.toUnmodifiableMap(ToolSpec::shortcut, spec -> spec));

    private final String displayName;
    private final KeyCode shortcut;
    private final boolean separatorBefore;
    private final NavigationMode navigationMode;
    private final double defaultSize;

    ToolSpec(String displayName, KeyCode shortcut, boolean separatorBefore,
             NavigationMode navigationMode, double defaultSize) {
        this.displayName = displayName;
        this.shortcut = shortcut;
        this.separatorBefore = separatorBefore;
        this.navigationMode = navigationMode;
        this.defaultSize = defaultSize;
    }

    public static List<ToolSpec> toolbarOrder() {
        return TOOLBAR_ORDER;
    }

    public static ToolSpec fromShortcut(KeyCode code) {
        return BY_SHORTCUT.get(code);
    }

    public Tool createTool() {
        return switch (this) {
            case BRUSH -> new BrushTool();
            case PENCIL -> new PencilTool();
            case ERASER -> new EraserTool();
            case FILL -> new FillTool();
            case EYEDROPPER -> new EyeDropperTool();
            case SELECTION -> new SelectionTool();
            case HAND, ZOOM -> new PassiveTool(this);
        };
    }

    public String displayName() {
        return displayName;
    }

    public KeyCode shortcut() {
        return shortcut;
    }

    public boolean separatorBefore() {
        return separatorBefore;
    }

    public NavigationMode navigationMode() {
        return navigationMode;
    }

    public double defaultSize() {
        return defaultSize;
    }
}
