# Create: Guidance & Control -- Development Guide

> 本文档指导后续模组开发：代码架构、Create 规范、建模与材质风格、Blockbench 工作流。

---

## 一、重复设计分析

### 1.1 与 Create 原版的对比

| CreateAddon 方块 | Create 原版相似物 | 重复程度 | 分析 |
|---|---|---|---|
| **Structural Stress Sensor** | **Stressometer** | ⚠️ 低重复 | Stressometer 仅显示动能网络应力值（SU）。你的传感器融合了 4 个负载因子（网络应力/线速度/转速/加速度），输出 0-1 危险等级。功能不同，但**名称和视觉上容易混淆**。 |
| **Structural Stress Sensor** | **Speedometer** | ⚠️ 低重复 | Speedometer 仅显示 RPM。你的传感器读取 `getTheoreticalSpeed()` 作为 4 个因子之一。不构成重复。 |
| **Laser Range Finder** | 无直接对应 | ✅ 无重复 | Create 无距离测量方块。 |
| **Terrain Altimeter** | 无直接对应 | ✅ 无重复 | Create 无高度/海拔测量。 |
| **Radar Indexer** | **Content Observer** | ⚠️ 低重复 | Content Observer 检测容器中的物品。Radar Indexer 扫描实体位置。机制完全不同，但"观察/检测"的定位有交集。 |
| **Guidance Computer** | 无直接对应 | ✅ 无重复 | Create 无自动控制器/自动驾驶仪。 |
| 其余 5 个传感器 | 无 | ✅ 无重复 | Bearing、Closing Speed、Drift、Lock-on、Velocity Vector 在 Create 中无对应。 |

### 1.2 与 Create: Aeronautics 的对比

Create Aeronautics 包含三个模块：
- **Simulated**：物理载具组装、红石组件
- **Aeronautics**：热气球、螺旋翼、浮空石等飞行器建造
- **Offroad**：轮式陆地载具

**结论：Aeronautics 聚焦于载具建造与物理模拟，不包含任何传感器/仪表方块。与你的模组无功能重叠。**

### 1.3 建议

1. **Structural Stress Sensor** 重命名为 **Load Monitor** 或 **Structural Load Sensor**，避免与 Stressometer 的 "stress" 概念混淆。
2. 考虑在 tooltip 中明确说明 "不同于 Stressometer，本传感器综合速度、转速和加速度来评估结构危险程度"。
3. 所有 9 个传感器 + 1 个电脑 = 10 个功能性方块，与 Create 和 Aeronautics 均**无实质性重复**，设计定位清晰。

---

## 二、代码架构设计

### 2.1 分层架构（当前已实现，保持不变）

```
┌─────────────────────────────────────────────┐
│                  API Layer                   │
│  IValueProvider<T>  /  ISensor<T>            │
│  SensorReading<T>                            │
├─────────────────────────────────────────────┤
│               Sensor Layer                   │
│  AbstractSensorBlockEntity<T>                │
│  ├── DoubleSensorBlockEntity                 │
│ │   ├── RelativeBearingSensorBE              │
│ │   ├── ClosingSpeedSensorBE                 │
│ │   ├── TerrainAltimeterBE                   │
│ │   ├── InertialDriftSensorBE                │
│ │   ├── LockOnProbabilitySensorBE            │
│ │   ├── StructuralStressSensorBE             │
│ │   └── LaserRangeFinderBE                   │
│ └── Vec3SensorBlockEntity                    │
│     ├── RelativeVelocityVectorSensorBE       │
│     └── RadarIndexerBE                       │
├─────────────────────────────────────────────┤
│              Computer Layer                  │
│  GuidanceComputerBlockEntity                 │
│  GuidanceUseCases (pure math)                │
├─────────────────────────────────────────────┤
│              Client Layer                    │
│  HUD / Renderers / Ponder                    │
└─────────────────────────────────────────────┘
```

### 2.2 新增传感器的标准流程

遵循 Create 的 `KineticBlockEntity` 模式：

