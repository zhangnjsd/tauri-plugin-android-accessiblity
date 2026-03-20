# Tauri Plugin Android Accessibility

[简体中文](./README_zh-cn.md)

A mobile plugin based on Tauri v2 for Android accessibility bridging, providing the following capabilities:

- Check if accessibility services are enabled
- Jump to the system accessibility settings page
- Get a UI tree snapshot of the current foreground window
- Perform click/long press/focus by node ID (supports falling back to a clickable parent node)

## 1. Functional Description

This plugin is designed for Android and consists of a Kotlin accessibility service and a Rust/JS bridge.

- Rust plugin name: android-accessibility
- Kotlin plugin class: AndroidAccessibilityPlugin
- Kotlin accessibility service: TauriAccessibilityService

Implementation entry points:

- Rust commands are defined in [src/commands.rs](src/commands.rs)
- Mobile bridging is in [src/mobile.rs](src/mobile.rs)
- Android native implementation is in [android/src/main/java/com/tauri/plugin/androidaccessibility/AndroidAccessibilityPlugin.kt](android/src/main/java/com/tauri/plugin/androidaccessibility/AndroidAccessibilityPlugin.kt)
- Accessibility service is in [android/src/main/java/com/tauri/plugin/androidaccessibility/TauriAccessibilityService.kt](android/src/main/java/com/tauri/plugin/androidaccessibility/TauriAccessibilityService.kt)

## 2. Usage in Tauri App

Register the plugin on the application side:

```rust
tauri::Builder::default()
        .plugin(tauri_plugin_android_accessibility::init())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
```

JS side (it is recommended to use bun for dependency installation and building):

```bash
bun install
bun run build
```

Example call:

```ts
import {
        checkAccessibilityEnabled,
        openAccessibilitySettings,
        getFrontmostUiTree,
        clickNode,
} from 'tauri-plugin-android-accessibility-api'

const status = await checkAccessibilityEnabled()
if (!status.enabled) {
        await openAccessibilitySettings()
}

const tree = await getFrontmostUiTree({
        maxDepth: 8,
        maxChildrenPerNode: 40,
        includeNonClickable: true,
})

await clickNode({
        nodeId: '0.1.2',
        action: 'click',
        fallbackToClickableParent: true,
})
```

## 3. API List

### checkAccessibilityEnabled

Returns:

- enabled: Whether the current app's accessibility service is enabled
- serviceId: Current service component ID
- enabledServices: List of accessibility services currently enabled by the system

### openAccessibilitySettings

Opens the system accessibility settings page, returns opened.

### getFrontmostUiTree

Parameters:

- maxDepth: Tree depth limit
- maxChildrenPerNode: Maximum number of child nodes per node
- includeNonClickable: Whether to include non-clickable leaf nodes

Returns:

- timestampMs
- packageName
- root (recursive UI node)

### clickNode

Parameters:

- nodeId: Node path ID in the UI tree (e.g., 0.1.2)
- action: click | longClick | focus
- fallbackToClickableParent: Whether to fall back to a parent node click if the target fails

Returns:

- success
- performedOnNodeId
- message

## 4. Android Specifications and Limitations

- Accessibility services are highly sensitive capabilities and must be manually authorized by the user in the system settings.
- This plugin does not attempt to bypass the system authorization process and will not silently enable accessibility.
- Reading the foreground app's UI tree depends on the system's visible windows and Android version behavior; results may be empty.
- Node clicks are affected by the target app's protection policies, dynamic UI states, and accessibility flags; 100% success is not guaranteed.
- Before submitting to an app store, please clearly inform users of the collection scope and purpose, and follow the app market's privacy and accessibility policies.

## 5. Permissions and Manifest

- Accessibility service declaration is located in [android/src/main/AndroidManifest.xml](android/src/main/AndroidManifest.xml)
- Service configuration is located in [android/src/main/res/xml/tauri_accessibility_service_config.xml](android/src/main/res/xml/tauri_accessibility_service_config.xml)
- The default set of Tauri plugin command permissions is located in [permissions/default.toml](permissions/default.toml)
