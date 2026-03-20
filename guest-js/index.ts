import { invoke } from '@tauri-apps/api/core'

export async function ping(value: string): Promise<string | null> {
  return await invoke<{value?: string}>('plugin:android-accessiblity|ping', {
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

export async function checkAccessibilityEnabled(): Promise<AccessibilityPermissionStatus> {
  return await invoke<AccessibilityPermissionStatus>('plugin:android-accessiblity|check_accessibility_enabled')
}

export async function openAccessibilitySettings(): Promise<OpenSettingsResponse> {
  return await invoke<OpenSettingsResponse>('plugin:android-accessiblity|open_accessibility_settings')
}

export async function getFrontmostUiTree(payload: UiTreeRequest = {}): Promise<UiTreeResponse> {
  return await invoke<UiTreeResponse>('plugin:android-accessiblity|get_frontmost_ui_tree', {
    payload,
  })
}

export async function clickNode(payload: ClickNodeRequest): Promise<ClickNodeResponse> {
  return await invoke<ClickNodeResponse>('plugin:android-accessiblity|click_node', {
    payload,
  })
}
