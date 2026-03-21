<script>
  import {
    ping,
    checkAccessibilityEnabled,
    openAccessibilitySettings,
    getFrontmostUiTree,
    clickNode,
    performGesture,
    performGlobalAction,
    performNodeAction,
  } from '../../../dist-js/index.js'

  let logs = $state([])
  let nodeId = $state('0')
  let maxDepth = $state(6)
  let maxChildrenPerNode = $state(30)
  let includeNonClickable = $state(true)
  let globalAction = $state('back')

  let tapX = $state(540)
  let tapY = $state(1200)
  let tapDurationMs = $state(80)
  let swipeStartX = $state(540)
  let swipeStartY = $state(1400)
  let swipeEndX = $state(540)
  let swipeEndY = $state(700)
  let swipeDurationMs = $state(320)

  let nodeAction = $state('scrollForward')
  let fallbackToScrollableParent = $state(true)

  function now() {
    return new Date().toLocaleTimeString()
  }

  function pushLog(label, payload) {
    const text = typeof payload === 'string' ? payload : JSON.stringify(payload)
    logs = [`[${now()}] ${label}: ${text}`, ...logs].slice(0, 200)
  }

  function normalizeError(err) {
    if (typeof err === 'string') {
      return err
    }
    if (err && typeof err === 'object') {
      return JSON.stringify(err)
    }
    return String(err)
  }

  async function runCase(label, task) {
    pushLog(label, 'running')
    try {
      const result = await task()
      pushLog(label, result)
      return result
    } catch (err) {
      const message = normalizeError(err)
      pushLog(label, `ERROR ${message}`)
      return null
    }
  }

  async function testPing() {
    return runCase('ping', () => ping('Pong!'))
  }

  async function testCheckAccessibilityEnabled() {
    return runCase('checkAccessibilityEnabled', () => checkAccessibilityEnabled())
  }

  async function testOpenAccessibilitySettings() {
    return runCase('openAccessibilitySettings', () => openAccessibilitySettings())
  }

  async function testGetFrontmostUiTree() {
    return runCase('getFrontmostUiTree', () =>
      getFrontmostUiTree({
        maxDepth,
        maxChildrenPerNode,
        includeNonClickable,
      }),
    )
  }

  async function testClickNode() {
    return runCase('clickNode', () =>
      clickNode({
        nodeId,
        action: 'click',
        fallbackToClickableParent: true,
      }),
    )
  }

  async function testPerformTapGesture() {
    return runCase('performGesture.tap', () =>
      performGesture({
        strokes: [
          {
            points: [{ x: tapX, y: tapY }],
            durationMs: tapDurationMs,
          },
        ],
      }),
    )
  }

  async function testPerformSwipeGesture() {
    return runCase('performGesture.swipe', () =>
      performGesture({
        strokes: [
          {
            points: [
              { x: swipeStartX, y: swipeStartY },
              { x: swipeEndX, y: swipeEndY },
            ],
            durationMs: swipeDurationMs,
          },
        ],
      }),
    )
  }

  async function testPerformPinchGesture() {
    return runCase('performGesture.pinch', () =>
      performGesture({
        strokes: [
          {
            points: [
              { x: 420, y: 1200 },
              { x: 520, y: 1200 },
            ],
            durationMs: 260,
          },
          {
            points: [
              { x: 660, y: 1200 },
              { x: 560, y: 1200 },
            ],
            durationMs: 260,
          },
        ],
      }),
    )
  }

  async function testPerformGlobalAction() {
    return runCase('performGlobalAction', () =>
      performGlobalAction({
        action: globalAction,
      }),
    )
  }

  async function testPerformNodeAction() {
    return runCase('performNodeAction', () =>
      performNodeAction({
        nodeId,
        action: nodeAction,
        fallbackToScrollableParent,
      }),
    )
  }

  async function runAll() {
    pushLog('runAll', 'start')
    await testPing()
    await testCheckAccessibilityEnabled()
    await testGetFrontmostUiTree()
    await testClickNode()
    await testPerformNodeAction()
    await testPerformGlobalAction()
    await testOpenAccessibilitySettings()
    pushLog('runAll', 'done')
  }
</script>

