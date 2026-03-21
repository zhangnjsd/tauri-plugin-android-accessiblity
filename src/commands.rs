use tauri::{AppHandle, command, Runtime};

use crate::models::*;
use crate::Result;
use crate::AndroidAccessiblityExt;

/**
 * Connectivity check
 * A simple command to test the plugin is working. It just echoes back the provided string.
 */
#[command]
pub(crate) async fn ping<R: Runtime>(
    app: AppHandle<R>,
    payload: PingRequest,
) -> Result<PingResponse> {
    app.android_accessiblity().ping(payload)
}

/**
 * Check if accessibility is enabled
 * This command checks whether the accessibility service is currently enabled on the device.
 */
#[command]
pub(crate) async fn check_accessibility_enabled<R: Runtime>(
    app: AppHandle<R>,
) -> Result<AccessibilityPermissionStatus> {
    app.android_accessiblity().check_accessibility_enabled()
}

/**
 * Open accessibility settings
 * This command opens the device's accessibility settings screen.
 */
#[command]
pub(crate) async fn open_accessibility_settings<R: Runtime>(
    app: AppHandle<R>,
) -> Result<OpenSettingsResponse> {
    app.android_accessiblity().open_accessibility_settings()
}

/**
 * Get frontmost UI tree
 * This command retrieves the UI tree of the currently frontmost app on the device.
 */
#[command]
pub(crate) async fn get_frontmost_ui_tree<R: Runtime>(
    app: AppHandle<R>,
    payload: UiTreeRequest,
) -> Result<UiTreeResponse> {
    app.android_accessiblity().get_frontmost_ui_tree(payload)
}

/**
 * Click node
 * This command performs a click action on a specified node in the UI tree.
 */
#[command]
pub(crate) async fn click_node<R: Runtime>(
    app: AppHandle<R>,
    payload: ClickNodeRequest,
) -> Result<ClickNodeResponse> {
    app.android_accessiblity().click_node(payload)
}

/**
 * Perform gesture
 * This command dispatches one or more gesture strokes (supports multi-touch).
 */
#[command]
pub(crate) async fn perform_gesture<R: Runtime>(
    app: AppHandle<R>,
    payload: PerformGestureRequest,
) -> Result<PerformGestureResponse> {
    app.android_accessiblity().perform_gesture(payload)
}

/**
 * Perform global action
 * This command triggers an Android global accessibility action.
 */
#[command]
pub(crate) async fn perform_global_action<R: Runtime>(
    app: AppHandle<R>,
    payload: GlobalActionRequest,
) -> Result<GlobalActionResponse> {
    app.android_accessiblity().perform_global_action(payload)
}

/**
 * Perform node action
 * This command performs a generic accessibility action on a specific node.
 */
#[command]
pub(crate) async fn perform_node_action<R: Runtime>(
    app: AppHandle<R>,
    payload: NodeActionRequest,
) -> Result<NodeActionResponse> {
    app.android_accessiblity().perform_node_action(payload)
}
