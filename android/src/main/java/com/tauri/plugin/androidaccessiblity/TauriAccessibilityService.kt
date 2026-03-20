package com.tauri.plugin.androidaccessiblity

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
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

    val requestedAction = when (action) {
      "longClick" -> AccessibilityNodeInfo.ACTION_LONG_CLICK
      "focus" -> AccessibilityNodeInfo.ACTION_FOCUS
      else -> AccessibilityNodeInfo.ACTION_CLICK
    }

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
