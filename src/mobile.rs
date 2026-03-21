use serde::de::DeserializeOwned;
use tauri::{
  plugin::{PluginApi, PluginHandle},
  AppHandle, Runtime,
};

use crate::models::*;

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_android_accessiblity);

// initializes the Kotlin or Swift plugin classes
pub fn init<R: Runtime, C: DeserializeOwned>(
  _app: &AppHandle<R>,
  api: PluginApi<R, C>,
) -> crate::Result<AndroidAccessiblity<R>> {
  #[cfg(target_os = "android")]
  let handle = api.register_android_plugin(
    "com.tauri.plugin.androidaccessiblity",
    "AndroidAccessiblityPlugin",
  )?;
  #[cfg(target_os = "ios")]
  let handle = api.register_ios_plugin(init_plugin_android_accessiblity)?;
  Ok(AndroidAccessiblity(handle))
}

/// Access to the android-accessiblity APIs.
pub struct AndroidAccessiblity<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> AndroidAccessiblity<R> {
  pub fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
    self
      .0
      .run_mobile_plugin("ping", payload)
      .map_err(Into::into)
  }

  pub fn check_accessibility_enabled(&self) -> crate::Result<AccessibilityPermissionStatus> {
    self
      .0
      .run_mobile_plugin("checkAccessibilityEnabled", ())
      .map_err(Into::into)
  }

  pub fn open_accessibility_settings(&self) -> crate::Result<OpenSettingsResponse> {
    self
      .0
      .run_mobile_plugin("openAccessibilitySettings", ())
      .map_err(Into::into)
  }

  pub fn get_frontmost_ui_tree(&self, payload: UiTreeRequest) -> crate::Result<UiTreeResponse> {
    self
      .0
      .run_mobile_plugin("getFrontmostUiTree", payload)
      .map_err(Into::into)
  }

  pub fn click_node(&self, payload: ClickNodeRequest) -> crate::Result<ClickNodeResponse> {
    self
      .0
      .run_mobile_plugin("clickNode", payload)
      .map_err(Into::into)
  }

  pub fn perform_gesture(&self, payload: PerformGestureRequest) -> crate::Result<PerformGestureResponse> {
    self
      .0
      .run_mobile_plugin("performGesture", payload)
      .map_err(Into::into)
  }

  pub fn perform_global_action(&self, payload: GlobalActionRequest) -> crate::Result<GlobalActionResponse> {
    self
      .0
      .run_mobile_plugin("performGlobalAction", payload)
      .map_err(Into::into)
  }

  pub fn perform_node_action(&self, payload: NodeActionRequest) -> crate::Result<NodeActionResponse> {
    self
      .0
      .run_mobile_plugin("performNodeAction", payload)
      .map_err(Into::into)
  }
}
