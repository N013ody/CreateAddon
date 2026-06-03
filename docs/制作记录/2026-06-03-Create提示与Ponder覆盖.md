# 2026-06-03 Create 提示与 Ponder 覆盖记录

## 目标

为《机械动力：计算学 / Create: Computation》补上更符合 Create 习惯的物品说明体验：

```text
按住 Shift 查看使用说明
按住 W 思考 / Ponder 方块
```

同时把这个要求固化为后续制作规则，避免未来新增方块时漏掉说明。

## 设计原则

- 创造栏中所有玩家会看到的方块和物品，都要有 Create 风格 tooltip。
- 创造栏中所有玩家会看到的方块，都要注册至少一个 Ponder storyboard。
- 第一版 Ponder 可以是同类方块共享的基础说明；重要机器后续再逐步替换为更精细的专属场景。
- `testblock` 仍在创造栏中，所以也给了基础开发测试说明，避免覆盖率校验出现例外。

## 本次实现

### Tooltip 注册

补充客户端 tooltip 注册：

```text
src/main/java/io/github/n013ody/createaddon/client/CreateAddonClient.java
```

现在矿物、材料、半导体机器、晶圆/芯片物品、差分机/分析机组件、制导方块与工具都注册了 Create 的 `ItemDescription.Modifier`。

### Tooltip 文案

补充中英文 tooltip summary 和关键机器行为说明：

```text
src/main/resources/assets/createaddon/lang/en_us.json
src/main/resources/assets/createaddon/lang/zh_cn.json
```

### Ponder 覆盖

补充 Ponder 标签和基础 storyboard：

```text
src/main/java/io/github/n013ody/createaddon/client/ponder/GuidancePonderTags.java
src/main/java/io/github/n013ody/createaddon/client/ponder/GuidancePonderScenes.java
src/main/java/io/github/n013ody/createaddon/client/ponder/GuidanceScenes.java
```

新增/扩展的 Ponder 分类包括：

- `Create: Computation`
- `Semiconductor Production`
- 原有 `Guidance & Control`
- 原有 `Sensor Inputs`

当前基础场景覆盖：

- 锡矿、深层锡矿、深层钨矿
- 半导体机器链
- 差分机组件与数表打印机
- 分析机组件与机械打印机
- 阈值控制器
- 旋转框架
- 原有制导计算机和传感器

## 新增校验脚本

新增：

```text
.codex-tools/validate-create-tooltips.mjs
```

它会从创造栏读取所有展示项，并检查：

- 每个展示项是否在客户端注册了 Create tooltip。
- 每个展示项是否有英文和中文 `.tooltip.summary`。
- 每个展示方块是否在 Ponder 注册文件中有 storyboard。

以后新增方块或物品后应运行：

```powershell
node .codex-tools\validate-create-tooltips.mjs
```

## 验证结果

已运行：

```powershell
node .codex-tools\validate-create-tooltips.mjs
node .codex-tools\validate-ore-content.mjs
node .codex-tools\validate-chip-content.mjs
node .codex-tools\validate-computation-engines.mjs
node .codex-tools\validate-chip-machine-behavior.mjs
.codex-tools\gradlew-dev.cmd build
```

结果：

```text
Create tooltip coverage validated
Ore content validation passed
Chip content validation passed
computation engine content validated
Chip machine behavior validation passed
BUILD SUCCESSFUL
```

## 后续规则

以后制作新方块时，完成标准至少包括：

```text
注册 -> 创造栏 -> 资源/掉落/配方 -> Shift tooltip -> W Ponder -> 校验脚本 -> build
```

如果是重要机器，基础 Ponder 之后还应补更直观的专属动画和实际用途演示。
