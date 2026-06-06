# 合并总结

## 合并日期
2026-06-06

## 合并内容

### 来源1: 你的版本 (Create-Computation)
- 独立的计算系统架构
- ComputingAssemblyBlock (继承 RotatedPillarKineticBlock)
- ComputingMachineBlockEntity
- ComputingBlockItem
- 完整的应力系统和速度因子
- 组件量化系统 (computationUnits)

### 来源2: 朋友的版本 (GitHub: N013ody/CreateAddon)
- GuidanceControllerBlock + GuidanceControllerBlockEntity
- ServoMountBlock + ServoMountBlockEntity

## 新增文件

```
src/main/java/io/github/n013ody/createaddon/content/guidance/controller/
├── GuidanceControllerBlock.java
├── GuidanceControllerBlockEntity.java
├── ServoMountBlock.java
└── ServoMountBlockEntity.java
```

## 更新的注册文件

### ModBlocks.java
- 添加了 GUIDANCE_CONTROLLER 方块注册
- 添加了 SERVO_MOUNT 方块注册
- 添加了对应的 BlockItem 注册

### ModBlockEntities.java
- 添加了 GUIDANCE_CONTROLLER 方块实体注册
- 添加了 SERVO_MOUNT 方块实体注册

## 功能说明

### GuidanceController (制导控制器)
- 继承 RotatedPillarKineticBlock，支持旋转力
- 从附近的制导计算机或绑定的传感器获取指令
- 输出方向舵、升降舵、油门三个控制通道
- 可通过 SENSOR_BINDER 绑定传感器

### ServoMount (伺服安装座)
- 控制 Create 的机械轴承和线性执行器
- 支持角度预设 (15°/30°/45°/90°/180°)
- 支持线性预设 (1-6格)
- 可切换控制通道 (RUDDER/ELEVATOR/THROTTLE)
- 可反转方向

## 下一步

1. 编译测试
2. 运行游戏验证功能
3. 添加资源文件 (blockstates, models, textures)
4. 添加配方和 loot tables
