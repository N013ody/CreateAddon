package com.example.createaddon.registry;

import com.example.createaddon.CreateAddon;
import com.example.createaddon.content.TestBlock;
import com.example.createaddon.content.guidance.computer.GuidanceComputerBlock;
import com.example.createaddon.content.guidance.sensor.ClosingSpeedSensorBlockEntity;
import com.example.createaddon.content.guidance.sensor.GuidanceSensorBlock;
import com.example.createaddon.content.guidance.sensor.InertialDriftSensorBlockEntity;
import com.example.createaddon.content.guidance.sensor.LockOnProbabilitySensorBlockEntity;
import com.example.createaddon.content.guidance.sensor.RelativeBearingSensorBlockEntity;
import com.example.createaddon.content.guidance.sensor.RelativeVelocityVectorSensorBlockEntity;
import com.example.createaddon.content.guidance.sensor.StructuralStressSensorBlockEntity;
import com.example.createaddon.content.guidance.sensor.TerrainAltimeterBlockEntity;
import com.example.createaddon.content.guidance.sensor.LaserRangeFinderBlockEntity;
import com.example.createaddon.content.guidance.sensor.LaserEmitterBlock;
import com.example.createaddon.content.guidance.sensor.RadarIndexerBlockEntity;
import com.example.createaddon.content.guidance.frame.RotaryFrameBlock;
import com.example.createaddon.content.guidance.link.SensorBinderItem;
import com.example.createaddon.content.guidance.threshold.ThresholdControllerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateAddon.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateAddon.MOD_ID);

    public static final DeferredBlock<TestBlock> TESTBLOCK = BLOCKS.register("testblock",
            () -> new TestBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> TESTBLOCK_ITEM = ITEMS.register("testblock",
            () -> new BlockItem(TESTBLOCK.get(), new Item.Properties()));

    public static final DeferredBlock<GuidanceComputerBlock> GUIDANCE_COMPUTER = BLOCKS.register("guidance_computer",
            () -> new GuidanceComputerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> GUIDANCE_COMPUTER_ITEM = ITEMS.register("guidance_computer",
            () -> new BlockItem(GUIDANCE_COMPUTER.get(), new Item.Properties()));

    public static final DeferredBlock<GuidanceSensorBlock<RelativeBearingSensorBlockEntity>> RELATIVE_BEARING_SENSOR = registerSensorBlock(
            "relative_bearing_sensor", RelativeBearingSensorBlockEntity.class, () -> ModBlockEntities.RELATIVE_BEARING_SENSOR.get());
    public static final DeferredBlock<GuidanceSensorBlock<ClosingSpeedSensorBlockEntity>> CLOSING_SPEED_SENSOR = registerSensorBlock(
            "closing_speed_sensor", ClosingSpeedSensorBlockEntity.class, () -> ModBlockEntities.CLOSING_SPEED_SENSOR.get());
    public static final DeferredBlock<GuidanceSensorBlock<TerrainAltimeterBlockEntity>> TERRAIN_ALTIMETER = registerSensorBlock(
            "terrain_altimeter", TerrainAltimeterBlockEntity.class, () -> ModBlockEntities.TERRAIN_ALTIMETER.get());
    public static final DeferredBlock<GuidanceSensorBlock<InertialDriftSensorBlockEntity>> INERTIAL_DRIFT_SENSOR = registerSensorBlock(
            "inertial_drift_sensor", InertialDriftSensorBlockEntity.class, () -> ModBlockEntities.INERTIAL_DRIFT_SENSOR.get());
    public static final DeferredBlock<GuidanceSensorBlock<LockOnProbabilitySensorBlockEntity>> LOCK_ON_PROBABILITY_SENSOR = registerSensorBlock(
            "lock_on_probability_sensor", LockOnProbabilitySensorBlockEntity.class, () -> ModBlockEntities.LOCK_ON_PROBABILITY_SENSOR.get());
    public static final DeferredBlock<GuidanceSensorBlock<StructuralStressSensorBlockEntity>> STRUCTURAL_STRESS_SENSOR = registerSensorBlock(
            "structural_stress_sensor", StructuralStressSensorBlockEntity.class, () -> ModBlockEntities.STRUCTURAL_STRESS_SENSOR.get());
    public static final DeferredBlock<GuidanceSensorBlock<RelativeVelocityVectorSensorBlockEntity>> RELATIVE_VELOCITY_VECTOR_SENSOR = registerSensorBlock(
            "relative_velocity_vector_sensor", RelativeVelocityVectorSensorBlockEntity.class, () -> ModBlockEntities.RELATIVE_VELOCITY_VECTOR_SENSOR.get());

    public static final DeferredBlock<GuidanceSensorBlock<RadarIndexerBlockEntity>> RADAR_INDEXER = registerSensorBlock(
            "radar_indexer", RadarIndexerBlockEntity.class, () -> ModBlockEntities.RADAR_INDEXER.get());

    public static final DeferredBlock<LaserEmitterBlock> LASER_RANGE_FINDER = BLOCKS.register("laser_range_finder",
            () -> new LaserEmitterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> RELATIVE_BEARING_SENSOR_ITEM = registerSensorItem("relative_bearing_sensor", RELATIVE_BEARING_SENSOR);
    public static final DeferredItem<BlockItem> CLOSING_SPEED_SENSOR_ITEM = registerSensorItem("closing_speed_sensor", CLOSING_SPEED_SENSOR);
    public static final DeferredItem<BlockItem> TERRAIN_ALTIMETER_ITEM = registerSensorItem("terrain_altimeter", TERRAIN_ALTIMETER);
    public static final DeferredItem<BlockItem> INERTIAL_DRIFT_SENSOR_ITEM = registerSensorItem("inertial_drift_sensor", INERTIAL_DRIFT_SENSOR);
    public static final DeferredItem<BlockItem> LOCK_ON_PROBABILITY_SENSOR_ITEM = registerSensorItem("lock_on_probability_sensor", LOCK_ON_PROBABILITY_SENSOR);
    public static final DeferredItem<BlockItem> STRUCTURAL_STRESS_SENSOR_ITEM = registerSensorItem("structural_stress_sensor", STRUCTURAL_STRESS_SENSOR);
    public static final DeferredItem<BlockItem> RELATIVE_VELOCITY_VECTOR_SENSOR_ITEM = registerSensorItem("relative_velocity_vector_sensor", RELATIVE_VELOCITY_VECTOR_SENSOR);

    public static final DeferredItem<BlockItem> LASER_RANGE_FINDER_ITEM = registerSensorItem("laser_range_finder", LASER_RANGE_FINDER);
    public static final DeferredItem<BlockItem> RADAR_INDEXER_ITEM = registerSensorItem("radar_indexer", RADAR_INDEXER);

    public static final DeferredBlock<ThresholdControllerBlock> THRESHOLD_CONTROLLER = BLOCKS.register("threshold_controller",
            () -> new ThresholdControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> THRESHOLD_CONTROLLER_ITEM = ITEMS.register("threshold_controller",
            () -> new BlockItem(THRESHOLD_CONTROLLER.get(), new Item.Properties()));

    public static final DeferredBlock<RotaryFrameBlock> ROTARY_FRAME = BLOCKS.register("rotary_frame",
            () -> new RotaryFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> ROTARY_FRAME_ITEM = ITEMS.register("rotary_frame",
            () -> new BlockItem(ROTARY_FRAME.get(), new Item.Properties()));

    public static final DeferredItem<SensorBinderItem> SENSOR_BINDER = ITEMS.register("sensor_binder",
            () -> new SensorBinderItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }

    private static <T extends com.example.createaddon.content.guidance.sensor.AbstractSensorBlockEntity<?>> DeferredBlock<GuidanceSensorBlock<T>> registerSensorBlock(
            String name, Class<T> blockEntityClass, java.util.function.Supplier<? extends net.minecraft.world.level.block.entity.BlockEntityType<? extends T>> blockEntityType) {
        return BLOCKS.register(name,
                () -> new GuidanceSensorBlock<>(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(2.0F, 6.0F)
                        .sound(SoundType.COPPER)
                        .noOcclusion()
                        .requiresCorrectToolForDrops(), blockEntityClass, blockEntityType));
    }

    private static DeferredItem<BlockItem> registerSensorItem(String name, DeferredBlock<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
