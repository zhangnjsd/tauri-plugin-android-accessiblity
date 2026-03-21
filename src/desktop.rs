use serde::de::DeserializeOwned;
use tauri::{plugin::PluginApi, AppHandle, Runtime};

use crate::models::*;

pub fn init<R: Runtime, C: DeserializeOwned>(
  app: &AppHandle<R>,
  _api: PluginApi<R, C>,
) -> crate::Result<AndroidAccessiblity<R>> {
  Ok(AndroidAccessiblity(app.clone()))
}

/// Access to the android-accessiblity APIs.
pub struct AndroidAccessiblity<R: Runtime>(AppHandle<R>);

impl<R: Runtime> AndroidAccessiblity<R> {
  pub fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
    Ok(PingResponse {
      value: payload.value,
    })
  }

  pub fn check_accessibility_enabled(&self) -> crate::Result<AccessibilityPermissionStatus> {
    Ok(AccessibilityPermissionStatus {
      enabled: false,
      service_id: String::new(),
      enabled_services: Vec::new(),
    })
  }

  pub fn open_accessibility_settings(&self) -> crate::Result<OpenSettingsResponse> {
    Ok(OpenSettingsResponse { opened: false })
  }

  pub fn get_frontmost_ui_tree(&self, _payload: UiTreeRequest) -> crate::Result<UiTreeResponse> {
    Ok(UiTreeResponse {
      timestamp_ms: 0,
      package_name: None,
      root: None,
    })
  }

  pub fn click_node(&self, _payload: ClickNodeRequest) -> crate::Result<ClickNodeResponse> {
    Ok(ClickNodeResponse {
      success: false,
      performed_on_node_id: None,
      message: Some("Accessibility actions are only available on Android".to_string()),
    })
  }

  pub fn perform_gesture(&self, _payload: PerformGestureRequest) -> crate::Result<PerformGestureResponse> {
    Ok(PerformGestureResponse {
      success: false,
      message: Some("Accessibility actions are only available on Android".to_string()),
    })
  }

  pub fn perform_global_action(&self, _payload: GlobalActionRequest) -> crate::Result<GlobalActionResponse> {
    Ok(GlobalActionResponse {
      success: false,
      message: Some("Accessibility actions are only available on Android".to_string()),
    })
  }

  pub fn perform_node_action(&self, _payload: NodeActionRequest) -> crate::Result<NodeActionResponse> {
    Ok(NodeActionResponse {
      success: false,
      performed_on_node_id: None,
      message: Some("Accessibility actions are only available on Android".to_string()),
    })
  }
}
