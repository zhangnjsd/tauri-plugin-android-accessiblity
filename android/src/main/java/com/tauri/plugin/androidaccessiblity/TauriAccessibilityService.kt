package com.tauri.plugin.androidaccessiblity

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

class TauriAccessibilityService : AccessibilityService() {
  companion object {
    @Volatile
    private var instanceRef: WeakReference<TauriAccessibilityService>? = null

    fun currentInstance(): TauriAccessibilityService? = instanceRef?.get()
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    instanceRef = WeakReference(this)
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    // Intentionally no-op. Consumers pull snapshots on demand.
  }

  override fun onInterrupt() {
    // Intentionally no-op.
  }

  override fun onUnbind(intent: android.content.Intent?): Boolean {
    instanceRef = null
    return super.onUnbind(intent)
  }

  fun getUiTree(maxDepth: Int, maxChildrenPerNode: Int, includeNonClickable: Boolean): JSONObject {
    val ret = JSONObject()
    ret.put("timestampMs", System.currentTimeMillis())

    val root = rootInActiveWindow
    if (root == null) {
      ret.put("packageName", JSONObject.NULL)
      ret.put("root", JSONObject.NULL)
      return ret
    }

    ret.put("packageName", root.packageName?.toString())
    ret.put("root", serializeNode(root, "0", 0, maxDepth, maxChildrenPerNode, includeNonClickable))
    return ret
  }

  fun clickNode(nodeId: String, action: String, fallbackToClickableParent: Boolean): JSONObject {
    val ret = JSONObject()
    val root = rootInActiveWindow

    if (root == null) {
      ret.put("success", false)
      ret.put("performedOnNodeId", JSONObject.NULL)
      ret.put("message", "AccessibilityService has no active window")
      return ret
    }

    val target = findNodeById(root, nodeId)
    if (target == null) {
      ret.put("success", false)
      ret.put("performedOnNodeId", JSONObject.NULL)
      ret.put("message", "nodeId not found in active window")
      return ret
    }

    val requestedAction = mapNodeAction(action)

    var success = target.performAction(requestedAction)
    var message = if (success) "action performed" else "action failed on target node"

    if (!success && fallbackToClickableParent) {
      var parent = target.parent
      while (parent != null) {
        success = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (success) {
          message = "fallback click performed on clickable parent"
          break
        }
        parent = parent.parent
      }
    }

    ret.put("success", success)
    ret.put("performedOnNodeId", if (success) nodeId else JSONObject.NULL)
    ret.put("message", message)
    return ret
  }

  fun performGesture(strokes: JSONArray): JSONObject {
    val ret = JSONObject()
    if (strokes.length() == 0) {
      ret.put("success", false)
      ret.put("message", "strokes is empty")
      return ret
    }

    val builder = GestureDescription.Builder()
    for (i in 0 until strokes.length()) {
      val stroke = strokes.optJSONObject(i)
      if (stroke == null) {
        ret.put("success", false)
        ret.put("message", "invalid stroke at index $i")
        return ret
      }

      val points = stroke.optJSONArray("points")
      if (points == null || points.length() == 0) {
        ret.put("success", false)
        ret.put("message", "stroke.points is required and cannot be empty")
        return ret
      }

      val path = Path()
      val firstPoint = points.optJSONObject(0)
      if (firstPoint == null) {
        ret.put("success", false)
        ret.put("message", "invalid first point at stroke index $i")
        return ret
      }

      val firstX = firstPoint.optDouble("x", Double.NaN)
      val firstY = firstPoint.optDouble("y", Double.NaN)
      if (firstX.isNaN() || firstY.isNaN()) {
        ret.put("success", false)
        ret.put("message", "point x/y is required")
        return ret
      }

      path.moveTo(firstX.toFloat(), firstY.toFloat())
      for (j in 1 until points.length()) {
        val point = points.optJSONObject(j) ?: continue
        val x = point.optDouble("x", Double.NaN)
        val y = point.optDouble("y", Double.NaN)
        if (x.isNaN() || y.isNaN()) {
          ret.put("success", false)
          ret.put("message", "point x/y is required")
          return ret
        }
        path.lineTo(x.toFloat(), y.toFloat())
      }

      val startTimeMs = stroke.optLong("startTimeMs", 0L).coerceAtLeast(0L)
      val durationMs = stroke.optLong("durationMs", 1L).coerceAtLeast(1L)
      val willContinue = stroke.optBoolean("willContinue", false)
      builder.addStroke(GestureDescription.StrokeDescription(path, startTimeMs, durationMs, willContinue))
    }

    return try {
      val dispatched = dispatchGesture(builder.build(), null, null)
      ret.put("success", dispatched)
      ret.put("message", if (dispatched) "gesture dispatched" else "dispatchGesture returned false")
      ret
    } catch (e: Exception) {
      ret.put("success", false)
      ret.put("message", "gesture dispatch failed: ${e.message}")
      ret
    }
  }

  fun performGlobalActionByName(action: String): JSONObject {
    val ret = JSONObject()
    val mappedAction = mapGlobalAction(action)
    if (mappedAction == null) {
      ret.put("success", false)
      ret.put("message", "unsupported global action: $action")
      return ret
    }

    return try {
      val success = performGlobalAction(mappedAction)
      ret.put("success", success)
      ret.put("message", if (success) "global action performed" else "global action failed")
      ret
    } catch (e: Exception) {
      ret.put("success", false)
      ret.put("message", "global action failed: ${e.message}")
      ret
    }
  }

