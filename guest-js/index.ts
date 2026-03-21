import { invoke } from '@tauri-apps/api/core'

export async function ping(value: string): Promise<string | null> {
  return await invoke<{value?: string}>('plugin:android-accessibility|ping', {
    payload: {
      value,
    },
  }).then((r) => (r.value ? r.value : null));
}

export interface AccessibilityPermissionStatus {
  enabled: boolean
  serviceId: string
  enabledServices: string[]
}

export interface Bounds {
  left: number
  top: number
  right: number
  bottom: number
}

export interface UiNode {
  nodeId: string
  className?: string | null
  text?: string | null
  contentDescription?: string | null
  resourceId?: string | null
  packageName?: string | null
  bounds?: Bounds | null
  clickable: boolean
  enabled: boolean
  focusable: boolean
  focused: boolean
  scrollable: boolean
  selected: boolean
  checkable: boolean
  checked: boolean
  longClickable: boolean
  children: UiNode[]
}

export interface UiTreeRequest {
  maxDepth?: number
  maxChildrenPerNode?: number
  includeNonClickable?: boolean
}

export interface UiTreeResponse {
  timestampMs: number
  packageName?: string | null
  root?: UiNode | null
}

export interface ClickNodeRequest {
  nodeId: string
  action?: 'click' | 'longClick' | 'focus'
  fallbackToClickableParent?: boolean
}

export interface ClickNodeResponse {
  success: boolean
  performedOnNodeId?: string | null
  message?: string | null
}

export interface OpenSettingsResponse {
  opened: boolean
}

export interface GesturePoint {
  x: number
  y: number
}

export interface GestureStroke {
  points: GesturePoint[]
  startTimeMs?: number
  durationMs: number
  willContinue?: boolean
}

export interface PerformGestureRequest {
  strokes: GestureStroke[]
}

export interface PerformGestureResponse {
  success: boolean
  message?: string | null
}

export type GlobalActionType =
  | 'back'
  | 'home'
  | 'recents'
  | 'notifications'
  | 'quickSettings'
  | 'powerDialog'
  | 'lockScreen'
  | 'takeScreenshot'

export interface GlobalActionRequest {
  action: GlobalActionType
}

export interface GlobalActionResponse {
  success: boolean
  message?: string | null
}

export type NodeActionType =
  | 'click'
  | 'longClick'
  | 'focus'
  | 'clearFocus'
  | 'select'
  | 'clearSelection'
  | 'scrollForward'
  | 'scrollBackward'

export interface NodeActionRequest {
  nodeId: string
  action: NodeActionType
  fallbackToScrollableParent?: boolean
}

export interface NodeActionResponse {
  success: boolean
  performedOnNodeId?: string | null
  message?: string | null
}

export async function checkAccessibilityEnabled(): Promise<AccessibilityPermissionStatus> {
  return await invoke<AccessibilityPermissionStatus>('plugin:android-accessibility|check_accessibility_enabled')
}

export async function openAccessibilitySettings(): Promise<OpenSettingsResponse> {
  return await invoke<OpenSettingsResponse>('plugin:android-accessibility|open_accessibility_settings')
}

export async function getFrontmostUiTree(payload: UiTreeRequest = {}): Promise<UiTreeResponse> {
  return await invoke<UiTreeResponse>('plugin:android-accessibility|get_frontmost_ui_tree', {
    payload,
  })
}

export async function clickNode(payload: ClickNodeRequest): Promise<ClickNodeResponse> {
  return await invoke<ClickNodeResponse>('plugin:android-accessibility|click_node', {
    payload,
  })
}

export async function performGesture(payload: PerformGestureRequest): Promise<PerformGestureResponse> {
  return await invoke<PerformGestureResponse>('plugin:android-accessibility|perform_gesture', {
    payload,
  })
}

export async function performGlobalAction(payload: GlobalActionRequest): Promise<GlobalActionResponse> {
  return await invoke<GlobalActionResponse>('plugin:android-accessibility|perform_global_action', {
    payload,
  })
}

export async function performNodeAction(payload: NodeActionRequest): Promise<NodeActionResponse> {
  return await invoke<NodeActionResponse>('plugin:android-accessibility|perform_node_action', {
    payload,
  })
}
