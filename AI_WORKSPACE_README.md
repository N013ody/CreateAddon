# AI Workspace Notes

This folder is a Claude/Codex-friendly local engineering copy of the Minecraft NeoForge mod project.

## Location

```text
<local workspace>/Create-Computation
```

Some contributors may also keep an older local working tree for reference:

```text
<local workspace>/createaddon
```

Do not delete or overwrite another contributor's old working tree unless they explicitly ask for it.

## Project

- Mod name direction: `机械动力：计算学 / Create: Computation`
- Loader: NeoForge
- Minecraft version: 1.21.1
- Java: 21
- Main package currently used by local work: `io.github.n013ody.createaddon`

## Core Design Principle

This mod is a Create addon, so its systems must follow Create's usual expectation of high customization and player-built machinery.

Do not design computation as a fixed one-block black box. Difference engines, analytical engines, semiconductor control systems, and later guidance/control computers should be modular, visible, and scalable. A player should be able to build an analytical engine from many mechanical components, and those components should contribute measurable capability such as:

- compute speed
- compute capacity or upper limit
- precision
- memory/storage
- program length
- throughput
- stability or calibration quality

For example, an analytical engine should not simply be "one block makes one result". Its gears, drums, registers, card readers, Mill blocks, Store columns, printers, shafts, and future modules should define how powerful the assembled machine is. Future content should prefer player-assembled structures and world-level interactions over hardcoded single-block machines.

## Build

On this Windows machine, `java` is not available on the global PATH. This copy includes a project-local JDK under `.codex-tools/jdk-21/`, so the easiest local build command is:

```powershell
.codex-tools\gradlew-dev.cmd build
```

If this project is uploaded to GitHub, the bundled JDK should not be uploaded. The `.gitignore` excludes:

```text
.codex-tools/jdk-21/
.codex-tools/blockbench-asar/
.codex-tools/temurin-jdk-21.zip
```

For a fresh clone, install JDK 21 and run:

```powershell
.\gradlew.bat build
```

## Documentation

Important handoff docs:

```text
docs\机械动力-计算学 CODEX总结文档.txt
docs\制作记录\
docs\建模与贴图提示词.txt
docs\blockbench\createaddon-mcp-modeling-prompt.md
```

Every completed task should add a new note under:

```text
docs\制作记录\
```

The note should include:

- goal
- design thinking
- implementation method
- changed files
- resource paths
- verification commands/results
- next suggested work

## Create Tooltip / Ponder Rule

Every player-facing entry added to the creative tab must have Create-style tooltip text:

```text
<block_or_item>.createaddon.<id>.tooltip.summary
```

Every player-facing block added to the creative tab must also have a Ponder storyboard so players can hold `W` to think/ponder the block in the usual Create style.

After adding new blocks or items, run:

```powershell
node .codex-tools\validate-create-tooltips.mjs
```

## GitHub Boundary

Do not push, upload, create remote branches, or open pull requests unless the project owner explicitly grants permission in the current conversation.