```java
// 1. 定义 BlockEntity
public class MyNewSensorBlockEntity extends DoubleSensorBlockEntity {

    public MyNewSensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0.0);
    }

    @Override
    public String getSensorId() {
        return "my_new_sensor";  // 唯一标识，用于 GuidanceComputer 按 ID 查询
    }

    @Override
    protected double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        // 你的测量逻辑
        return computedValue;
    }

    @Override
    protected double computeNeedleAngle(Double value) {
        return value * 180.0 - 90.0;  // 仪表盘指针角度
    }

    @Override
    protected double computeLampIntensity(Double value, boolean valid) {
        return valid ? clamp01(value) : 0.0;  // 指示灯亮度
    }
}

// 2. 注册到 ModBlockEntities
public static final DeferredBlockEntityType<MyNewSensorBE> MY_NEW_SENSOR =
    BLOCK_ENTITY_TYPES.register("my_new_sensor",
        () -> BlockEntityType.Builder.of(
            MyNewSensorBE::new, ModBlocks.MY_NEW_SENSOR.get()).build(null));

// 3. 注册到 ModBlocks (使用 registerSensorBlock 辅助方法)
public static final DeferredBlock<GuidanceSensorBlock<MyNewSensorBE>> MY_NEW_SENSOR =
    registerSensorBlock("my_new_sensor", MyNewSensorBE.class,
        () -> ModBlockEntities.MY_NEW_SENSOR.get());
```

### 2.3 对照 Create 规范的要点

| Create 规范 | 你的实现 | 建议 |
|---|---|---|
| `KineticBlockEntity` 作为旋转方块基类 | ✅ 已遵循 | 所有传感器继承 `AbstractSensorBlockEntity` → `KineticBlockEntity` |
| `IBE<T>` 接口管理 BlockEntity | ✅ 已遵循 | `GuidanceSensorBlock` 实现 `IBE<T>` |
| `RotatedPillarKineticBlock` 作为旋转方块基类 | ✅ 已遵循 | `GuidanceSensorBlock extends RotatedPillarKineticBlock` |
| `DirectionalKineticBlock` 用于方向性方块 | ✅ 已遵循 | `LaserEmitterBlock extends DirectionalKineticBlock` |
| `SmartBlockEntity` 用于非旋转逻辑方块 | ✅ 已遵循 | `GuidanceComputerBlockEntity extends SmartBlockEntity` |
| 使用 `addBehaviours()` 注册行为 | ⚠️ 空实现 | 当前为空。如需添加 link/显示行为，应在此注册 |
| 使用 Ponder 场景做教程 | ✅ 已实现 | 已有 `GuidancePonderScenes` |
| 使用 `BlockEntityBehaviour` 模式 | ❌ 未使用 | 建议将 targeting 逻辑抽取为独立 Behaviour |

### 2.4 建议的架构改进

#### 2.4.1 将 Targeting 抽取为 BlockEntityBehaviour

```java
public class TargetingBehaviour extends BlockEntityBehaviour {
    private TargetSpec targetSpec = new TargetSpec();

    @Override
    public void tick() {
        super.tick();
        // 自动更新目标
    }

    public TargetData getTarget() {
        return targetSpec.resolve(level, getPos());
    }
}
```

#### 2.4.2 使用 Create 的 Display Source/Target 系统

Create 的 `DisplayLink` 系统允许方块作为数据源/目标。你的传感器天然适合作为 `DisplaySource`：

```java
public class SensorDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        ISensor<?> sensor = context.getSourceBlockEntity();
        return List.of(Component.literal(String.valueOf(sensor.getValue())));
    }
}
```

#### 2.4.3 考虑 Redstone Link 集成

传感器输出可以通过 Create 的 `RedstoneLink` 发射红石信号，让传感器驱动非动能红石设备。

---

## 三、Create 规范开发指南

### 3.1 方块命名规范

| 类别 | Create 命名模式 | 你的示例 |
|---|---|---|
| 旋转方块 | `mechanical_*`、`*_wheel`、`*_engine` | `guidance_sensor_block` → `relative_bearing_sensor` ✅ |
| 方向方块 | `directional_*` 或功能名 | `laser_range_finder` ✅ |
| 非旋转方块 | 功能名 | `guidance_computer` ✅ |

### 3.2 物品栏/创造模式标签页

Create 使用 `Building Tools`、`Aesthetic Technology` 等分类。你已创建 `Create: Guidance & Control` 标签页，合理。

### 3.3 本地化键名规范

Create 格式：`create.方块注册名.tooltip.summary`

你的格式：`block.createaddon.方块注册名.tooltip.summary` ✅ 已遵循 Forge/NeoForge 标准。

### 3.4 方块状态 JSON 规范

Create 的旋转方块通常使用 `axis` 属性（X/Y/Z）：

```json
{
  "variants": {
    "axis=x": { "model": "createaddon:block/my_sensor", "y": 90 },
    "axis=y": { "model": "createaddon:block/my_sensor" },
    "axis=z": { "model": "createaddon:block/my_sensor", "y": 0 }
  }
}
```

### 3.5 注册模式

Create 使用 `DeferredRegister` 模式，你已遵循：

