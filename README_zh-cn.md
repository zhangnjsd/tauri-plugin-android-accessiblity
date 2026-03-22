# Tauri Plugin Android Accessibility

基于 Tauri v2 的移动端插件，用于 Android 无障碍能力桥接，提供以下能力：

- 检查无障碍服务是否已启用
- 跳转系统无障碍设置页
- 获取当前前台窗口的 UI 树快照
- 按节点 ID 执行点击/长按/聚焦（支持回退到可点击父节点）
- 手势模拟（点击、长按、滑动、拖拽、多指）
- 系统级动作（返回、主页、最近任务、通知栏等）
- 节点通用动作（滚动、焦点、选择等）

## 1. 功能说明

本插件面向 Android 端，由 Kotlin 无障碍服务和 Rust/JS 桥接组成。

- Rust 插件名：android-accessibility
- Kotlin 插件类：AndroidAccessibilityPlugin
- Kotlin 无障碍服务：TauriAccessibilityService

实现入口：

- Rust 命令定义在 [src/commands.rs](src/commands.rs)
- 移动端桥接在 [src/mobile.rs](src/mobile.rs)
- Android 原生实现在 [android/src/main/java/com/tauri/plugin/androidaccessibility/AndroidAccessibilityPlugin.kt](android/src/main/java/com/tauri/plugin/androidaccessibility/AndroidAccessibilityPlugin.kt)
- 无障碍服务在 [android/src/main/java/com/tauri/plugin/androidaccessibility/TauriAccessibilityService.kt](android/src/main/java/com/tauri/plugin/androidaccessibility/TauriAccessibilityService.kt)

## 2. 在 Tauri App 中使用

直接添加：

```bash
bun tauri add android-accessibility
```

或 按照以下方式手动添加：

在应用端注册插件：

```rust
tauri::Builder::default()
        .plugin(tauri_plugin_android_accessibility::init())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
```

JS 侧（建议用 bun 进行依赖安装与构建）：

```bash
bun install
bun run build
```

示例调用：

```ts
import {
        checkAccessibilityEnabled,
        openAccessibilitySettings,
        getFrontmostUiTree,
        clickNode,
        performGesture,
        performGlobalAction,
        performNodeAction,
} from 'tauri-plugin-android-accessibility-api'

const status = await checkAccessibilityEnabled()
if (!status.enabled) {
        await openAccessibilitySettings()
}

const tree = await getFrontmostUiTree({
        maxDepth: 8,
        maxChildrenPerNode: 40,
        includeNonClickable: true,
})

await clickNode({
        nodeId: '0.1.2',
        action: 'click',
        fallbackToClickableParent: true,
})

await performGesture({
        strokes: [
                {
                        points: [
                                { x: 540, y: 1500 },
                                { x: 540, y: 700 },
                        ],
                        durationMs: 320,
                },
        ],
})

await performGlobalAction({ action: 'back' })

await performNodeAction({
        nodeId: '0.1.0',
        action: 'scrollForward',
        fallbackToScrollableParent: true,
})
```

## 3. API 列表

### `checkAccessibilityEnabled`

返回：

- `enabled`: 当前应用的无障碍服务是否启用
- `serviceId`: 当前服务组件 ID
- `enabledServices`: 系统当前已启用的服务列表

### `openAccessibilitySettings`

打开系统无障碍设置页，返回 `opened`。

### `getFrontmostUiTree`

参数：

- `maxDepth`: 树深限制
- `maxChildrenPerNode`: 每节点最大子节点数量
- `includeNonClickable`: 是否包含不可点击叶子节点

返回：

- `timestampMs`
- `packageName`
- `root`（递归 UI 节点）

### `clickNode`

参数：

- `nodeId`: UI 树中的节点路径 ID（示例 0.1.2）
- `action`: click | longClick | focus
- `fallbackToClickableParent`: 目标失败时是否回退父节点 click

返回：

- `success`
- `performedOnNodeId`
- `message`

### `performGesture`

参数：

- `strokes`: 手势轨迹数组
- `strokes[].points`: 路径点数组，`[{ x, y }, ...]`
- `strokes[].startTimeMs`: 相对起始时间（可选）
- `strokes[].durationMs`: 手势持续时间（毫秒）
- `strokes[].willContinue`: 是否续接手势（可选）

说明：

- 点击：传 1 个点 + 短 duration
- 长按：传 1 个点 + 长 duration
- 滑动/拖拽：传 2 个或更多点
- 多指：一次请求中传多条 stroke

返回：

- `success`
- `message`

### `performGlobalAction`

参数：

- `action`: `back` | `home` | `recents` | `notifications` | `quickSettings` | `powerDialog` | `lockScreen` | `takeScreenshot`

返回：

- `success`
- `message`

### `performNodeAction`

参数：

- `nodeId`: UI 树节点路径 ID
- `action`: `click` | `longClick` | `focus` | `clearFocus` | `select` | `clearSelection` | `scrollForward` | `scrollBackward`
- `fallbackToScrollableParent`: 滚动类动作失败时是否回退到可滚动父节点

返回：

- `success`
- `performedOnNodeId`
- `message`

### `typeText`

Parameters:

- `nodeId`: 文本框的 Node ID
- `text`: 要输入的文字
  
Returns:

- `success`
- `message`

## 4. Android 规范与限制

- 无障碍服务属于高敏感能力，必须由用户在系统设置中手动授权。
- 本插件不会尝试绕过系统授权流程，不会静默开启无障碍。
- 对前台应用 UI 树读取依赖系统可见窗口和 Android 版本行为，结果可能为空。
- 节点点击受目标应用防护策略、动态 UI 状态、可访问性标记影响，不保证 100% 成功。
- 上架商店前请明确告知用户采集范围和用途，遵循应用市场隐私与可访问性政策。

## 5. 权限与清单

- 无障碍服务声明位于 [android/src/main/AndroidManifest.xml](android/src/main/AndroidManifest.xml)
- 服务配置位于 [android/src/main/res/xml/tauri_accessibility_service_config.xml](android/src/main/res/xml/tauri_accessibility_service_config.xml)
- Tauri 插件命令权限默认集位于 [permissions/default.toml](permissions/default.toml)
