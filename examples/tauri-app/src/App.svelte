<script>
  import {
    ping,
    checkAccessibilityEnabled,
    openAccessibilitySettings,
    getFrontmostUiTree,
    clickNode,
  } from '../../../dist-js/index.js'

  let logs = $state([])
  let nodeId = $state('0')
  let maxDepth = $state(6)
  let maxChildrenPerNode = $state(30)
  let includeNonClickable = $state(true)

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

  async function runAll() {
    pushLog('runAll', 'start')
    await testPing()
    await testCheckAccessibilityEnabled()
    await testGetFrontmostUiTree()
    await testClickNode()
    await testOpenAccessibilitySettings()
    pushLog('runAll', 'done')
  }
</script>

<main class="container">
  <h1>Android Accessiblity Plugin Debugger</h1>

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
  </div>

  <div class="actions">
    <button onclick={testPing}>Ping</button>
    <button onclick={testCheckAccessibilityEnabled}>Check Accessibility</button>
    <button onclick={testOpenAccessibilitySettings}>Open Settings</button>
    <button onclick={testGetFrontmostUiTree}>Get UI Tree</button>
    <button onclick={testClickNode}>Click Node</button>
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