  fun performNodeAction(nodeId: String, action: String, fallbackToScrollableParent: Boolean): JSONObject {
    val ret = JSONObject()
    val root = rootInActiveWindow

    if (root == null) {
      ret.put("success", false)
      ret.put("performedOnNodeId", JSONObject.NULL)
      ret.put("message", "AccessibilityService has no active window")
      return ret
    }

    val target = findNodeById(root, nodeId)
    if (target == null) {
      ret.put("success", false)
      ret.put("performedOnNodeId", JSONObject.NULL)
      ret.put("message", "nodeId not found in active window")
      return ret
    }

    val requestedAction = mapNodeAction(action)
    var success = target.performAction(requestedAction)
    var message = if (success) "action performed" else "action failed on target node"

    if (!success && fallbackToScrollableParent && isScrollableAction(requestedAction)) {
      var parent = target.parent
      while (parent != null) {
        if (parent.isScrollable) {
          success = parent.performAction(requestedAction)
          if (success) {
            message = "fallback action performed on scrollable parent"
            break
          }
        }
        parent = parent.parent
      }
    }

    ret.put("success", success)
    ret.put("performedOnNodeId", if (success) nodeId else JSONObject.NULL)
    ret.put("message", message)
    return ret
  }

  private fun findNodeById(root: AccessibilityNodeInfo, nodeId: String): AccessibilityNodeInfo? {
    if (nodeId == "0") {
      return root
    }

    val parts = nodeId.split('.')
    if (parts.isEmpty() || parts.first() != "0") {
      return null
    }

    var current: AccessibilityNodeInfo? = root
    for (i in 1 until parts.size) {
      val childIndex = parts[i].toIntOrNull() ?: return null
      current = current?.getChild(childIndex) ?: return null
    }

    return current
  }

  private fun mapNodeAction(action: String): Int {
    return when (action) {
      "click" -> AccessibilityNodeInfo.ACTION_CLICK
      "longClick" -> AccessibilityNodeInfo.ACTION_LONG_CLICK
      "focus" -> AccessibilityNodeInfo.ACTION_FOCUS
      "clearFocus" -> AccessibilityNodeInfo.ACTION_CLEAR_FOCUS
      "select" -> AccessibilityNodeInfo.ACTION_SELECT
      "clearSelection" -> AccessibilityNodeInfo.ACTION_CLEAR_SELECTION
      "scrollForward" -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
      "scrollBackward" -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
      else -> AccessibilityNodeInfo.ACTION_CLICK
    }
  }

  private fun isScrollableAction(action: Int): Boolean {
    return action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD ||
      action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
  }

  private fun mapGlobalAction(action: String): Int? {
    return when (action) {
      "back" -> GLOBAL_ACTION_BACK
      "home" -> GLOBAL_ACTION_HOME
      "recents" -> GLOBAL_ACTION_RECENTS
      "notifications" -> GLOBAL_ACTION_NOTIFICATIONS
      "quickSettings" -> GLOBAL_ACTION_QUICK_SETTINGS
      "powerDialog" -> GLOBAL_ACTION_POWER_DIALOG
      "lockScreen" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) GLOBAL_ACTION_LOCK_SCREEN else null
      "takeScreenshot" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) GLOBAL_ACTION_TAKE_SCREENSHOT else null
      else -> null
    }
  }

  private fun serializeNode(
    node: AccessibilityNodeInfo,
    nodeId: String,
    depth: Int,
    maxDepth: Int,
    maxChildrenPerNode: Int,
    includeNonClickable: Boolean,
  ): JSONObject? {
    if (depth > maxDepth) {
      return null
    }

    val children = JSONArray()
    val childCount = minOf(node.childCount, maxChildrenPerNode)
    for (i in 0 until childCount) {
      val child = node.getChild(i) ?: continue
      val childJson = serializeNode(
        child,
        "$nodeId.$i",
        depth + 1,
        maxDepth,
        maxChildrenPerNode,
        includeNonClickable,
      )
      if (childJson != null) {
        children.put(childJson)
      }
    }

    val hasChildren = children.length() > 0
    if (!includeNonClickable && !node.isClickable && !hasChildren) {
      return null
    }

    val bounds = Rect()
    node.getBoundsInScreen(bounds)

    val ret = JSONObject()
    ret.put("nodeId", nodeId)
    ret.put("className", node.className?.toString() ?: JSONObject.NULL)
    ret.put("text", node.text?.toString() ?: JSONObject.NULL)
    ret.put("contentDescription", node.contentDescription?.toString() ?: JSONObject.NULL)
    ret.put("resourceId", node.viewIdResourceName ?: JSONObject.NULL)
    ret.put("packageName", node.packageName?.toString() ?: JSONObject.NULL)

    val boundsJson = JSONObject()
    boundsJson.put("left", bounds.left)
    boundsJson.put("top", bounds.top)
    boundsJson.put("right", bounds.right)
    boundsJson.put("bottom", bounds.bottom)
    ret.put("bounds", boundsJson)

    ret.put("clickable", node.isClickable)
    ret.put("enabled", node.isEnabled)
    ret.put("focusable", node.isFocusable)
    ret.put("focused", node.isFocused)
    ret.put("scrollable", node.isScrollable)
    ret.put("selected", node.isSelected)
    ret.put("checkable", node.isCheckable)
    ret.put("checked", node.isChecked)
    ret.put("longClickable", node.isLongClickable)
    ret.put("children", children)
    return ret
  }
}
