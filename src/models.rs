use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PingRequest {
  pub value: Option<String>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PingResponse {
  pub value: Option<String>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AccessibilityPermissionStatus {
  pub enabled: bool,
  pub service_id: String,
  pub enabled_services: Vec<String>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UiTreeRequest {
  pub max_depth: Option<u32>,
  pub max_children_per_node: Option<u32>,
  pub include_non_clickable: Option<bool>,
}

impl Default for UiTreeRequest {
  fn default() -> Self {
    Self {
      max_depth: Some(10),
      max_children_per_node: Some(50),
      include_non_clickable: Some(true),
    }
  }
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Bounds {
  pub left: i32,
  pub top: i32,
  pub right: i32,
  pub bottom: i32,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UiNode {
  pub node_id: String,
  pub class_name: Option<String>,
  pub text: Option<String>,
  pub content_description: Option<String>,
  pub resource_id: Option<String>,
  pub package_name: Option<String>,
  pub bounds: Option<Bounds>,
  pub clickable: bool,
  pub enabled: bool,
  pub focusable: bool,
  pub focused: bool,
  pub scrollable: bool,
  pub selected: bool,
  pub checkable: bool,
  pub checked: bool,
  pub long_clickable: bool,
  pub children: Vec<UiNode>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UiTreeResponse {
  pub timestamp_ms: u64,
  pub package_name: Option<String>,
  pub root: Option<UiNode>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ClickNodeRequest {
  pub node_id: String,
  pub action: Option<String>,
  pub fallback_to_clickable_parent: Option<bool>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ClickNodeResponse {
  pub success: bool,
  pub performed_on_node_id: Option<String>,
  pub message: Option<String>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct OpenSettingsResponse {
  pub opened: bool,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct GesturePoint {
  pub x: f32,
  pub y: f32,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct GestureStroke {
  pub points: Vec<GesturePoint>,
  pub start_time_ms: Option<u64>,
  pub duration_ms: u64,
  pub will_continue: Option<bool>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PerformGestureRequest {
  pub strokes: Vec<GestureStroke>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PerformGestureResponse {
  pub success: bool,
  pub message: Option<String>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct GlobalActionRequest {
  pub action: String,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct GlobalActionResponse {
  pub success: bool,
  pub message: Option<String>,
}

#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct NodeActionRequest {
  pub node_id: String,
  pub action: String,
  pub fallback_to_scrollable_parent: Option<bool>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct NodeActionResponse {
  pub success: bool,
  pub performed_on_node_id: Option<String>,
  pub message: Option<String>,
}
