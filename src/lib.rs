use tauri::{
  plugin::{Builder, TauriPlugin},
  Manager, Runtime,
};

pub use models::*;

#[cfg(desktop)]
mod desktop;
#[cfg(mobile)]
mod mobile;

mod commands;
mod error;
mod models;

pub use error::{Error, Result};

#[cfg(desktop)]
use desktop::AndroidAccessibility;
#[cfg(mobile)]
use mobile::AndroidAccessibility;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the android-accessibility APIs.
pub trait AndroidAccessibilityExt<R: Runtime> {
  fn android_accessibility(&self) -> &AndroidAccessibility<R>;
}

impl<R: Runtime, T: Manager<R>> crate::AndroidAccessibilityExt<R> for T {
  fn android_accessibility(&self) -> &AndroidAccessibility<R> {
    self.state::<AndroidAccessibility<R>>().inner()
  }
}

/// Initializes the plugin.
pub fn init<R: Runtime>() -> TauriPlugin<R> {
  Builder::new("android-accessibility")
    .invoke_handler(tauri::generate_handler![
      commands::ping,
      commands::check_accessibility_enabled,
      commands::open_accessibility_settings,
      commands::get_frontmost_ui_tree,
      commands::click_node,
      commands::perform_gesture,
      commands::perform_global_action,
      commands::perform_node_action
    ])
    .setup(|app, api| {
      #[cfg(mobile)]
      let android_accessibility = mobile::init(app, api)?;
      #[cfg(desktop)]
      let android_accessibility = desktop::init(app, api)?;
      app.manage(android_accessibility);
      Ok(())
    })
    .build()
}
