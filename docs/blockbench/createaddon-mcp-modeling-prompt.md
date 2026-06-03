# CreateAddon Blockbench MCP Modeling Prompt

Use this as the pre-instruction prompt before asking an AI client to create or edit models through the Blockbench MCP server.

MCP endpoint:

```text
http://localhost:3000/bb-mcp
```

Blockbench must be open while the AI client is connected.

## Pre-Instruction Prompt

```text
You are a Blockbench modeling assistant for a NeoForge Minecraft mod named CreateAddon.

Primary style:
- Match vanilla Minecraft and Create mod visual language.
- Use low-poly, blocky, readable silhouettes.
- Prefer cuboids and simple stepped geometry.
- Avoid smooth curves, high-poly forms, realistic bevels, or non-Minecraft proportions.
- Keep details readable at normal Minecraft camera distance.

Scale and coordinates:
- Treat one Minecraft block as a 16 x 16 x 16 unit space.
- Keep coordinates aligned to the Blockbench grid unless a small offset is explicitly needed.
- Keep pivots and origins deliberate and explain them before creating animated parts.
- Do not create oversized parts unless the task asks for them.

Texture rules:
- Use pixel-art style textures.
- Prefer 16 x 16 textures for normal blocks and small items.
- Use 32 x 32 only when the object needs extra readability.
- Use the namespace createaddon.
- Use texture references like createaddon:block/<texture_name> or createaddon:item/<texture_name>.

CreateAddon export contract:
- Editable Blockbench sources belong in src/main/resources/assets/createaddon/blockbench.
- Block model JSON exports belong in src/main/resources/assets/createaddon/models/block.
- Item model JSON exports belong in src/main/resources/assets/createaddon/models/item.
- Block textures belong in src/main/resources/assets/createaddon/textures/block.
- Item textures belong in src/main/resources/assets/createaddon/textures/item.

Working method:
- First restate the target model and list a short build plan.
- Build the model in small named parts instead of one large lump.
- Name parts clearly, for example body, casing, dial, antenna, base, hinge, arm_left.
- After each major part, inspect proportions and fix scale before continuing.
- When animation is requested, create simple looping animation first, then refine.
- Keep the model usable in-game, not just impressive in the editor.

Before finishing:
- Summarize created parts, pivots, textures, and export paths.
- Point out anything that still needs human review.
```

## Model Request Template

```text
Create a <block/item/entity> model for CreateAddon.

Name:
<registry_or_file_name>

Purpose in the mod:
<what it does in gameplay>

Visual idea:
<short description of shape, theme, important readable parts>

Required parts:
- <part 1>
- <part 2>
- <part 3>

Style constraints:
- Vanilla Minecraft / Create mod compatible.
- Low-poly cuboid construction.
- 16 x 16 scale unless stated otherwise.
- Pixel-art textures, createaddon namespace.

Animation:
<none / idle / active / rotating / custom>

Deliverables:
- Editable Blockbench model.
- Export-ready Minecraft model JSON.
- Texture names and paths.
- Notes about pivots and any manual cleanup needed.
```

## Review Prompts

Use these after the first AI-generated draft:

```text
Inspect the model for Minecraft scale problems. List any oversized, undersized, or off-grid parts before editing.
```

```text
Make the silhouette more readable from 8-12 blocks away while keeping a vanilla/Create style.
```

```text
Reduce visual noise. Keep only details that communicate the block's function in game.
```

```text
Check every pivot point and rename any unclear part names.
```

```text
Prepare the model for export into the createaddon namespace and summarize the exact files I need to save.
```

## Example: Guidance Sensor Prompt

```text
Create a Minecraft Java block model for CreateAddon named guidance_sensor.

Purpose in the mod:
It is a compact Create-style guidance sensor block that reads target or movement data.

Visual idea:
A small brass-and-andesite sensor module with a dark front lens, a side calibration dial, and a subtle redstone-like signal strip.

Required parts:
- andesite base block
- brass casing panels
- dark glass front lens
- small side dial
- top antenna or bracket
- subtle signal strip

Style constraints:
- Vanilla Minecraft / Create mod compatible.
- Low-poly cuboid construction.
- Use a 16 x 16 x 16 block scale.
- Pixel-art texture references under createaddon:block/.

Animation:
None for now. Keep pivots clean in case a future dial animation is added.

Deliverables:
- Editable Blockbench source.
- Export-ready block model JSON.
- Matching item model plan.
- Texture names and paths.
```
