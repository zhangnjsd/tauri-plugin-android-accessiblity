// Learn more about Tauri commands at https://v2.tauri.app/develop/calling-rust/#commands
use tauri_plugin_android_accessiblity::{
    AndroidAccessiblityExt, ClickNodeRequest, UiTreeRequest,
};

#[tauri::command]
fn greet(name: &str) -> String {
    format!("Hello, {}! You've been greeted from Rust!", name)
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![greet])
        .plugin(tauri_plugin_android_accessiblity::init())
        .setup(|app| {
            let plugin = app.android_accessiblity();

            let ping = plugin.ping(tauri_plugin_android_accessiblity::PingRequest {
                value: Some("debug-ping".to_string()),
            });
            println!("[plugin-debug] ping => {:?}", ping);

            let enabled = plugin.check_accessibility_enabled();
            println!("[plugin-debug] check_accessibility_enabled => {:?}", enabled);

            let ui_tree = plugin.get_frontmost_ui_tree(UiTreeRequest::default());
            println!("[plugin-debug] get_frontmost_ui_tree => {:?}", ui_tree);

            let click = plugin.click_node(ClickNodeRequest {
                node_id: "0".to_string(),
                action: Some("click".to_string()),
                fallback_to_clickable_parent: Some(true),
            });
            println!("[plugin-debug] click_node => {:?}", click);

            let open_settings = plugin.open_accessibility_settings();
            println!("[plugin-debug] open_accessibility_settings => {:?}", open_settings);

            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
