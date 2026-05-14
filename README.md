A simple paint app made with JavaFX. This was made as a school project for coding class, will remain open source forever.

### Features
- **Drawing Tools**: Brush, Eraser, Bucket Fill.
- **Color Management**: Color picker and history.
- **Selection Tool**: Move, resize, cut, copy, and paste rectangular areas.
- **Undo/Redo**: Up to 50 states of history.
- **Haptic Feedback**: macOS-specific haptic feedback for tool switching and slider adjustments.
- **Import/Export**: Load and save drawings as PNG files.

### Architecture
- **Canvas Management**: Dual-layer system (background + drawing) with efficient alpha compositing.
- **Selection Controller**: Manages floating selection state and interactions.
- **Tool Manager**: Handles tool activation and lifecycle.
- **Haptic Feedback**: JNI wrapper for macOS haptic feedback patterns.