```java
// Create 的方式
public static final DeferredBlock<MyBlock> MY_BLOCK = BLOCKS.register("my_block", () -> new MyBlock(...));
public static final DeferredItem<BlockItem> MY_BLOCK_ITEM = ITEMS.register("my_block", () -> new BlockItem(MY_BLOCK.get(), ...));

// 你的方式 -- 完全一致 ✅
```

---

## 四、建模与材质风格指南

### 4.1 Create 的视觉设计语言

Create 方块遵循以下设计原则：

| 原则 | 说明 | 示例 |
|---|---|---|
| **工业蒸汽朋克** | 黄铜、铜、生铁、齿轮 | Brass casing, copper pipes |
| **可见的机械结构** | 齿轮、轴、皮带可见运转 | Cogwheel 有可见齿牙 |
| **16x16 像素基础** | 方块面为 16x16，细节通过 3D 出模实现 | 大多数方块 |
| **分层材质** | 基础层 + 金属层 + 细节层 | 方块有石质底层 + 金属外壳 |
| **色板一致** | 使用固定的 Create 调色板 | Andesite alloy 暗灰, Brass 金黄 |

### 4.2 传感器方块的建模建议

#### 风格定位

你的模组主题是 **Guidance & Control**（制导与控制），视觉风格应为：

- **精密仪器感**：类似航空仪表、航海罗盘、雷达屏幕
- **铜色 + 黄铜色主调**：与 Create 的金属系统一致
- **玻璃/透镜元素**：传感器需要"观察"外部世界
- **旋转部件**：雷达盘、陀螺仪、指针

#### 各方块的视觉概念

| 方块 | 建模概念 | 主要材质 |
|---|---|---|
| **Guidance Computer** | 控制台式机柜，带屏幕和旋钮 | 深色金属 + 黄铜边框 + 玻璃屏幕 |
| **Relative Bearing Sensor** | 罗盘/航向指示器，带旋转指针 | 黄铜底座 + 玻璃罩 + 指针 |
| **Closing Speed Sensor** | 多普勒雷达显示器 | 铜壳 + 圆形玻璃屏 |
| **Terrain Altimeter** | 高度计仪表，带压力表刻度盘 | 铜壳 + 玻璃 + 刻度盘 |
| **Inertial Drift Sensor** | 陀螺仪/惯性平台 | 黄铜 + 旋转环 |
| **Lock-on Probability Sensor** | 雷达锁定指示器/瞄准环 | 深色金属 + 十字准星玻璃 |
| **Structural Stress Sensor** | 应变仪/载荷指示器 | 铜 + 机械指针 |
| **Velocity Vector Sensor** | 3D 矢量显示球体 | 玻璃球 + 黄铜支架 |
| **Laser Range Finder** | 望远镜/激光发射器 | 铜管 + 透镜 |
| **Radar Indexer** | 旋转雷达天线/抛物面碟 | 金属支架 + 旋转碟面 |

### 4.3 材质制作规范

#### 纹理分辨率

- **标准**：16x16 像素（与 Minecraft/Create 一致）
- **可选**：32x32 用于精密仪表细节（但需保持整体风格一致）

#### 调色板参考（从 Create 提取）

```
Andesite Alloy:    #7A7A6A (暗灰)
Copper:            #B87333 (铜色)
Brass:             #CD9B1D (黄铜金)
Industrial Iron:   #5A5A5A (深铁灰)
Weathered Copper:  #5C8A6A (铜绿)
```

#### 材质文件结构

```
assets/createaddon/textures/block/
├── guidance_computer.png          # 方块纹理
├── guidance_computer_front.png    # 前面（屏幕）
├── guidance_sensor_side.png       # 通用传感器侧面
├── guidance_sensor_top.png        # 通用传感器顶面
├── laser_range_finder_front.png   # 激光发射面
├── radar_dish.png                 # 雷达碟面
└── ...
```

---

## 五、Blockbench 工作流

### 5.1 方块建模流程

#### 步骤 1：创建新项目

1. 打开 Blockbench
2. 选择 **Block** 模式（不是 Entity 或 Item）
3. 设置网格为 16x16x16

#### 步骤 2：基础形状

1. 使用 **Cube** 工具创建基础方块形状
2. 参考 Create 方块的多边形数量：
   - 简单方块：1-3 个 cube
   - 中等复杂度：4-8 个 cube（如 Speedometer）
   - 复杂方块：8-15 个 cube（如 Mechanical Crafter）

#### 步骤 3：UV 映射

1. 选择面 → 在 UV 面板中调整纹理坐标
2. 确保每个面的 UV 对齐 16x16 网格
3. 使用 **Paint** 模式直接在模型上绘制纹理

#### 步骤 4：导出设置

1. **File → Export → Export Block/Item Model**
2. 导出路径：`src/main/resources/assets/createaddon/models/block/`
3. 格式：`.json`（Forge/NeoForge 标准格式）

