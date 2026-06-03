import { spawn } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const PLUGIN_URL = 'https://jasonjgardner.github.io/blockbench-mcp-plugin/mcp.js';
const PLUGIN_VERSION = '1.6.0';
const MCP_PORT = 3000;
const MCP_ENDPOINT = '/bb-mcp';
const DEBUG_PORT = 9341;

const cwd = process.cwd();
const appData = process.env.APPDATA;
const localAppData = process.env.LOCALAPPDATA;

if (!appData || !localAppData) {
  throw new Error('APPDATA or LOCALAPPDATA is not set.');
}

const blockbenchExe = path.join(localAppData, 'Programs', 'Blockbench', 'Blockbench.exe');
const blockbenchUserData = path.join(appData, 'Blockbench');
const blockbenchPluginDir = path.join(blockbenchUserData, 'plugins');
const pluginSource = path.join(cwd, '.codex-tools', 'blockbench-mcp-plugin', 'mcp.js');
const pluginRuntimeFile = path.join(blockbenchPluginDir, 'mcp.js');
const permissionFile = path.join(blockbenchUserData, 'plugin_permissions.json');

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function readJson(file, fallback) {
  try {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch {
    return fallback;
  }
}

function writeJson(file, data) {
  fs.writeFileSync(file, JSON.stringify(data, null, '\t'), 'utf8');
}

async function fetchJson(url) {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`${url} returned HTTP ${response.status}`);
  }
  return await response.json();
}

async function ensurePluginSource() {
  if (fs.existsSync(pluginSource)) return;

  fs.mkdirSync(path.dirname(pluginSource), { recursive: true });
  const response = await fetch(PLUGIN_URL);
  if (!response.ok) {
    throw new Error(`${PLUGIN_URL} returned HTTP ${response.status}`);
  }
  fs.writeFileSync(pluginSource, await response.text(), 'utf8');
}

async function waitForDebugTarget() {
  const deadline = Date.now() + 30000;
  while (Date.now() < deadline) {
    try {
      const targets = await fetchJson(`http://127.0.0.1:${DEBUG_PORT}/json/list`);
      const page = targets.find(target => target.type === 'page' && target.webSocketDebuggerUrl);
      if (page) return page;
    } catch {
      // Blockbench is still starting.
    }
    await sleep(500);
  }
  throw new Error('Timed out waiting for Blockbench remote debugging target.');
}

async function connectCdp(webSocketDebuggerUrl) {
  const socket = new WebSocket(webSocketDebuggerUrl);
  const pending = new Map();

  await new Promise((resolve, reject) => {
    socket.addEventListener('open', resolve, { once: true });
    socket.addEventListener('error', reject, { once: true });
  });

  socket.addEventListener('message', event => {
    const message = JSON.parse(event.data);
    if (!message.id || !pending.has(message.id)) return;

    const { resolve, reject } = pending.get(message.id);
    pending.delete(message.id);

    if (message.error) {
      reject(new Error(message.error.message || JSON.stringify(message.error)));
    } else {
      resolve(message.result);
    }
  });

  let id = 0;
  return {
    send(method, params = {}) {
      id += 1;
      socket.send(JSON.stringify({ id, method, params }));
      return new Promise((resolve, reject) => pending.set(id, { resolve, reject }));
    },
    close() {
      socket.close();
    }
  };
}

async function evaluate(cdp, expression) {
  const result = await cdp.send('Runtime.evaluate', {
    expression,
    awaitPromise: true,
    returnByValue: true
  });
  if (result.exceptionDetails) {
    throw new Error(result.exceptionDetails.text || 'Runtime.evaluate failed.');
  }
  return result.result?.value;
}

async function waitForBlockbenchRuntime(cdp) {
  const ready = await evaluate(cdp, `
    new Promise(resolve => {
      const started = Date.now();
      const tick = () => {
        if (window.Blockbench && window.Plugin && window.Plugins && Blockbench.setup_successful) {
          resolve(true);
        } else if (Date.now() - started > 20000) {
          resolve(false);
        } else {
          setTimeout(tick, 250);
        }
      };
      tick();
    })
  `);
  if (!ready) {
    throw new Error('Blockbench runtime did not become ready.');
  }
}

async function installPluginInBlockbench(cdp) {
  const pluginPath = JSON.stringify(pluginRuntimeFile);
  return await evaluate(cdp, `
    (async () => {
      const pluginPath = ${pluginPath};
      const installEntry = {
        id: 'mcp',
        version: '${PLUGIN_VERSION}',
        path: pluginPath,
        source: 'file'
      };

      let installed = [];
      try {
        installed = JSON.parse(localStorage.getItem('StateMemory.installed_plugins') || '[]');
      } catch {
        installed = [];
      }
      installed = installed.filter(plugin => plugin && plugin.id !== 'mcp');
      installed.push(installEntry);
      localStorage.setItem('StateMemory.installed_plugins', JSON.stringify(installed));

      if (window.StateMemory) {
        StateMemory.installed_plugins = installed;
        StateMemory.save('installed_plugins');
      }

      let plugin = Plugins.all.find(item => item.id === 'mcp' && item.installed);
      if (plugin && plugin.disabled) {
        plugin.toggleDisabled();
      }
      if (!plugin || !plugin.installed) {
        plugin = new Plugin();
        await plugin.loadFromFile({ path: pluginPath, name: pluginPath, content: '' }, false);
      }

      return {
        id: plugin.id,
        title: plugin.title,
        version: plugin.version,
        source: plugin.source,
        path: plugin.path || pluginPath,
        installed: plugin.installed,
        disabled: plugin.disabled === true
      };
    })()
  `);
}

async function waitForMcpHealth() {
  const url = `http://127.0.0.1:${MCP_PORT}${MCP_ENDPOINT}/health`;
  const deadline = Date.now() + 30000;
  let lastError = '';

  while (Date.now() < deadline) {
    try {
      const data = await fetchJson(url);
      if (data.status === 'ok') return data;
      lastError = JSON.stringify(data);
    } catch (error) {
      lastError = error.message;
    }
    await sleep(1000);
  }

  throw new Error(`MCP health check failed: ${lastError}`);
}

if (!fs.existsSync(blockbenchExe)) {
  throw new Error(`Blockbench.exe not found at ${blockbenchExe}`);
}
await ensurePluginSource();

fs.mkdirSync(blockbenchPluginDir, { recursive: true });
fs.copyFileSync(pluginSource, pluginRuntimeFile);

const permissions = readJson(permissionFile, {});
permissions.mcp = permissions.mcp || {};
permissions.mcp.allowed = permissions.mcp.allowed || {};
permissions.mcp.allowed.process = true;
permissions.mcp.allowed.net = true;
writeJson(permissionFile, permissions);

const child = spawn(blockbenchExe, [`--remote-debugging-port=${DEBUG_PORT}`], {
  detached: true,
  stdio: 'ignore'
});
child.unref();

const target = await waitForDebugTarget();
const cdp = await connectCdp(target.webSocketDebuggerUrl);

try {
  await cdp.send('Runtime.enable');
  await waitForBlockbenchRuntime(cdp);
  const installed = await installPluginInBlockbench(cdp);
  const health = await waitForMcpHealth();

  console.log(JSON.stringify({
    ok: true,
    blockbench: blockbenchExe,
    pluginUrl: PLUGIN_URL,
    pluginFile: pluginRuntimeFile,
    installed,
    mcpUrl: `http://localhost:${MCP_PORT}${MCP_ENDPOINT}`,
    health
  }, null, 2));
} finally {
  cdp.close();
}
