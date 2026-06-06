# Create 附属与航空学经验基线

> 适用项目：机械动力：计算学 / Create: Computation  
> 整理时间：2026-06-04  
> 研究对象：Create Aeronautics / Simulated Project、Create Big Cannons、Create Crafts & Additions  

## 版本边界

本文件用于沉淀设计经验，不表示可以直接复制外部源码。

- Create Aeronautics / Simulated Project：Minecraft 1.21.1，NeoForge 21.1.219，Create 6.0.10-280，Ponder 1.0.81。它和本项目版本接近，适合作为航空学与物理子世界的主要参考。
- Create Big Cannons：Minecraft 1.21.1，NeoForge 21.1.225，Create 6.0.10-280，Ponder 1.0.82。它和本项目同世代，适合作为复杂 Create 机构拆层参考。
- Create Crafts & Additions：Minecraft 1.20.1，Forge 47.4.0，Create 6.0.8-289。它版本较旧，只参考架构思想，不能直接照抄 API。

## 航空学 / Simulated 的核心经验

航空学不是一个会飞的单方块，而是一整套物理系统：

- `Simulated` 把可移动结构装配成 SubLevel，并处理装配、拆解、胶水、实体搬运、追踪点和拆解限制。
- `Aeronautics` 在物理子世界之上增加气球、推进器、陀螺推进器、空气压力、粒子、声音和 Ponder 教程。
- 推进器方块使用 Create 动力网络。转速决定推力和气流，帆叶数量会影响应力消耗。
- 陀螺推进器会读取重力方向和 SubLevel 姿态，再把目标方向限制在机械允许范围内，而不是瞬间锁死姿态。
- 物理信息通过图表、力箭头、质心、HUD、Tooltip、Ponder 和异常文本让玩家理解。

给计算学的结论：

- 未来不要直接做“一个航空控制电脑控制所有飞行器”的黑盒。
- 先做通用控制信号，再做航空学适配层。
- 计算机应该输出目标、误差、PID 或控制通道，而不是直接假设某一种飞行器结构。
- 航空学兼容应尽量是可选层：没有 Aeronautics 时，计算学核心仍能运行。
- 任何物理/制导功能都要能被玩家看见：目标点、误差、输出强度、稳定性、延迟和失败原因都要有显示路径。

## Big Cannons 的核心经验

Create Big Cannons 说明复杂机构不能塞进一个类里。它把系统拆成：

- 控制器方块：负责玩家交互、动力输入、状态和护目镜信息。
- contraption entity：负责被装配后的移动结构和姿态。
- 接口：例如 pitch/yaw 控制接口，把控制器和被控制结构解耦。
- 兼容注册表：记录哪些 Create contraption 可兼容、哪些脆弱、哪些需要特殊规则。
- mixin 兼容层：只在必须改变 Create 内部行为时使用，并放在明确的 compat 包里。
- renderer/visual：单独处理运动部件和视觉反馈。
- Ponder：复杂机构必须有分步骤演示，而不是只靠文字说明。

给计算学的结论：

- 分析机、差分机、飞控计算机都应拆成控制器、组件、存储、输出、显示和可选兼容层。
- 制导/飞控相关代码应先定义接口，例如 `ControlSignalProvider`、`SensorInputProvider`、`ComputationBus`，再决定接 Aeronautics、红石、Display Link 或其他系统。
- 如果以后需要 mixin，一定先证明普通 API 无法完成，并把 mixin 限制在很小的兼容层。
- 失败原因要玩家可读：应力不足、速度为 0、组件不足、目标丢失、精度不足，都应有 tooltip/goggles/Ponder 说明。

## Crafts & Additions 的核心经验

Create Crafts & Additions 的价值在于“桥接外部系统但仍保留 Create 味道”：

- 电机和发电机把 FE 与 Create 动力互相转换，但转换逻辑集中在明确的适配器中。
- 机器仍使用 `KineticBlockEntity`、`GeneratingKineticBlockEntity`、renderer、visual、capability 和 Ponder。
- 线缆、储能、多方块、便携接口、ComputerCraft 外设都以独立模块组织。
- 旧版 Forge API 与 NeoForge 1.21.1 差异较大，只能学习拆层方式，不应复制代码。

给计算学的结论：

- 计算学不应引入 RF/FE 作为核心能量系统；核心仍是 Create 转速、应力、结构、组件和可见机械逻辑。
- 如果要接外部系统，应放进适配层，例如 ComputerCraft、Aeronautics、红石链路、Display Link。
- 适配层可以提供数据或控制通道，但不能替代计算学自己的机械计算玩法。
- 自动化接口要保持面向世界：物品、红石、显示屏、频率、模块面，而不是只靠 GUI。

## 计算学的自我迭代清单

以后每次设计新方块、新机构或新兼容层，都先过这张清单：

1. 版本确认：先确认目标 Create、NeoForge、Ponder、Flywheel 版本，不照搬旧教程。
2. 归类：这是核心玩法、可选兼容、研究验证，还是仅文档经验。
3. Create 原语：它更像 kinetic block、SmartBlockEntity、contraption、capability、display source、Ponder 还是数据组件。
4. 模块能力：它贡献什么可量化能力，例如速度、容量、精度、内存、稳定性、控制通道数、传感范围。
5. 玩家搭建：玩家怎样通过齿轮、轴、模块数量、卡片、存储、传感器来改变能力。
6. 动力代价：是否吃转速，是否吃应力，速度更快时收益和代价是否同时变大。
7. 可读性：方块各面作用、状态、错误、输入输出、控制方向是否能通过贴图、tooltip、Ponder、护目镜读懂。
8. 验证：是否有脚本或轻量检查防止漏 tooltip、Ponder、语言键、资源文件和制作记录。

## 航空学兼容路线建议

不要直接把制导计算机写死成航空学自动驾驶。推荐路线：

1. 先做 `控制信号总线`：能输出俯仰、偏航、滚转、油门、刹车、目标向量、误差值。
2. 再做 `传感模块`：读取方位、速度、角速度、目标距离、红石输入、Display Link 数据。
3. 再做 `机械 PID / 稳定器`：用计算速度、存储和精度决定控制更新频率和稳定性。
4. 最后做 `Aeronautics 适配器`：把通用控制信号转换为航空学推进器、陀螺仪或其他可控部件能理解的输入。

可能的未来方块：

- 飞控接口：把计算学控制信号输出到飞行器。
- 机械 PID 调速器：用转速和应力换取更高控制频率。
- 陀螺稳定模块：提高姿态稳定性，但需要持续转速。
- 目标追踪卡：存储目标点或目标实体信息。
- 物理探针：读取速度、姿态、质心、推力、误差。
- 控制面驱动器：把抽象控制信号转成红石/机械动作。

## 禁止倾向

- 不做单方块黑盒航空电脑。
- 不让航空学成为计算学核心玩法的硬前置，除非项目明确转向。
- 不把控制逻辑塞进 GUI 后台悄悄运行。
- 不只输出“成功/失败”，必须输出玩家能理解的中间量。
- 不在新方块缺少 Shift 说明、W/Ponder、各面作用说明时继续扩展功能。
- 不把旧版 Forge 附属源码直接复制到 NeoForge 1.21.1。

## 资料来源

- https://github.com/Creators-of-Aeronautics/Simulated-Project
- https://modrinth.com/mod/create-aeronautics/versions
- https://github.com/Cannoneers-of-Create/CreateBigCannons
- https://github.com/mrh0/createaddition
