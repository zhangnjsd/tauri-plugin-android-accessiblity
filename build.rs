const COMMANDS: &[&str] = &[
  "ping",
  "check_accessibility_enabled",
  "open_accessibility_settings",
  "get_frontmost_ui_tree",
  "click_node",
  "perform_gesture",
  "perform_global_action",
  "perform_node_action",
  "type_text",
];

fn main() {
  tauri_plugin::Builder::new(COMMANDS)
    .android_path("android")
    .ios_path("ios")
    .build();
}
