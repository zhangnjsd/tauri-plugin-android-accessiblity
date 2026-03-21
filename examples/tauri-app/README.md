# Tauri Android Accessibility Example

This example app demonstrates all plugin APIs in one debug UI.

## What It Covers

- Service checks: `ping`, `checkAccessibilityEnabled`, `openAccessibilitySettings`
- UI tree and click action: `getFrontmostUiTree`, `clickNode`
- Gestures: `performGesture` (tap, swipe, two-finger pinch sample)
- Global actions: `performGlobalAction`
- Node actions: `performNodeAction` (including scroll actions)

## Run

From this folder:

```bash
bun install
bun tauri android dev
```

Then use the buttons in the page to trigger each method and inspect logs.

