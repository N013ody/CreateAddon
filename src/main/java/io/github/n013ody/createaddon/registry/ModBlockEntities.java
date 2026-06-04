package io.github.n013ody.createaddon.registry;

import io.github.n013ody.createaddon.CreateAddon;
import io.github.n013ody.createaddon.content.guidance.controller.GuidanceControllerBlockEntity;
import io.github.n013ody.createaddon.content.guidance.controller.ServoMountBlockEntity;
import io.github.n013ody.createaddon.content.TestBlockEntity;
import io.github.n013ody.createaddon.content.guidance.computer.GuidanceComputerBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.ClosingSpeedSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.InertialDriftSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.LockOnProbabilitySensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.RelativeBearingSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.RelativeVelocityVectorSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.StructuralStressSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.TerrainAltimeterBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.LaserRangeFinderBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.RadarIndexerBlockEntity;
import io.github.n013ody.createaddon.content.guidance.frame.RotaryFrameBlockEntity;
import io.github.n013ody.createaddon.content.guidance.threshold.ThresholdControllerBlockEntity;
import io.github.n013ody.createaddon.content.semiconductor.ChipMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateAddon.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestBlockEntity>> TESTBLOCK = BLOCK_ENTITIES.register("testblock",
            () -> BlockEntityType.Builder.of(TestBlockEntity::new, ModBlocks.TESTBLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GuidanceComputerBlockEntity>> GUIDANCE_COMPUTER = BLOCK_ENTITIES.register("guidance_computer",
            () -> BlockEntityType.Builder.of(GuidanceComputerBlockEntity::new, ModBlocks.GUIDANCE_COMPUTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RelativeBearingSensorBlockEntity>> RELATIVE_BEARING_SENSOR = BLOCK_ENTITIES.register("relative_bearing_sensor",
            () -> BlockEntityType.Builder.of(RelativeBearingSensorBlockEntity::new, ModBlocks.RELATIVE_BEARING_SENSOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClosingSpeedSensorBlockEntity>> CLOSING_SPEED_SENSOR = BLOCK_ENTITIES.register("closing_speed_sensor",
            () -> BlockEntityType.Builder.of(ClosingSpeedSensorBlockEntity::new, ModBlocks.CLOSING_SPEED_SENSOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TerrainAltimeterBlockEntity>> TERRAIN_ALTIMETER = BLOCK_ENTITIES.register("terrain_altimeter",
            () -> BlockEntityType.Builder.of(TerrainAltimeterBlockEntity::new, ModBlocks.TERRAIN_ALTIMETER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InertialDriftSensorBlockEntity>> INERTIAL_DRIFT_SENSOR = BLOCK_ENTITIES.register("inertial_drift_sensor",
            () -> BlockEntityType.Builder.of(InertialDriftSensorBlockEntity::new, ModBlocks.INERTIAL_DRIFT_SENSOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LockOnProbabilitySensorBlockEntity>> LOCK_ON_PROBABILITY_SENSOR = BLOCK_ENTITIES.register("lock_on_probability_sensor",
            () -> BlockEntityType.Builder.of(LockOnProbabilitySensorBlockEntity::new, ModBlocks.LOCK_ON_PROBABILITY_SENSOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StructuralStressSensorBlockEntity>> STRUCTURAL_STRESS_SENSOR = BLOCK_ENTITIES.register("structural_stress_sensor",
            () -> BlockEntityType.Builder.of(StructuralStressSensorBlockEntity::new, ModBlocks.STRUCTURAL_STRESS_SENSOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RelativeVelocityVectorSensorBlockEntity>> RELATIVE_VELOCITY_VECTOR_SENSOR = BLOCK_ENTITIES.register("relative_velocity_vector_sensor",
            () -> BlockEntityType.Builder.of(RelativeVelocityVectorSensorBlockEntity::new, ModBlocks.RELATIVE_VELOCITY_VECTOR_SENSOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserRangeFinderBlockEntity>> LASER_RANGE_FINDER = BLOCK_ENTITIES.register("laser_range_finder",
            () -> BlockEntityType.Builder.of(LaserRangeFinderBlockEntity::new, ModBlocks.LASER_RANGE_FINDER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadarIndexerBlockEntity>> RADAR_INDEXER = BLOCK_ENTITIES.register("radar_indexer",
            () -> BlockEntityType.Builder.of(RadarIndexerBlockEntity::new, ModBlocks.RADAR_INDEXER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThresholdControllerBlockEntity>> THRESHOLD_CONTROLLER = BLOCK_ENTITIES.register("threshold_controller",
            () -> BlockEntityType.Builder.of(ThresholdControllerBlockEntity::new, ModBlocks.THRESHOLD_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RotaryFrameBlockEntity>> ROTARY_FRAME = BLOCK_ENTITIES.register("rotary_frame",
            () -> BlockEntityType.Builder.of(RotaryFrameBlockEntity::new, ModBlocks.ROTARY_FRAME.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChipMachineBlockEntity>> CHIP_MACHINE = BLOCK_ENTITIES.register("chip_machine",
            () -> BlockEntityType.Builder.of(ChipMachineBlockEntity::new,
                    ModBlocks.CRYSTAL_PULL_FURNACE.get(),
                    ModBlocks.WAFER_SAW.get(),
                    ModBlocks.WAFER_POLISHER.get(),
                    ModBlocks.OXIDATION_DIFFUSION_FURNACE.get(),
                    ModBlocks.PHOTOLITHOGRAPHY_TABLE.get(),
                    ModBlocks.PACKAGING_TEST_BENCH.get(),
                    ModBlocks.TABLE_PRINTER.get(),
                    ModBlocks.MECHANICAL_PRINTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GuidanceControllerBlockEntity>> GUIDANCE_CONTROLLER = BLOCK_ENTITIES.register("guidance_controller",
            () -> BlockEntityType.Builder.of(GuidanceControllerBlockEntity::new, ModBlocks.GUIDANCE_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServoMountBlockEntity>> SERVO_MOUNT = BLOCK_ENTITIES.register("servo_mount",
            () -> BlockEntityType.Builder.of(ServoMountBlockEntity::new, ModBlocks.SERVO_MOUNT.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

