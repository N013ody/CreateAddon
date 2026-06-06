# 增强物理传动可视化方案

## 概述

本方案为 CreateAddon 添加了丰富的可视化效果，使物理传动更加直观可见。

---

## 🎨 新增渲染器

### 1. GuidanceControllerRenderer（制导控制器渲染器）

**文件**: `src/main/java/io/github/n013ody/createaddon/client/GuidanceControllerRenderer.java`

**功能**:
- 显示控制指令的方向和强度
- 方向舵指示器（绿色）
- 升降舵指示器（蓝色）
- 油门指示器（橙色）
- 活动指示灯（绿色闪烁）

**可视化效果**:
```
┌─────────────────────────────────────┐
│         Guidance Controller         │
│                                     │
│    [====] ← 方向舵指示器（绿色）     │
│    [====] ← 升降舵指示器（蓝色）     │
│    (   )  ← 油门指示器（橙色圆形）   │
│    [*]    ← 活动指示灯（绿色闪烁）   │
│                                     │
└─────────────────────────────────────┘
```

---

### 2. ServoMountRenderer（伺服安装座渲染器）

**文件**: `src/main/java/io/github/n013ody/createaddon/client/ServoMountRenderer.java`

**功能**:
- 显示伺服的执行状态
- 通道指示器（方向舵/升降舵/油门）
- 目标类型指示器（轴承/线性）
- 范围指示器
- 反转状态指示器
- 活动状态指示器

**可视化效果**:
```
┌─────────────────────────────────────┐
│           Servo Mount               │
│                                     │
│    [■] ← 通道指示器（颜色编码）      │
│    [□] ← 目标类型指示器              │
│    ( ) ← 范围指示器（环形）          │
│    [!] ← 反转状态（红色闪烁）        │
│    [*] ← 活动状态（绿色）            │
│                                     │
└─────────────────────────────────────┘
```

