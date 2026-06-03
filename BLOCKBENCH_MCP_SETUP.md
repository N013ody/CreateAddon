# Blockbench MCP Setup

This project uses the open-source Blockbench MCP plugin from:

- Repository: `https://github.com/jasonjgardner/blockbench-mcp-plugin`
- Plugin URL: `https://jasonjgardner.github.io/blockbench-mcp-plugin/mcp.js`
- Installed MCP endpoint: `http://localhost:3000/bb-mcp`

## Local Install

The plugin is staged locally at:

- Downloaded source: `.codex-tools/blockbench-mcp-plugin/mcp.js`
- Blockbench runtime copy: `%APPDATA%/Blockbench/plugins/mcp.js`

Run this any time you want to reinstall the local Blockbench MCP plugin:

```powershell
.\.codex-tools\blockbench-mcp-install.cmd
```

That script opens Blockbench, registers the `mcp` plugin, and pre-approves the local `process` and `net` permissions needed for the MCP server.

## Health Check

Blockbench must be open for the MCP server to be reachable.

```powershell
.\.codex-tools\blockbench-mcp-health.cmd
```

Expected result is JSON with `"status":"ok"`.

## AI Client Notes

Use `http://localhost:3000/bb-mcp` as the MCP server URL in clients that support Streamable HTTP MCP.

The MIMO/OpenAI-compatible API key belongs in the AI client or model gateway configuration, not in Blockbench or this plugin. Do not commit API keys to the repository.

## Modeling Prompt

Use `docs/blockbench/createaddon-mcp-modeling-prompt.md` as the standard pre-instruction prompt before asking AI to create or edit CreateAddon models through Blockbench MCP.
