# Create 附属开发技术基线

> 适用项目：机械动力：计算学 / Create: Computation  
> 当前工程版本：Minecraft 1.21.1、NeoForge 21.1.219、Create 6.0.10-280、Flywheel 1.0.6、Ponder 1.0.82  
> 整理时间：2026-06-04

## 资料来源

本基线以当前工程和同版本 Create 源码为准，不按旧版教程盲写。

- 当前工程：`build.gradle`、`gradle.properties`、`src/main/java/io/github/n013ody/createaddon`
- Create 官方源码：`Creators-of-Create/Create`，分支 `mc1.21.1/dev`
- Create 官方发布页：Create 6.0.10 for mc1.21.1
- Create 官方说明：Create 的设计目标是让玩家用可见组件搭建机械结构，并用 Ponder 做游戏内教学

## 总原则

计算学是 Create 附属，不应做成 RF/EU 能量机器，也不应把核心玩法压进固定 GUI 或单方块黑盒。

新方块要优先满足：

- 世界中可见的机械输入、输出和运动。
- 玩家能通过轴、齿轮、皮带、组件布局和模块数量改变结果。
- 速度、应力、容量、精度、程序长度、存储量等能力要可解释、可平衡。
- 每个玩家可见方块都有 Shift 说明和 W/Ponder 思考场景。
- 重要状态应能通过外观、粒子、声音、tooltip、Ponder 或护目镜信息被玩家理解。

## 动力方块选择

### 轴向机器

需要从两端接传动轴、齿轮箱或同轴机械结构时，优先继承：

```java
RotatedPillarKineticBlock
```

它提供 `AXIS` 状态，并会在放置时尝试对齐相邻 Create 动力结构。适合：

- 数表打印机
- 机械打印机
- 未来的计算鼓、差分轴、同步轴、可替换计算模块

资源侧要同步提供 `axis=x/y/z` blockstate。

### 单面方向机器

只有一个朝向面、输入/输出方向和方块朝向绑定时，再考虑：

```java
DirectionalKineticBlock
```

适合激光测距仪、单向传感器、风扇式输出设备等。

### 非动力方块

如果方块只是模块、结构件、存储柱、设置鼓，且本身不吃转速，可以先使用普通 `Block` 或 `SmartBlockEntity`。但只要它参与动力网络、响应速度、显示应力或有持续状态，就应有明确的 block entity。

## KineticBlockEntity 规则

Create 动力机器的 block entity 应继承：

```java
KineticBlockEntity
```

关键行为：

- `getSpeed()` 是机器实际可用速度。
- 当网络过载或 tick 冻结时，`getSpeed()` 会返回 0。
- 如果需要显示理论转速，才使用 `getTheoreticalSpeed()`。
- `isOverStressed()` 可以判断网络是否过载。
- `calculateStressApplied()` 返回 1 RPM 下的基础应力影响。
- Create 会按实际转速把基础应力换算成当前压力。

因此计算学机器应遵守：

- 没有速度就不开始、不推进。
- 过载时暂停，而不是继续扣进度。
- 速度越快，处理越快。
- 速度越快，网络需要的应力容量越多。
- 状态改变后调用 `setChanged()`，需要同步客户端显示时调用 `sendData()`。

## 应力规则

Create 的应力分为：

- Stress Capacity：动力源能提供多少应力容量。
- Stress Impact：机器消耗多少应力。

Create 源码里 `BlockStressValues` 说明基础 impact/capacity 都是按 1 RPM 记录。实际运行时，当前应力会随转速放大。

对附属模组要注意：

- 不要直接使用 Create 内部的 `CStress.setImpact(...)` 给本模组注册，因为它会检查注册 owner 是否为 Create。
- 简单机器可以在自己的 `KineticBlockEntity#calculateStressApplied()` 中返回动态 impact。
- 需要显示静态 tooltip/goggles 应力时，再考虑向 `BlockStressValues.IMPACTS` 注册 provider。
- 动态机器要保证 `lastStressApplied` 被更新，否则护目镜统计可能不准确。

计算学当前设计适合：

```text
基础应力 = 机器底座消耗 + 组件规模消耗
运行时间 = 基础时间 / 转速倍率 / 组件能力倍率
```

这样可以形成 Create 风格取舍：玩家可以堆组件、提转速，但要承担更高应力网络成本。

## 处理流程模式

参考 Millstone、Mixer 等 Create 机器，持续加工不应写成一次右击瞬间完成。

推荐流程：

