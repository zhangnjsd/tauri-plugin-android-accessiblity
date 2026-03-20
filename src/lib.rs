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
use desktop::AndroidAccessiblity;
#[cfg(mobile)]
use mobile::AndroidAccessiblity;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the android-accessiblity APIs.
pub trait AndroidAccessiblityExt<R: Runtime> {
  fn android_accessiblity(&self) -> &AndroidAccessiblity<R>;
}

impl<R: Runtime, T: Manager<R>> crate::AndroidAccessiblityExt<R> for T {
  fn android_accessiblity(&self) -> &AndroidAccessiblity<R> {
    self.state::<AndroidAccessiblity<R>>().inner()
  }
}

/// Initializes the plugin.
pub fn init<R: Runtime>() -> TauriPlugin<R> {
  Builder::new("android-accessiblity")
    .invoke_handler(tauri::generate_handler![
      commands::ping,
      commands::check_accessibility_enabled,
      commands::open_accessibility_settings,
      commands::get_frontmost_ui_tree,
      commands::click_node
    ])
    .setup(|app, api| {
      #[cfg(mobile)]
      let android_accessiblity = mobile::init(app, api)?;
      #[cfg(desktop)]
      let android_accessiblity = desktop::init(app, api)?;
      app.manage(android_accessiblity);
      Ok(())
    })
    .build()
}
