# Graph Report - Paint App  (2026-06-09)

## Corpus Check
- 68 files · ~63,295 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 856 nodes · 1894 edges · 74 communities (60 shown, 14 thin omitted)
- Extraction: 77% EXTRACTED · 23% INFERRED · 0% AMBIGUOUS · INFERRED: 430 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `87c5592b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Selection & Clipboard System|Selection & Clipboard System]]
- [[_COMMUNITY_Layer Management & Undo|Layer Management & Undo]]
- [[_COMMUNITY_Color State Management|Color State Management]]
- [[_COMMUNITY_Drawing Tool Base Classes|Drawing Tool Base Classes]]
- [[_COMMUNITY_Canvas Compositing Engine|Canvas Compositing Engine]]
- [[_COMMUNITY_Opacity Drawing Tools|Opacity Drawing Tools]]
- [[_COMMUNITY_Tool Settings & Haptics|Tool Settings & Haptics]]
- [[_COMMUNITY_Color Utilities|Color Utilities]]
- [[_COMMUNITY_Viewport & Zoom System|Viewport & Zoom System]]
- [[_COMMUNITY_File IO & PNG Export|File I/O & PNG Export]]
- [[_COMMUNITY_Layer Model & Properties|Layer Model & Properties]]
- [[_COMMUNITY_Canvas Tab & Options Bar|Canvas Tab & Options Bar]]
- [[_COMMUNITY_Tool Interface Contract|Tool Interface Contract]]
- [[_COMMUNITY_Application Controller|Application Controller]]
- [[_COMMUNITY_App Entry & Selection Init|App Entry & Selection Init]]
- [[_COMMUNITY_Menu Bar & Shortcuts|Menu Bar & Shortcuts]]
- [[_COMMUNITY_Color History Panel|Color History Panel]]
- [[_COMMUNITY_Haptic Feedback (JNI)|Haptic Feedback (JNI)]]
- [[_COMMUNITY_Tool Selection Panel|Tool Selection Panel]]
- [[_COMMUNITY_Status Bar & Zoom|Status Bar & Zoom]]
- [[_COMMUNITY_Tool Manager|Tool Manager]]
- [[_COMMUNITY_Tool Specification Enum|Tool Specification Enum]]
- [[_COMMUNITY_Application Overview|Application Overview]]
- [[_COMMUNITY_Eyedropper Tool|Eyedropper Tool]]
- [[_COMMUNITY_Passive (Pan) Tool|Passive (Pan) Tool]]
- [[_COMMUNITY_Claude Code Settings|Claude Code Settings]]
- [[_COMMUNITY_Eyedropper Icon Assets|Eyedropper Icon Assets]]
- [[_COMMUNITY_App Launch Entry|App Launch Entry]]
- [[_COMMUNITY_Fill Tool Icon Assets|Fill Tool Icon Assets]]
- [[_COMMUNITY_Pencil Tool Icon Assets|Pencil Tool Icon Assets]]
- [[_COMMUNITY_App Icon Assets|App Icon Assets]]
- [[_COMMUNITY_Brush Tool Icon Assets|Brush Tool Icon Assets]]
- [[_COMMUNITY_Eraser Tool Icon Assets|Eraser Tool Icon Assets]]
- [[_COMMUNITY_Selection Tool Icon Assets|Selection Tool Icon Assets]]
- [[_COMMUNITY_Hand Tool Icon Assets|Hand Tool Icon Assets]]
- [[_COMMUNITY_Zoom Tool Icon Assets|Zoom Tool Icon Assets]]
- [[_COMMUNITY_Local Permissions Config|Local Permissions Config]]
- [[_COMMUNITY_Architecture Patterns|Architecture Patterns]]
- [[_COMMUNITY_Icon Build Script|Icon Build Script]]
- [[_COMMUNITY_Performance Agent|Performance Agent]]
- [[_COMMUNITY_Debugger Agent|Debugger Agent]]
- [[_COMMUNITY_C++ Pro Agent|C++ Pro Agent]]
- [[_COMMUNITY_Refactoring Agent|Refactoring Agent]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 70|Community 70]]

## God Nodes (most connected - your core abstractions)
1. `CanvasManager` - 56 edges
2. `SelectionController` - 37 edges
3. `Tool` - 24 edges
4. `ColorPickerPanel` - 23 edges
5. `CanvasViewport` - 23 edges
6. `SizedTool` - 23 edges
7. `LayerManager` - 22 edges
8. `SelectionOverlay` - 21 edges
9. `Layer` - 20 edges
10. `ColorManager` - 19 edges

## Surprising Connections (you probably didn't know these)
- `paint-orchestrator agent` --references--> `CanvasManager`  [EXTRACTED]
  .claude/agents/paint-orchestrator.md → src/com/martinpaint/canvas/CanvasManager.java
- `ToolSettingsView` --references--> `HapticFeedback.m (JNI bridge)`  [INFERRED]
  src/com/martinpaint/ui/ToolSettingsView.java → native/HapticFeedback.m
- `paint-orchestrator agent` --references--> `SelectionController`  [EXTRACTED]
  .claude/agents/paint-orchestrator.md → src/com/martinpaint/selection/SelectionController.java
- `Pencil Tool Icon` --references--> `PencilTool (Paint App)`  [INFERRED]
  src/resources/images/pencil.svg → src/com/martinpaint/tools/PencilTool.java
- `PassiveTool` --semantically_similar_to--> `EraserTool`  [INFERRED] [semantically similar]
  src/com/martinpaint/tools/PassiveTool.java → src/com/martinpaint/tools/EraserTool.java

## Hyperedges (group relationships)
- **HSV Color Picker system: HsvPickerSquare + HueStrip + RgbSlidersView wired by ColorPickerPanel** — ui_colorpickerpanel_colorpickerpanel, ui_hsvpickersquare_hsvpickersquare, ui_huestrip_huestrip, ui_rgbslidersview_rgbslidersview [EXTRACTED 1.00]
- **MainWindow assembles ToolPanel, SidePanel, CanvasArea (TabBar+Viewport), StatusBar, OptionsBar into BorderPane** — ui_mainwindow_mainwindow, ui_canvasviewport_canvasviewport, ui_canvastabbar_canvastabbar, ui_statusbar_statusbar, ui_optionsbar_optionsbar [EXTRACTED 1.00]
- **Panels utility shared by ColorHistoryPanel, ColorPickerPanel, ToolSettingsContainer, HsvPickerSquare, HueStrip, RgbSlidersView** — ui_panels_panels, ui_colorhistorypanel_colorhistorypanel, ui_colorpickerpanel_colorpickerpanel, ui_toolsettingscontainer_toolsettingscontainer, ui_hsvpickersquare_hsvpickersquare [EXTRACTED 1.00]
- **Tool inheritance hierarchy: Tool → SizedTool → BrushTool/EraserTool; Tool → EyeDropperTool/SelectionTool/PassiveTool** — tools_tool_tool, tools_sizedtool_sizedtool, tools_brushtool_brushtool, tools_erasertool_erasertool, tools_eyedroppertool_eyedroppertool, tools_selectiontool_selectiontool, tools_passivetool_passivetool [EXTRACTED 1.00]
- **AppController composes and wires together ColorManager, CanvasManager, ToolManager, FileManager, and MainWindow** — app_appcontroller_appcontroller, color_colormanager_colormanager, tools_toolmanager_toolmanager, io_filemanager_filemanager [EXTRACTED 1.00]
- **SidePanel aggregates ColorPickerPanel, ColorHistoryPanel, ToolSettingsContainer, and LayerPanel into the right-side UI column** — ui_sidepanel_sidepanel, ui_layerpanel_layerpanel, color_colormanager_colormanager, tools_toolmanager_toolmanager [EXTRACTED 1.00]
- **Undo/Redo snapshot chain: CanvasManager, CanvasSnapshot, LayerState, LayerManager** — canvas_canvasmanager_canvasmanager, canvas_canvassnapshot_canvassnapshot, canvas_layerstate_layerstate, canvas_layermanager_layermanager [EXTRACTED 1.00]
- **Selection state machine: SelectionController, SelectionInteractionMode, SelectionOverlay, Selection** — selection_selectioncontroller_selectioncontroller, selection_selectioninteractionmode_selectioninteractionmode, selection_selectionoverlay_selectionoverlay, selection_selection_selection [EXTRACTED 1.00]
- **Layer compositing pipeline: CanvasManager, Layer, LayerManager** — canvas_canvasmanager_canvasmanager, canvas_layer_layer, canvas_layermanager_layermanager [EXTRACTED 1.00]

## Communities (74 total, 14 thin omitted)

### Community 1 - "Layer Management & Undo"
Cohesion: 0.06
Nodes (38): Button, ColorHistory, ColorHistory, Cursor, FlowPane, OpacityAware, Color, ObservableList (+30 more)

### Community 2 - "Color State Management"
Cohesion: 0.09
Nodes (14): Active, BiConsumer, ColorManager, Double, DoubleConsumer, Pane, Color, ObjectProperty (+6 more)

### Community 3 - "Drawing Tool Base Classes"
Cohesion: 0.07
Nodes (22): PathSpec, SizedTool, GraphicsContext, Override, Color, GraphicsContext, Override, Canvas (+14 more)

### Community 4 - "Canvas Compositing Engine"
Cohesion: 0.17
Nodes (6): BooleanSupplier, Deque, ReadOnlyLongProperty, Canvas, SnapshotParameters, WritableImage

### Community 5 - "Opacity Drawing Tools"
Cohesion: 0.21
Nodes (4): Color, Override, BrushTool, OpacityAware

### Community 6 - "Tool Settings & Haptics"
Cohesion: 0.18
Nodes (13): FillTool, IntConsumer, HapticFeedback.m (JNI bridge), JNI-to-macOS NSHapticFeedbackManager bridge, Slider, HBox, Node, OpacityAware (+5 more)

### Community 7 - "Color Utilities"
Cohesion: 0.15
Nodes (7): ColorUtils, Integer, ObjIntConsumer, Color, String, Color, RgbSlidersView

### Community 8 - "Viewport & Zoom System"
Cohesion: 0.09
Nodes (19): Layered canvas z-order: bg → layers → interaction → preview → overlays, Smooth zoom via AnimationTimer lerp, One-shot viewportBounds listener for initial centering, HBox, Point2D, ReadOnlyDoubleProperty, ScrollPane, CanvasViewport (+11 more)

### Community 9 - "File I/O & PNG Export"
Cohesion: 0.31
Nodes (4): GraphicsContext, Override, SelectionController, SelectionTool

### Community 10 - "Layer Model & Properties"
Cohesion: 0.11
Nodes (11): BooleanProperty, Layer, DoubleProperty, Canvas, GraphicsContext, SnapshotParameters, String, WritableImage (+3 more)

### Community 11 - "Canvas Tab & Options Bar"
Cohesion: 0.15
Nodes (8): CanvasManager, CanvasSnapshot, LayerState, SRC_OVER pixel compositing, CanvasSnapshot, LayerState, List, IntStack

### Community 12 - "Tool Interface Contract"
Cohesion: 0.16
Nodes (6): ReadOnlyIntegerProperty, CanvasManager, ColorManager, GraphicsContext, String, Tool

### Community 13 - "Application Controller"
Cohesion: 0.19
Nodes (8): AppController, FileManager, CanvasManager, ColorManager, Stage, ToolManager, AppController, AppMenuBar

### Community 14 - "App Entry & Selection Init"
Cohesion: 0.16
Nodes (8): Main, EventHandler, KeyEvent, NavMode, AppController, NavMode, Tool, MainWindow

### Community 15 - "Menu Bar & Shortcuts"
Cohesion: 0.18
Nodes (13): ActionEvent, KeyCombination, MenuBar, MenuItem, SelectionTool, AppController, CanvasViewport, EventHandler (+5 more)

### Community 16 - "Color History Panel"
Cohesion: 0.16
Nodes (7): Dragboard, Runnable, ObservableList, CanvasManager, Layer, Window, LayerPanel

### Community 17 - "Haptic Feedback (JNI)"
Cohesion: 0.20
Nodes (3): HapticFeedback, Path, ToolSpec

### Community 18 - "Tool Selection Panel"
Cohesion: 0.19
Nodes (11): ToolSpec, ColorManager, Node, Pane, Region, Tool, ToolManager, ToggleButton (+3 more)

### Community 19 - "Status Bar & Zoom"
Cohesion: 0.16
Nodes (6): LayerManager, Undo/Redo via snapshot stacks, IntegerProperty, Layer, LayerState, List

### Community 20 - "Tool Manager"
Cohesion: 0.22
Nodes (7): GraphicsContext, CanvasManager, ColorManager, List, ObjectProperty, Tool, ToolManager

### Community 21 - "Tool Specification Enum"
Cohesion: 0.23
Nodes (11): KeyCode, NavigationMode, List, String, Tool, createTool(), displayName(), fromShortcut() (+3 more)

### Community 22 - "Application Overview"
Cohesion: 0.24
Nodes (11): Color Management, Drawing Tools (Brush, Eraser, Fill), Dual-Layer Canvas System, Haptic Feedback (JNI/macOS), PNG Import/Export, JavaFX Paint Application, Selection Controller, Selection Tool (+3 more)

### Community 23 - "Eyedropper Tool"
Cohesion: 0.44
Nodes (3): GraphicsContext, Override, EyeDropperTool

### Community 24 - "Passive (Pan) Tool"
Cohesion: 0.36
Nodes (4): GraphicsContext, Override, ToolSpec, PassiveTool

### Community 25 - "Claude Code Settings"
Cohesion: 0.29
Nodes (6): enabledPlugins, frontend-design@claude-plugins-official, env, CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS, hooks, PreToolUse

### Community 26 - "Eyedropper Icon Assets"
Cohesion: 0.43
Nodes (7): Eyedropper Body Path (diagonal barrel shape), Eyedropper Handle/Dropper Tip Path, Eyedropper Tool Icon, SVG Vector Graphic (24x24 viewBox), Eyedropper Cross-line Detail Path, Color Picker / Eyedropper Tool Concept, White Stroke Style

### Community 27 - "App Launch Entry"
Cohesion: 0.36
Nodes (4): Application, Main, Stage, String

### Community 28 - "Fill Tool Icon Assets"
Cohesion: 0.40
Nodes (6): Diamond / Rhombus Shape (bucket body outline), Paint Drip / Drop Element, Fill Tool Concept (flood fill / bucket fill), Paint Bucket / Fill Tool Icon, SVG Vector Asset for Paint App UI, FillTool Java Class

### Community 29 - "Pencil Tool Icon Assets"
Cohesion: 0.33
Nodes (6): Pencil Body Path (diagonal shaft and tip), Pencil Highlight/Glint Line, Pencil Tool Icon, PencilTool (Paint App), SVG Vector Graphic, Pencil Drawing Tool

### Community 30 - "App Icon Assets"
Cohesion: 0.60
Nodes (5): Dark Rounded Rectangle Background, Primary Colors (Red, Blue, Yellow) on Bristles, App Icon - Paint Brush, Paint Brush Visual Element, Digital Painting / Drawing Application Concept

### Community 31 - "Brush Tool Icon Assets"
Cohesion: 0.60
Nodes (5): Paint Brush Tool Concept, White Outline Icon Style, Brush Tool Icon (SVG), Brush Body Path, Brush Detail Strokes

### Community 32 - "Eraser Tool Icon Assets"
Cohesion: 0.40
Nodes (5): Eraser Tool Icon, Eraser Baseline / Tip Line, Eraser Body Path (diagonal parallelogram shape), SVG Vector Graphic (24x24 viewBox), Eraser Tool (Paint Application Feature)

### Community 33 - "Selection Tool Icon Assets"
Cohesion: 0.70
Nodes (5): Center Dot (crosshair reference point), Dashed Rectangle Border (marching ants selection outline), Selection Tool Icon, Paint Application Tool Panel, Rectangular Selection Tool

### Community 34 - "Hand Tool Icon Assets"
Cohesion: 0.67
Nodes (4): Hand Tool Icon (SVG), Paint Application, Hand Tool (Pan/Navigate Canvas), Hand/Finger Shape Visual Element

### Community 35 - "Zoom Tool Icon Assets"
Cohesion: 0.83
Nodes (4): Zoom Tool Icon (SVG), Magnifying Glass Shape, Plus Symbol Inside Lens, Zoom In Concept

### Community 37 - "Architecture Patterns"
Cohesion: 1.00
Nodes (3): java-architect Agent, Clean Architecture, SOLID Principles

### Community 50 - "Community 50"
Cohesion: 0.25
Nodes (8): paint-orchestrator agent, ui-designer agent, Float selection lifecycle (IDLE>DEFINING>FLOATING>commit/cancel), ResizeHandle(), Selection, SelectionController, SelectionInteractionMode, SelectionOrigin

### Community 51 - "Community 51"
Cohesion: 0.19
Nodes (8): Circle, Consumer, Runnable, getCursor(), ResizeDragHandler, Rectangle2D, ResizeHandle, Runnable

### Community 52 - "Community 52"
Cohesion: 0.26
Nodes (3): GraphicsContext, Override, FillTool

### Community 55 - "Community 55"
Cohesion: 0.38
Nodes (10): CanvasManager, Override, String, Window, Supplier, apply(), button(), ClearAction() (+2 more)

### Community 56 - "Community 56"
Cohesion: 0.22
Nodes (8): 1. Architecture Analysis, 2. Implementation Phase, 3. Quality Verification, C++ Project Assessment, code:json ({), code:json ({), Communication Protocol, Development Workflow

### Community 57 - "Community 57"
Cohesion: 0.22
Nodes (8): 1. Issue Analysis, 2. Implementation Phase, 3. Resolution Excellence, code:json ({), code:json ({), Communication Protocol, Debugging Context, Development Workflow

### Community 58 - "Community 58"
Cohesion: 0.22
Nodes (8): 1. Architecture Analysis, 2. Implementation Phase, 3. Quality Assurance, code:json ({), code:json ({), Communication Protocol, Development Workflow, Java Project Assessment

### Community 59 - "Community 59"
Cohesion: 0.22
Nodes (8): 1. Performance Analysis, 2. Implementation Phase, 3. Performance Excellence, code:json ({), code:json ({), Communication Protocol, Development Workflow, Performance Assessment

### Community 60 - "Community 60"
Cohesion: 0.22
Nodes (8): 1. Code Analysis, 2. Implementation Phase, 3. Code Excellence, code:json ({), code:json ({), Communication Protocol, Development Workflow, Refactoring Context Assessment

### Community 61 - "Community 61"
Cohesion: 0.47
Nodes (5): File, FileManager, CanvasManager, Stage, String

### Community 62 - "Community 62"
Cohesion: 0.44
Nodes (6): anchorX(), anchorY(), handleX(), handleY(), Rectangle2D, ResizeHandle

### Community 63 - "Community 63"
Cohesion: 0.25
Nodes (4): SelectionOverlay, SnapshotParameters, CanvasManager, GraphicsContext

### Community 65 - "Community 65"
Cohesion: 0.47
Nodes (3): Layer, Rectangle2D, WritableImage

### Community 67 - "Community 67"
Cohesion: 0.50
Nodes (3): Available subagents and when to delegate to them, Project structure, Workflow

## Knowledge Gaps
- **96 isolated node(s):** `update_icon.sh script`, `frontend-design@claude-plugins-official`, `PreToolUse`, `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS`, `allow` (+91 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **14 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CanvasManager` connect `Canvas Tab & Options Bar` to `Community 66`, `Canvas Compositing Engine`, `Layer Model & Properties`, `Menu Bar & Shortcuts`, `Color History Panel`, `Community 50`, `Status Bar & Zoom`, `Tool Manager`, `Community 53`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Why does `SelectionController` connect `Community 50` to `Selection & Clipboard System`, `Community 64`, `Layer Model & Properties`, `Canvas Tab & Options Bar`, `Community 51`, `Community 53`, `Community 54`, `Community 62`, `Community 63`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **Why does `Tool` connect `Tool Interface Contract` to `Color State Management`, `Drawing Tool Base Classes`, `File I/O & PNG Export`, `Tool Selection Panel`, `Community 52`, `Tool Manager`, `Tool Specification Enum`, `Eyedropper Tool`, `Passive (Pan) Tool`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `CanvasManager` (e.g. with `Undo/Redo via snapshot stacks` and `SRC_OVER pixel compositing`) actually correct?**
  _`CanvasManager` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `update_icon.sh script`, `frontend-design@claude-plugins-official`, `PreToolUse` to the rest of the system?**
  _102 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Layer Management & Undo` be split into smaller, more focused modules?**
  _Cohesion score 0.05821917808219178 - nodes in this community are weakly interconnected._
- **Should `Color State Management` be split into smaller, more focused modules?**
  _Cohesion score 0.08974358974358974 - nodes in this community are weakly interconnected._