<main class="container">
  <h1>Android Accessibility Plugin Debugger</h1>

  <div class="controls">
    <div class="line">
      <label for="maxDepth">maxDepth</label>
      <input id="maxDepth" type="number" min="1" max="30" bind:value={maxDepth} />

      <label for="maxChildrenPerNode">maxChildrenPerNode</label>
      <input
        id="maxChildrenPerNode"
        type="number"
        min="1"
        max="200"
        bind:value={maxChildrenPerNode}
      />

      <label for="includeNonClickable" class="checkbox">
        <input id="includeNonClickable" type="checkbox" bind:checked={includeNonClickable} />
        includeNonClickable
      </label>
    </div>

    <div class="line">
      <label for="nodeId">nodeId</label>
      <input id="nodeId" type="text" bind:value={nodeId} placeholder="0.1.2" />
    </div>

    <div class="line">
      <label for="globalAction">globalAction</label>
      <select id="globalAction" bind:value={globalAction}>
        <option value="back">back</option>
        <option value="home">home</option>
        <option value="recents">recents</option>
        <option value="notifications">notifications</option>
        <option value="quickSettings">quickSettings</option>
        <option value="powerDialog">powerDialog</option>
        <option value="lockScreen">lockScreen</option>
        <option value="takeScreenshot">takeScreenshot</option>
      </select>

      <label for="nodeAction">nodeAction</label>
      <select id="nodeAction" bind:value={nodeAction}>
        <option value="click">click</option>
        <option value="longClick">longClick</option>
        <option value="focus">focus</option>
        <option value="clearFocus">clearFocus</option>
        <option value="select">select</option>
        <option value="clearSelection">clearSelection</option>
        <option value="scrollForward">scrollForward</option>
        <option value="scrollBackward">scrollBackward</option>
      </select>

      <label for="fallbackToScrollableParent" class="checkbox">
        <input
          id="fallbackToScrollableParent"
          type="checkbox"
          bind:checked={fallbackToScrollableParent}
        />
        fallbackToScrollableParent
      </label>
    </div>

    <div class="line">
      <label for="tapX">tapX</label>
      <input id="tapX" type="number" bind:value={tapX} />
      <label for="tapY">tapY</label>
      <input id="tapY" type="number" bind:value={tapY} />
      <label for="tapDurationMs">tapDurationMs</label>
      <input id="tapDurationMs" type="number" min="1" bind:value={tapDurationMs} />
    </div>

    <div class="line">
      <label for="swipeStartX">swipeStartX</label>
      <input id="swipeStartX" type="number" bind:value={swipeStartX} />
      <label for="swipeStartY">swipeStartY</label>
      <input id="swipeStartY" type="number" bind:value={swipeStartY} />
      <label for="swipeEndX">swipeEndX</label>
      <input id="swipeEndX" type="number" bind:value={swipeEndX} />
      <label for="swipeEndY">swipeEndY</label>
      <input id="swipeEndY" type="number" bind:value={swipeEndY} />
      <label for="swipeDurationMs">swipeDurationMs</label>
      <input id="swipeDurationMs" type="number" min="1" bind:value={swipeDurationMs} />
    </div>
  </div>

  <div class="actions">
    <button onclick={testPing}>Ping</button>
    <button onclick={testCheckAccessibilityEnabled}>Check Accessibility</button>
    <button onclick={testOpenAccessibilitySettings}>Open Settings</button>
    <button onclick={testGetFrontmostUiTree}>Get UI Tree</button>
    <button onclick={testClickNode}>Click Node</button>
    <button onclick={testPerformNodeAction}>Perform Node Action</button>
    <button onclick={testPerformGlobalAction}>Perform Global Action</button>
    <button onclick={testPerformTapGesture}>Tap Gesture</button>
    <button onclick={testPerformSwipeGesture}>Swipe Gesture</button>
    <button onclick={testPerformPinchGesture}>Pinch Gesture</button>
    <button class="primary" onclick={runAll}>Run All Methods</button>
  </div>

  <div class="logs">
    {#if logs.length === 0}
      <p>No logs yet.</p>
    {:else}
      {#each logs as line}
        <pre>{line}</pre>
      {/each}
    {/if}
  </div>
</main>
