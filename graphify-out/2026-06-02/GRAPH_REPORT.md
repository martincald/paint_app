# Graph Report - Paint App  (2026-06-02)

## Corpus Check
- 66 files · ~62,508 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 841 nodes · 1740 edges · 58 communities (43 shown, 15 thin omitted)
- Extraction: 74% EXTRACTED · 26% INFERRED · 0% AMBIGUOUS · INFERRED: 454 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `87c5592b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Color History & UI Palette|Color History & UI Palette]]
- [[_COMMUNITY_Color Manager|Color Manager]]
- [[_COMMUNITY_Dev Agents & Architecture|Dev Agents & Architecture]]
- [[_COMMUNITY_Canvas Management|Canvas Management]]
- [[_COMMUNITY_Tool Base & Input Events|Tool Base & Input Events]]
- [[_COMMUNITY_Sized Tool & Icons|Sized Tool & Icons]]
- [[_COMMUNITY_Brush & Fill Tools|Brush & Fill Tools]]
- [[_COMMUNITY_Tool Settings UI|Tool Settings UI]]
- [[_COMMUNITY_Color Utilities|Color Utilities]]
- [[_COMMUNITY_Eraser & Pencil Tools|Eraser & Pencil Tools]]
- [[_COMMUNITY_Menu Bar & Keyboard|Menu Bar & Keyboard]]
- [[_COMMUNITY_Selection Operations|Selection Operations]]
- [[_COMMUNITY_Selection State & Undo|Selection State & Undo]]
- [[_COMMUNITY_Canvas Viewport|Canvas Viewport]]
- [[_COMMUNITY_Haptic Feedback JNI|Haptic Feedback JNI]]
- [[_COMMUNITY_Resize Handles|Resize Handles]]
- [[_COMMUNITY_Selection Overlay|Selection Overlay]]
- [[_COMMUNITY_App Controller|App Controller]]
- [[_COMMUNITY_Selection Drag Handlers|Selection Drag Handlers]]
- [[_COMMUNITY_Selection Tool|Selection Tool]]
- [[_COMMUNITY_Passive Tool|Passive Tool]]
- [[_COMMUNITY_EyeDropper Icon|EyeDropper Icon]]
- [[_COMMUNITY_Fill Tool Icon|Fill Tool Icon]]
- [[_COMMUNITY_Eraser Icon|Eraser Icon]]
- [[_COMMUNITY_Pencil Icon|Pencil Icon]]
- [[_COMMUNITY_UI Window Packages|UI Window Packages]]
- [[_COMMUNITY_Canvas Input Handling|Canvas Input Handling]]
- [[_COMMUNITY_App Icon Asset|App Icon Asset]]
- [[_COMMUNITY_Brush Icon Asset|Brush Icon Asset]]
- [[_COMMUNITY_Selection Icon Asset|Selection Icon Asset]]
- [[_COMMUNITY_Zoom Icon Asset|Zoom Icon Asset]]
- [[_COMMUNITY_Hand Tool Icon|Hand Tool Icon]]
- [[_COMMUNITY_Color Picker UI|Color Picker UI]]
- [[_COMMUNITY_Opacity Interface|Opacity Interface]]
- [[_COMMUNITY_Claude Settings|Claude Settings]]
- [[_COMMUNITY_Local Permissions|Local Permissions]]
- [[_COMMUNITY_Tool Settings Container|Tool Settings Container]]
- [[_COMMUNITY_Icon Update Script|Icon Update Script]]
- [[_COMMUNITY_SelectionOrigin Package|SelectionOrigin Package]]
- [[_COMMUNITY_EyeDropper Package|EyeDropper Package]]
- [[_COMMUNITY_PassiveTool Package|PassiveTool Package]]
- [[_COMMUNITY_CanvasTabBar Package|CanvasTabBar Package]]
- [[_COMMUNITY_ColorHistoryPanel Package|ColorHistoryPanel Package]]
- [[_COMMUNITY_Panels Package|Panels Package]]
- [[_COMMUNITY_SidePanel Package|SidePanel Package]]
- [[_COMMUNITY_ToolIcons Package|ToolIcons Package]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]

## God Nodes (most connected - your core abstractions)
1. `CanvasManager` - 29 edges
2. `SelectionController` - 26 edges
3. `ColorPickerPanel` - 18 edges
4. `SizedTool` - 18 edges
5. `CanvasViewport` - 16 edges
6. `LayerManager` - 16 edges
7. `SelectionOverlay` - 16 edges
8. `Tool` - 15 edges
9. `Layer` - 15 edges
10. `RgbSlidersView` - 13 edges

## Surprising Connections (you probably didn't know these)
- `Pencil Tool Icon` --references--> `PencilTool (Paint App)`  [INFERRED]
  src/resources/images/pencil.svg → src/com/martinpaint/tools/PencilTool.java
- `Dual-Layer Canvas System` --implements--> `com.martinpaint.canvas.CanvasManager`  [INFERRED]
  README.md → sources.txt
- `Tool Manager` --implements--> `com.martinpaint.tools.ToolManager`  [INFERRED]
  README.md → sources.txt
- `Selection Controller` --implements--> `com.martinpaint.selection.SelectionController`  [INFERRED]
  README.md → sources.txt
- `Haptic Feedback (JNI/macOS)` --implements--> `com.martinpaint.app.HapticFeedback`  [INFERRED]
  README.md → sources.txt

## Hyperedges (group relationships)
- **Tool Class Hierarchy: Tool, SizedTool, BrushTool/EraserTool/PencilTool** — pkg_tool, pkg_sizedtool, pkg_brushtool, pkg_erasertool, pkg_penciltool [INFERRED 0.85]
- **Selection Subsystem: SelectionController, Selection, SelectionOverlay, SelectionInteractionMode** — pkg_selectioncontroller, pkg_selection, pkg_selectionoverlay, pkg_selectioninteractionmode [INFERRED 0.85]
- **paint-orchestrator delegates to java-architect, performance-engineer, debugger, cpp-pro, refactoring-specialist** — agent_paint_orchestrator, agent_java_architect, agent_performance_engineer, agent_debugger, agent_cpp_pro [EXTRACTED 1.00]

## Communities (58 total, 15 thin omitted)

### Community 0 - "Color History & UI Palette"
Cohesion: 0.09
Nodes (13): LayerManager, Dragboard, IntegerProperty, Layer, Layer, LayerState, List, ObservableList (+5 more)

### Community 1 - "Color Manager"
Cohesion: 0.07
Nodes (23): BiConsumer, Circle, Consumer, Cursor, Double, DoubleConsumer, Pane, Runnable (+15 more)

### Community 2 - "Dev Agents & Architecture"
Cohesion: 0.07
Nodes (46): cpp-pro Agent, debugger Agent, java-architect Agent, paint-orchestrator Agent, performance-engineer Agent, refactoring-specialist Agent, Multi-Agent Orchestration Pattern, Clean Architecture (+38 more)

### Community 3 - "Canvas Management"
Cohesion: 0.10
Nodes (13): CanvasManager, CanvasSnapshot, Deque, ReadOnlyLongProperty, SnapshotParameters, Canvas, LayerManager, LayerState (+5 more)

### Community 4 - "Tool Base & Input Events"
Cohesion: 0.09
Nodes (27): KeyCode, NavigationMode, String, CanvasManager, ColorManager, List, ObjectProperty, Tool (+19 more)

### Community 5 - "Sized Tool & Icons"
Cohesion: 0.12
Nodes (14): PathSpec, Canvas, Color, GraphicsContext, Override, ToolSpec, Node, String (+6 more)

### Community 6 - "Brush & Fill Tools"
Cohesion: 0.14
Nodes (7): OpacityAware, Color, Override, GraphicsContext, Override, BrushTool, FillTool

### Community 7 - "Tool Settings UI"
Cohesion: 0.21
Nodes (11): FillTool, IntConsumer, Slider, HBox, Node, OpacityAware, SizedTool, String (+3 more)

### Community 8 - "Color Utilities"
Cohesion: 0.15
Nodes (7): ColorUtils, Integer, ObjIntConsumer, Color, String, Color, RgbSlidersView

### Community 9 - "Eraser & Pencil Tools"
Cohesion: 0.06
Nodes (23): BooleanProperty, Layer, DoubleProperty, Point2D, ReadOnlyDoubleProperty, ScrollPane, Canvas, GraphicsContext (+15 more)

### Community 10 - "Menu Bar & Keyboard"
Cohesion: 0.12
Nodes (19): ActionEvent, File, FileManager, FileManager, KeyCombination, MenuBar, MenuItem, SelectionTool (+11 more)

### Community 11 - "Selection Operations"
Cohesion: 0.07
Nodes (17): ClipboardService, xSign(), ySign(), Selection, SelectionController, SelectionOrigin, GraphicsContext, WritableImage (+9 more)

### Community 12 - "Selection State & Undo"
Cohesion: 0.05
Nodes (43): Active, Button, ColorHistory, ColorManager, ColorHistory, FlowPane, HBox, Label (+35 more)

### Community 13 - "Canvas Viewport"
Cohesion: 0.09
Nodes (14): ReadOnlyIntegerProperty, SizedTool, GraphicsContext, Override, Color, GraphicsContext, Override, CanvasManager (+6 more)

### Community 14 - "Haptic Feedback JNI"
Cohesion: 0.20
Nodes (3): HapticFeedback, Path, ToolSpec

### Community 15 - "Resize Handles"
Cohesion: 0.22
Nodes (8): 1. Architecture Analysis, 2. Implementation Phase, 3. Quality Verification, C++ Project Assessment, code:json ({), code:json ({), Communication Protocol, Development Workflow

### Community 17 - "Selection Overlay"
Cohesion: 0.22
Nodes (8): 1. Issue Analysis, 2. Implementation Phase, 3. Resolution Excellence, code:json ({), code:json ({), Communication Protocol, Debugging Context, Development Workflow

### Community 18 - "App Controller"
Cohesion: 0.09
Nodes (19): AppController, Application, ClipboardService, Main, SelectionOverlay, CanvasManager, ColorManager, Stage (+11 more)

### Community 19 - "Selection Drag Handlers"
Cohesion: 0.22
Nodes (8): 1. Architecture Analysis, 2. Implementation Phase, 3. Quality Assurance, code:json ({), code:json ({), Communication Protocol, Development Workflow, Java Project Assessment

### Community 20 - "Selection Tool"
Cohesion: 0.22
Nodes (8): 1. Performance Analysis, 2. Implementation Phase, 3. Performance Excellence, code:json ({), code:json ({), Communication Protocol, Development Workflow, Performance Assessment

### Community 21 - "Passive Tool"
Cohesion: 0.36
Nodes (4): GraphicsContext, Override, ToolSpec, PassiveTool

### Community 23 - "EyeDropper Icon"
Cohesion: 0.43
Nodes (7): Eyedropper Body Path (diagonal barrel shape), Eyedropper Handle/Dropper Tip Path, Eyedropper Tool Icon, SVG Vector Graphic (24x24 viewBox), Eyedropper Cross-line Detail Path, Color Picker / Eyedropper Tool Concept, White Stroke Style

### Community 24 - "Fill Tool Icon"
Cohesion: 0.40
Nodes (6): Diamond / Rhombus Shape (bucket body outline), Paint Drip / Drop Element, Fill Tool Concept (flood fill / bucket fill), Paint Bucket / Fill Tool Icon, SVG Vector Asset for Paint App UI, FillTool Java Class

### Community 25 - "Eraser Icon"
Cohesion: 0.40
Nodes (6): Eraser Tool Icon, Eraser Baseline / Tip Line, Eraser Body Path (diagonal parallelogram shape), SVG Vector Graphic (24x24 viewBox), Eraser Tool (Paint Application Feature), EraserTool Java Class

### Community 26 - "Pencil Icon"
Cohesion: 0.33
Nodes (6): Pencil Body Path (diagonal shaft and tip), Pencil Highlight/Glint Line, Pencil Tool Icon, PencilTool (Paint App), SVG Vector Graphic, Pencil Drawing Tool

### Community 27 - "UI Window Packages"
Cohesion: 0.33
Nodes (6): com.martinpaint.ui.AppMenuBar, com.martinpaint.ui.CanvasViewport, com.martinpaint.ui.MainWindow, com.martinpaint.ui.OptionsBar, com.martinpaint.ui.StatusBar, com.martinpaint.ui.ToolPanel

### Community 30 - "Canvas Input Handling"
Cohesion: 0.22
Nodes (8): 1. Code Analysis, 2. Implementation Phase, 3. Code Excellence, code:json ({), code:json ({), Communication Protocol, Development Workflow, Refactoring Context Assessment

### Community 31 - "App Icon Asset"
Cohesion: 0.60
Nodes (5): Dark Rounded Rectangle Background, Primary Colors (Red, Blue, Yellow) on Bristles, App Icon - Paint Brush, Paint Brush Visual Element, Digital Painting / Drawing Application Concept

### Community 32 - "Brush Icon Asset"
Cohesion: 0.60
Nodes (5): Paint Brush Tool Concept, White Outline Icon Style, Brush Tool Icon (SVG), Brush Body Path, Brush Detail Strokes

### Community 33 - "Selection Icon Asset"
Cohesion: 0.70
Nodes (5): Center Dot (crosshair reference point), Dashed Rectangle Border (marching ants selection outline), Selection Tool Icon, Paint Application Tool Panel, Rectangular Selection Tool

### Community 34 - "Zoom Icon Asset"
Cohesion: 0.60
Nodes (5): Zoom Tool Icon (SVG), Magnifying Glass Shape, Plus Symbol Inside Lens, ToolManager (Java), Zoom In Concept

### Community 36 - "Hand Tool Icon"
Cohesion: 0.67
Nodes (4): Hand Tool Icon (SVG), Paint Application, Hand Tool (Pan/Navigate Canvas), Hand/Finger Shape Visual Element

### Community 37 - "Color Picker UI"
Cohesion: 0.50
Nodes (4): com.martinpaint.ui.ColorPickerPanel, com.martinpaint.ui.HsvPickerSquare, com.martinpaint.ui.HueStrip, com.martinpaint.ui.RgbSlidersView

### Community 39 - "Claude Settings"
Cohesion: 0.29
Nodes (6): enabledPlugins, frontend-design@claude-plugins-official, env, CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS, hooks, PreToolUse

### Community 54 - "Community 54"
Cohesion: 0.50
Nodes (3): Available subagents and when to delegate to them, Project structure, Workflow

## Knowledge Gaps
- **108 isolated node(s):** `update_icon.sh script`, `frontend-design@claude-plugins-official`, `PreToolUse`, `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS`, `allow` (+103 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SelectionController` connect `Selection Operations` to `Color Manager`, `App Controller`, `Canvas Management`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `SizedTool` connect `Sized Tool & Icons` to `Eraser & Pencil Tools`, `Canvas Viewport`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `Tool` connect `Canvas Viewport` to `Tool Base & Input Events`, `Brush & Fill Tools`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **What connects `update_icon.sh script`, `frontend-design@claude-plugins-official`, `PreToolUse` to the rest of the system?**
  _110 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Color History & UI Palette` be split into smaller, more focused modules?**
  _Cohesion score 0.09268707482993198 - nodes in this community are weakly interconnected._
- **Should `Color Manager` be split into smaller, more focused modules?**
  _Cohesion score 0.06708595387840671 - nodes in this community are weakly interconnected._
- **Should `Dev Agents & Architecture` be split into smaller, more focused modules?**
  _Cohesion score 0.06938020351526364 - nodes in this community are weakly interconnected._