1. 右击或输入物品后创建任务。
2. 记录输入、输出、剩余 ticks、总 ticks、应力影响。
3. 每 tick 检查 `getSpeed()`。
4. 速度为 0 或过载时暂停。
5. 用 `abs(getSpeed())` 计算本 tick 进度。
6. 完成后产出物品，并清除任务。

建议对速度换算做上下限：

```text
进度倍率 = clamp(abs(speed) / 32, 0.25, 8.0)
```

避免低速完全无意义，也避免高速让机器瞬间完成导致反馈看不清。

## BlockEntity 与 IBE

Create 常用 `IBE<T>` 连接方块和 block entity。

新机器如果有 block entity，方块应实现：

```java
IBE<MyBlockEntity>
```

并提供：

- `getBlockEntityClass()`
- `getBlockEntityType()`

如果 block entity 继承 `SmartBlockEntity` 或 `KineticBlockEntity`，移除方块时要让 Create 正确清理：

```java
IBE.onRemove(oldState, level, pos, newState);
```

需要物品、流体等自动化接口时，用 NeoForge capability 注册。Create 机器通常在静态方法里注册 `Capabilities.ItemHandler.BLOCK` 或 `Capabilities.FluidHandler.BLOCK`。

## Tooltip 与 Ponder

Create 的物品说明链路是：

- `TooltipModifier.REGISTRY`
- `ItemDescription.Modifier`
- 语言键：`.tooltip.summary`
- 语言键：`.tooltip.conditionN` + `.tooltip.behaviourN`
- 语言键：`.tooltip.controlN` + `.tooltip.actionN`

但本项目使用 NeoForge `DeferredRegister`，不是 Create 自己的 Registrate 全链路。对于计算学方块，必须使用项目内的兜底入口：

```java
registerComputingItem(...)
ComputingBlockItem
```

这样至少保证：

- 未按 Shift 时显示 `按住 [Shift] 可查看概要`。
- 始终显示 `按住 [W] 开始思索`。
- 按住 Shift 时展开对应 `block.createaddon.<id>.tooltip.*`。

Ponder 注册规则：

- 每个玩家可见方块都要有 `forComponents(...).addStoryBoard(...)`。
- 每类机制至少有一个可演示的场景。
- 场景要展示摆放、动力输入、组件范围、运行结果，而不是只写文字。

## 资源与模型

Create 风格方块资源要优先可读：

- 动力口、操作面、状态面要在模型或贴图上区分。
- 轴向机器必须有 `axis=x/y/z` blockstate。
- 如果各面功能不同，不要用 `cube_all` 糊过去。
- 方块名、tooltip、Ponder 文本至少维护 `zh_cn.json` 和 `en_us.json`。
- loot table、recipe、item model、blockstate、block model 要成套添加。

## 渲染路线

当前阶段可以先使用普通 `BlockEntityRenderer` 做简单可视化。

以后如果要做更 Create 风格的运动部件，应优先研究并复用：

- `KineticBlockEntityRenderer`
- `KineticBlockEntityVisual`
- Flywheel visual
- partial model
- rotating instance

不要先手写大量低层渲染数学。先找 Create 同类方块，例如 Millstone、Mechanical Press、Mixer、Shaft、Gearbox。

## 计算学专用设计约束

以后新增差分机、分析机、机械计算机、电子计算或工业控制内容时，先回答：

1. 这个方块是模块、动力输入、存储、程序、运算、输出，还是控制器？
2. 它贡献什么可量化能力？
3. 它如何被玩家组合进更大的机器？
4. 它吃不吃转速？吃多少应力？
5. 它是否需要方向、轴向或具体面作用？
6. 玩家能否通过 Shift/W 明白怎么用？
7. 是否有验证脚本防止以后漏掉 tooltip、Ponder、资源文件？

## 禁止倾向

- 不做固定一方块完成整个“差分机/分析机”的黑盒。
- 不引入 RF/EU 式外部能量系统。
- 不把主要玩法放进复杂 GUI。
- 不写无视 `getSpeed()` 的持续机器。
- 不在过载时继续加工。
- 不新增方块后漏掉 Shift 说明、W/Ponder、语言键、配方、掉落和制作记录。

## 后续学习方向

接下来继续深入时，优先看这些 Create 源码区域：

- `content/kinetics/base`：动力网络、轴向传播、速度与应力。
- `content/kinetics/millstone`：速度驱动的持续加工。
- `content/kinetics/mixer`：处理时长与速度映射。
- `content/kinetics/press`：动画阶段与加工阶段分离。
- `foundation/item`：Create tooltip 机制。
- `infrastructure/ponder`：Ponder 教学场景写法。
- `foundation/data` 和 `AllBlocks`：资源生成、模型、应力默认值与注册习惯。
