package com.martinpaint.ui;

import com.martinpaint.tools.ToolSpec;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.Map;

// Provides the design's SVG line icons for the tool rail.
// All icons are on a 24x24 grid, 1.5px stroke, scaled to ICON_SIZE display pixels.
public final class ToolIcons {

    private static final double ICON_SIZE  = 18.0;
    private static final double VIEWBOX    = 24.0;
    private static final double SCALE      = ICON_SIZE / VIEWBOX;
    private static final String PATH_STYLE =
            "-fx-fill: none; -fx-stroke: white; -fx-stroke-width: 1.5; " +
            "-fx-stroke-line-cap: round; -fx-stroke-line-join: round;";
    private static final String FILL_STYLE =
            "-fx-fill: white; -fx-stroke: none;";

    private ToolIcons() {}

    /** Returns a scaled Group of SVGPaths for the tool, or null if unknown. */
    public static Node iconFor(ToolSpec toolSpec) {
        var icon = ICONS.get(toolSpec);
        if (icon == null) return null;
        Group g = new Group();
        for (PathSpec ps : icon) {
            SVGPath p = new SVGPath();
            p.setContent(ps.d());
            p.setStyle(ps.filled() ? FILL_STYLE : PATH_STYLE);
            g.getChildren().add(p);
        }
        g.setScaleX(SCALE);
        g.setScaleY(SCALE);
        return g;
    }

    private record PathSpec(String d, boolean filled) {
        static PathSpec stroke(String d) { return new PathSpec(d, false); }
        static PathSpec fill(String d)   { return new PathSpec(d, true);  }
    }

    private static final Map<ToolSpec, List<PathSpec>> ICONS = Map.ofEntries(
        Map.entry(ToolSpec.BRUSH, List.of(
            PathSpec.stroke("M14 4l6 6-8 8-3 1-3-3 1-3 7-9z"),
            PathSpec.stroke("M11 13l-3 3M16 8l2 2")
        )),
        Map.entry(ToolSpec.PENCIL, List.of(
            PathSpec.stroke("M4 20l4-1 11-11-3-3L5 16l-1 4z"),
            PathSpec.stroke("M14 6l4 4")
        )),
        Map.entry(ToolSpec.ERASER, List.of(
            PathSpec.stroke("M8 20l-4-4L14 6l4 4-10 10z"),
            PathSpec.stroke("M8 20h10M11 9l4 4")
        )),
        Map.entry(ToolSpec.FILL, List.of(
            PathSpec.stroke("M5 11l7-7 7 7-7 7-7-7z"),
            PathSpec.stroke("M5 11l-1 4a2 2 0 1 0 4 0")
        )),
        Map.entry(ToolSpec.EYEDROPPER, List.of(
            PathSpec.stroke("M14 4l6 6-2 2-6-6 2-2z"),
            PathSpec.stroke("M12 8l-7 7v4h4l7-7"),
            PathSpec.stroke("M10 14l3 3")
        )),
        Map.entry(ToolSpec.SELECTION, List.of(
            PathSpec.stroke("M4 4h5M15 4h5v5M20 15v5h-5M4 9V4M4 20h5M4 15v5"),
            PathSpec.fill("M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0")
        )),
        Map.entry(ToolSpec.HAND, List.of(
            PathSpec.stroke("M9 11V5a1.5 1.5 0 1 1 3 0v6M12 11V4a1.5 1.5 0 1 1 3 0v7" +
                "M15 11V6a1.5 1.5 0 1 1 3 0v8c0 4-3 7-7 7s-7-3-7-7v-3a1.5 1.5 0 1 1 3 0v2")
        )),
        Map.entry(ToolSpec.ZOOM, List.of(
            PathSpec.stroke("M5 11a6 6 0 1 0 12 0a6 6 0 1 0 -12 0"),
            PathSpec.stroke("M16 16l4 4M8 11h6M11 8v6")
        ))
    );
}
