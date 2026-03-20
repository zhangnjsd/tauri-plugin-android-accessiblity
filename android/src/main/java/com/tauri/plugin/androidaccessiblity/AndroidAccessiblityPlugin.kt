package com.tauri.plugin.androidaccessiblity

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import app.tauri.annotation.Command
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import org.json.JSONArray
import org.json.JSONObject

@TauriPlugin
class AndroidAccessiblityPlugin(private val activity: Activity) : Plugin(activity) {
  @Command
  fun ping(invoke: Invoke) {
    val args = invoke.getArgs()
    val value = args.getString("value", null)
    val ret = JSObject()
    ret.put("value", value ?: "pong")
    invoke.resolve(ret)
  }

  @Command
  fun checkAccessibilityEnabled(invoke: Invoke) {
    val enabledServicesRaw = Settings.Secure.getString(
      activity.contentResolver,
      Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: ""

    val enabledServices = enabledServicesRaw
      .split(':')
      .map { it.trim() }
      .filter { it.isNotEmpty() }

    val serviceId = "${activity.packageName}/${TauriAccessibilityService::class.java.name}"
    val isEnabled = enabledServices.any { it.equals(serviceId, ignoreCase = true) }

    val ret = JSObject()
    ret.put("enabled", isEnabled)
    ret.put("serviceId", serviceId)
    ret.put("enabledServices", JSONArray(enabledServices))
    invoke.resolve(ret)
  }

  @Command
  fun openAccessibilitySettings(invoke: Invoke) {
    val ret = JSObject()
    try {
      val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      activity.startActivity(intent)
      ret.put("opened", true)
      invoke.resolve(ret)
    } catch (e: Exception) {
      invoke.reject("Failed to open accessibility settings: ${e.message}")
    }
  }

  @Command
  fun getFrontmostUiTree(invoke: Invoke) {
    val args = invoke.getArgs()
    val maxDepth = args.getInteger("maxDepth", 10)
    val maxChildrenPerNode = args.getInteger("maxChildrenPerNode", 50)
    val includeNonClickable = args.getBoolean("includeNonClickable", true)

    val service = TauriAccessibilityService.currentInstance()
    if (service == null) {
      val ret = JSObject()
      ret.put("timestampMs", System.currentTimeMillis())
      ret.put("packageName", JSONObject.NULL)
      ret.put("root", JSONObject.NULL)
      invoke.resolve(ret)
      return
    }

    val ret = service.getUiTree(maxDepth, maxChildrenPerNode, includeNonClickable)
    invoke.resolve(JSObject.fromJSONObject(ret))
  }

  @Command
  fun clickNode(invoke: Invoke) {
    val args = invoke.getArgs()
    val nodeId = args.getString("nodeId", null)
    if (nodeId.isNullOrBlank()) {
      invoke.reject("nodeId is required")
      return
    }

    val action = args.getString("action", "click") ?: "click"
    val fallbackToClickableParent = args.getBoolean("fallbackToClickableParent", true)

    val service = TauriAccessibilityService.currentInstance()
    if (service == null) {
      val ret = JSObject()
      ret.put("success", false)
      ret.put("performedOnNodeId", JSONObject.NULL)
      ret.put("message", "Accessibility service is not running or not enabled")
      invoke.resolve(ret)
      return
    }

    val ret = service.clickNode(nodeId, action, fallbackToClickableParent)
    invoke.resolve(JSObject.fromJSONObject(ret))
  }
}
