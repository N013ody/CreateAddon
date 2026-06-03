# Blockbench Setup

This project uses Blockbench for Minecraft Java block and item models.

## Installed App

- Blockbench: 5.1.4
- Executable: expected at `%LOCALAPPDATA%\Programs\Blockbench\Blockbench.exe` on Windows
- Optional local project config: `.codex-tools/blockbench-paths.json` (ignored by Git because it may contain machine-specific paths)

## Quick Commands

```powershell
.\.codex-tools\blockbench-open.cmd
.\.codex-tools\blockbench-open-guidance-sensor.cmd
.\.codex-tools\blockbench-open-guidance-computer.cmd
.\.codex-tools\mcmod-model-check.cmd
.\.codex-tools\blockbench-mcp-install.cmd
.\.codex-tools\blockbench-mcp-health.cmd
```

## Project Paths

- Blockbench source files: `src/main/resources/assets/createaddon/blockbench`
- Export block models to: `src/main/resources/assets/createaddon/models/block`
- Export item models to: `src/main/resources/assets/createaddon/models/item`
- Blockstates live in: `src/main/resources/assets/createaddon/blockstates`
- Block textures live in: `src/main/resources/assets/createaddon/textures/block`
- Item textures live in: `src/main/resources/assets/createaddon/textures/item`

## Export Rules

- Use project type: Minecraft Java Block/Item.
- Keep the namespace as `createaddon`.
- Use texture references like `createaddon:block/guidance_sensor_side`.
- Export block model JSON to `models/block`.
- Export item model JSON to `models/item`.
- Keep `.bbmodel` files in `assets/createaddon/blockbench` as editable source files.
- Run `.\.codex-tools\mcmod-model-check.cmd` after exporting to catch JSON/resource issues.

## Current Source Models

- `guidance_sensor.bbmodel`
- `guidance_computer.bbmodel`
- `sensor_model_design_notes.bbmodel`

## MCP

See `BLOCKBENCH_MCP_SETUP.md` for the local Blockbench MCP plugin setup.
Use `docs/blockbench/createaddon-mcp-modeling-prompt.md` as the standard AI modeling pre-instruction prompt.