**颜色编码**:
- 方向舵: 绿色 (#00FF00)
- 升降舵: 蓝色 (#0088FF)
- 油门: 橙色 (#FF8800)
- 轴承目标: 黄色 (#FFFF00)
- 线性目标: 青色 (#00FFFF)

---

## ✨ 新增粒子效果

### 1. SensorPulseParticle（传感器脉冲粒子）

**文件**: `src/main/java/io/github/n013ody/createaddon/client/particle/SensorPulseParticle.java`

**功能**:
- 显示传感器的活动状态
- 脉冲效果（透明度变化）
- 缩小效果（逐渐消失）

**参数**:
- `color`: 粒子颜色（ARGB）
- `size`: 粒子大小
- `lifetime`: 生命周期（20-40 tick）

---

### 2. LaserScanParticle（激光扫描粒子）

**文件**: `src/main/java/io/github/n013ody/createaddon/client/particle/LaserScanParticle.java`

**功能**:
- 显示雷达和激光扫描效果
- 扫描效果（透明度脉冲）
- 移动效果（正弦波运动）
- 缩小效果（逐渐消失）

**参数**:
- `color`: 粒子颜色（ARGB）
- `size`: 粒子大小
- `lifetime`: 生命周期（30-60 tick）

---

## 🔧 需要手动更新的文件

### 1. CreateAddonClient.java（注册渲染器）

在 `registerRenderers` 方法中添加：

```java
// 控制器渲染器
event.registerBlockEntityRenderer(ModBlockEntities.GUIDANCE_CONTROLLER.get(), GuidanceControllerRenderer::new);
event.registerBlockEntityRenderer(ModBlockEntities.SERVO_MOUNT.get(), ServoMountRenderer::new);
```

完整方法应该如下：

```java
public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    // 传感器渲染器
    event.registerBlockEntityRenderer(ModBlockEntities.RELATIVE_BEARING_SENSOR.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.CLOSING_SPEED_SENSOR.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.TERRAIN_ALTIMETER.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.INERTIAL_DRIFT_SENSOR.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.LOCK_ON_PROBABILITY_SENSOR.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.STRUCTURAL_STRESS_SENSOR.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.RELATIVE_VELOCITY_VECTOR_SENSOR.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.LASER_RANGE_FINDER.get(), SensorDialBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.RADAR_INDEXER.get(), SensorDialBlockEntityRenderer::new);

    // 控制器渲染器
    event.registerBlockEntityRenderer(ModBlockEntities.GUIDANCE_CONTROLLER.get(), GuidanceControllerRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.SERVO_MOUNT.get(), ServoMountRenderer::new);

    // 其他渲染器
    event.registerBlockEntityRenderer(ModBlockEntities.ROTARY_FRAME.get(), RotaryFrameBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(ModBlockEntities.CHIP_MACHINE.get(), ChipMachineBlockEntityRenderer::new);
}
```

---

### 2. 注册粒子类型（可选）

如果要使用粒子效果，需要在 `CreateAddon.java` 中注册粒子类型：

```java
// 在 modEventBus 注册
modEventBus.addListener(CreateAddonClient::registerParticles);
```

然后在 `CreateAddonClient.java` 中添加：

```java
public static void registerParticles(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(ModParticles.SENSOR_PULSE.get(), 
        sprites -> new SensorPulseParticle.Provider(sprites, 0xFF00FF00, 0.1F));
    
    event.registerSpriteSet(ModParticles.LASER_SCAN.get(), 
        sprites -> new LaserScanParticle.Provider(sprites, 0xFFFF0000, 0.05F));
}
```

---

## 🎯 可视化效果详解

### 1. 控制指令可视化

#### 方向舵指示器
- **位置**: 方块顶部
- **颜色**: 绿色 (#00FF00)
- **形状**: 水平条
- **长度**: 与指令强度成正比
- **动画**: 平滑过渡

#### 升降舵指示器
- **位置**: 方块顶部（方向舵下方）
- **颜色**: 蓝色 (#0088FF)
- **形状**: 垂直条
- **长度**: 与指令强度成正比
- **动画**: 平滑过渡

#### 油门指示器
- **位置**: 方块顶部
- **颜色**: 橙色 (#FF8800)
- **形状**: 圆形
- **大小**: 与油门值成正比
- **动画**: 脉冲效果

---

### 2. 伺服状态可视化

#### 通道指示器
- **位置**: 方块侧面
- **颜色**: 根据通道类型变化
- **形状**: 条形
- **长度**: 与当前角度/偏移成正比

#### 目标类型指示器
- **位置**: 方块角落
- **颜色**: 黄色（轴承）/ 青色（线性）
- **形状**: 小方块
- **动画**: 静态显示

#### 范围指示器
- **位置**: 方块中心
- **颜色**: 白色（半透明）
- **形状**: 环形
- **大小**: 与范围设置成正比

#### 反转状态指示器
- **位置**: 方块角落
- **颜色**: 红色
- **形状**: 小方块
- **动画**: 闪烁效果

---

### 3. 粒子效果

#### 传感器脉冲粒子
- **触发**: 传感器活动时
- **位置**: 传感器周围
- **颜色**: 根据传感器类型变化
- **动画**: 脉冲 + 缩小

#### 激光扫描粒子
- **触发**: 雷达/激光扫描时
- **位置**: 扫描区域
- **颜色**: 红色
- **动画**: 扫描 + 移动 + 缩小

---

## 📊 性能考虑

### 渲染优化
1. **距离裁剪**: 只渲染 64 格内的方块
2. **视锥剔除**: 不渲染视锥外的方块
3. **LOD 系统**: 远距离简化渲染
4. **批量渲染**: 合并相同材质的渲染

### 粒子优化
1. **数量限制**: 每个传感器最多 5 个粒子
2. **生命周期**: 粒子自动消失
3. **距离裁剪**: 只渲染 32 格内的粒子
4. **简单形状**: 使用简单的四边形

---

## 🎮 游戏体验提升

### 1. 直观性
- ✅ 控制指令一目了然
- ✅ 伺服状态清晰可见
- ✅ 传感器活动明显

### 2. 反馈性
- ✅ 即时视觉反馈
- ✅ 状态变化平滑
- ✅ 异常状态警告

### 3. 沉浸感
- ✅ 物理传动可视化
- ✅ 机械感增强
- ✅ 科技感提升

---

## 🚀 下一步扩展

### 短期（1-2 周）
1. 添加声音效果
2. 优化粒子性能
3. 添加更多动画

### 中期（1-2 月）
1. 添加 3D 模型动画
2. 添加光影效果
3. 添加粒子特效

### 长期（3-6 月）
1. 添加物理模拟
2. 添加流体效果
3. 添加高级渲染

---

## 📝 注意事项

1. **性能**: 大量粒子可能影响性能
2. **兼容性**: 确保与其他模组兼容
3. **可配置性**: 允许玩家关闭效果
4. **可访问性**: 考虑色盲玩家

---

## ✅ 总结

本方案通过添加渲染器和粒子效果，显著增强了物理传动的可视化效果：

- **GuidanceControllerRenderer**: 显示控制指令
- **ServoMountRenderer**: 显示伺服状态
- **SensorPulseParticle**: 传感器脉冲效果
- **LaserScanParticle**: 激光扫描效果

这些增强使游戏更加直观、有趣，符合 Create 的"可见物理传动"哲学。