### 5.2 已有 Blockbench 文件

你的项目中已有：
- `guidance_computer.bbmodel` -- 指引电脑
- `guidance_sensor.bbmodel` -- 通用传感器模型
- `sensor_model_design_notes.bbmodel` -- 设计笔记

### 5.3 模型 JSON 规范

#### 方块模型 (block model)

```json
{
  "parent": "block/block",
  "textures": {
    "particle": "createaddon:block/guidance_sensor_side",
    "side": "createaddon:block/guidance_sensor_side",
    "top": "createaddon:block/guidance_sensor_top",
    "bottom": "createaddon:block/guidance_sensor_bottom"
  },
  "elements": [
    {
      "from": [0, 0, 0],
      "to": [16, 16, 16],
      "faces": {
        "north": { "texture": "#side", "uv": [0, 0, 16, 16] },
        "south": { "texture": "#side", "uv": [0, 0, 16, 16] },
        "east":  { "texture": "#side", "uv": [0, 0, 16, 16] },
        "west":  { "texture": "#side", "uv": [0, 0, 16, 16] },
        "up":    { "texture": "#top",  "uv": [0, 0, 16, 16] },
        "down":  { "texture": "#bottom", "uv": [0, 0, 16, 16] }
      }
    },
    {
      "name": "Gauge",
      "from": [3, 16, 3],
      "to": [13, 18, 13],
      "faces": {
        "up": { "texture": "#gauge_face", "uv": [3, 3, 13, 13] },
        "north": { "texture": "#side", "uv": [3, 0, 13, 2] },
        "south": { "texture": "#side", "uv": [3, 0, 13, 2] },
        "east":  { "texture": "#side", "uv": [3, 0, 13, 2] },
        "west":  { "texture": "#side", "uv": [3, 0, 13, 2] }
      }
    }
  ]
}
```

#### 物品模型 (item model)

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "createaddon:item/relative_bearing_sensor"
  }
}
```

### 5.4 与 Flywheel 的关系

Create 6.0+ 使用 **Flywheel** 进行实例化渲染。对于自定义方块渲染（如旋转指针）：

```java
// BlockEntityRenderer 用于动态渲染
public class SensorDialRenderer extends BlockEntityRenderer<SensorBlockEntity> {
    @Override
    public void render(SensorBlockEntity be, float partialTick, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        // 渲染旋转指针
        float angle = (float) be.getNeedleAngleDegrees();
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(angle));
        // 渲染指针模型...
    }
}
```

---

## 六、剩余开发任务清单

### 6.1 优先级 P0（核心功能完善）

- [ ] **BlockEntityRenderer**：为所有传感器实现指针/仪表盘渲染器
- [ ] **Ponder 场景**：确保所有 10 个方块都有 Ponder 教程
- [ ] **配方系统**：添加所有传感器的合成配方（使用 Create 材料：andesite alloy, brass, copper）
- [ ] **战利品表**：确认所有方块的战利品表正确

### 6.2 优先级 P1（功能扩展）

- [ ] **Display Source 集成**：让传感器可以作为 Display Link 数据源
- [ ] **Redstone Link 兼容**：通过 Redstone Link 传输传感器值
- [ ] **ComputerCraft/CC:Tweaked 集成**：暴露传感器为外设
- [ ] **Goggle HUD 集成**：佩戴工程师护目镜时显示传感器读数

### 6.3 优先级 P2（质量提升）

- [ ] **数据生成器 (DataGen)**：用代码生成 blockstate/model/loot/recipe JSON
- [ ] **高级目标系统**：支持方块坐标目标、区域扫描
- [ ] **传感器校准机制**：旋转速度影响精度
- [ ] **更多控制算法**：PID 控制器、比例导航等

### 6.4 优先级 P3（视觉打磨）

- [ ] **自定义粒子效果**：激光束粒子、雷达扫描波纹
- [ ] **方块动画**：雷达碟旋转、陀螺仪摆动
- [ ] **音效**：机械运转声、锁定蜂鸣声
- [ ] **高精度纹理**：32x32 细节纹理（可选）

---

## 七、参考资源

| 资源 | URL |
|---|---|
| Create Wiki | https://wiki.createmod.net |
| Create GitHub | https://github.com/Creators-of-Create/Create |
| Create Aeronautics | https://github.com/Creators-of-Aeronautics/Simulated-Project |
| NeoForge Docs | https://docs.neoforged.net |
| Blockbench | https://www.blockbench.net |
| Create Ponder (教程系统) | https://wiki.createmod.net/developers |
| Flywheel (渲染引擎) | https://github.com/Jozufozu/Flywheel |
