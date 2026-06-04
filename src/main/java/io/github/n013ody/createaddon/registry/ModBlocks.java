package io.github.n013ody.createaddon.registry;

import io.github.n013ody.createaddon.CreateAddon;
import io.github.n013ody.createaddon.content.TestBlock;
import io.github.n013ody.createaddon.content.computation.ComputingAssemblyBlock;
import io.github.n013ody.createaddon.content.guidance.computer.GuidanceComputerBlock;
import io.github.n013ody.createaddon.content.guidance.controller.GuidanceControllerBlock;
import io.github.n013ody.createaddon.content.guidance.controller.ServoMountBlock;
import io.github.n013ody.createaddon.content.guidance.sensor.ClosingSpeedSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.GuidanceSensorBlock;
import io.github.n013ody.createaddon.content.guidance.sensor.InertialDriftSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.LockOnProbabilitySensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.RelativeBearingSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.RelativeVelocityVectorSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.StructuralStressSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.TerrainAltimeterBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.LaserRangeFinderBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.LaserEmitterBlock;
import io.github.n013ody.createaddon.content.guidance.sensor.RadarIndexerBlockEntity;
import io.github.n013ody.createaddon.content.guidance.frame.RotaryFrameBlock;
import io.github.n013ody.createaddon.content.guidance.link.SensorBinderItem;
import io.github.n013ody.createaddon.content.guidance.threshold.ThresholdControllerBlock;
import io.github.n013ody.createaddon.content.semiconductor.ChipMachineBlock;
import io.github.n013ody.createaddon.content.semiconductor.CrystalPullFurnaceBlock;
import io.github.n013ody.createaddon.content.semiconductor.OxidationDiffusionFurnaceBlock;
import io.github.n013ody.createaddon.content.semiconductor.PhotolithographyTableBlock;
import io.github.n013ody.createaddon.content.semiconductor.WaferPolisherBlock;
import io.github.n013ody.createaddon.content.semiconductor.WaferSawBlock;
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
import java.util.List;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateAddon.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateAddon.MOD_ID);

    public static final DeferredBlock<Block> TIN_ORE = BLOCKS.register("tin_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 3.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = BLOCKS.register("deepslate_tin_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEEPSLATE_TUNGSTEN_ORE = BLOCKS.register("deepslate_tungsten_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.5F, 6.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> TIN_ORE_ITEM = ITEMS.register("tin_ore",
            () -> new BlockItem(TIN_ORE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> DEEPSLATE_TIN_ORE_ITEM = ITEMS.register("deepslate_tin_ore",
            () -> new BlockItem(DEEPSLATE_TIN_ORE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> DEEPSLATE_TUNGSTEN_ORE_ITEM = ITEMS.register("deepslate_tungsten_ore",
            () -> new BlockItem(DEEPSLATE_TUNGSTEN_ORE.get(), new Item.Properties()));

    public static final DeferredItem<Item> RAW_TIN = ITEMS.register("raw_tin",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TIN_INGOT = ITEMS.register("tin_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_TUNGSTEN = ITEMS.register("raw_tungsten",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TUNGSTEN_INGOT = ITEMS.register("tungsten_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredBlock<Block> CRYSTAL_PULL_FURNACE = BLOCKS.register("crystal_pull_furnace",
            () -> new CrystalPullFurnaceBlock(machineProperties()));
    public static final DeferredBlock<Block> WAFER_SAW = BLOCKS.register("wafer_saw",
            () -> new WaferSawBlock(machineProperties()));
    public static final DeferredBlock<Block> WAFER_POLISHER = BLOCKS.register("wafer_polisher",
            () -> new WaferPolisherBlock(machineProperties()));
    public static final DeferredBlock<Block> OXIDATION_DIFFUSION_FURNACE = BLOCKS.register("oxidation_diffusion_furnace",
            () -> new OxidationDiffusionFurnaceBlock(machineProperties()));
    public static final DeferredBlock<Block> PHOTOLITHOGRAPHY_TABLE = BLOCKS.register("photolithography_table",
            () -> new PhotolithographyTableBlock(machineProperties()));
    public static final DeferredBlock<Block> PACKAGING_TEST_BENCH = registerMachineBlock("packaging_test_bench");

    public static final DeferredItem<BlockItem> CRYSTAL_PULL_FURNACE_ITEM = registerMachineItem("crystal_pull_furnace", CRYSTAL_PULL_FURNACE);
    public static final DeferredItem<BlockItem> WAFER_SAW_ITEM = registerMachineItem("wafer_saw", WAFER_SAW);
    public static final DeferredItem<BlockItem> WAFER_POLISHER_ITEM = registerMachineItem("wafer_polisher", WAFER_POLISHER);
    public static final DeferredItem<BlockItem> OXIDATION_DIFFUSION_FURNACE_ITEM = registerMachineItem("oxidation_diffusion_furnace", OXIDATION_DIFFUSION_FURNACE);
    public static final DeferredItem<BlockItem> PHOTOLITHOGRAPHY_TABLE_ITEM = registerMachineItem("photolithography_table", PHOTOLITHOGRAPHY_TABLE);
    public static final DeferredItem<BlockItem> PACKAGING_TEST_BENCH_ITEM = registerMachineItem("packaging_test_bench", PACKAGING_TEST_BENCH);

    public static final DeferredItem<Item> SILICA_DUST = ITEMS.register("silica_dust",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_SILICON = ITEMS.register("raw_silicon",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SILICON_BOULE = ITEMS.register("silicon_boule",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROUGH_WAFER = ITEMS.register("rough_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POLISHED_WAFER = ITEMS.register("polished_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OXIDIZED_WAFER = ITEMS.register("oxidized_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PHOTORESIST_WAFER = ITEMS.register("photoresist_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ETCHED_WAFER = ITEMS.register("etched_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DOPED_WAFER = ITEMS.register("doped_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> METALLIZED_WAFER = ITEMS.register("metallized_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FINISHED_WAFER = ITEMS.register("finished_wafer",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BARE_LOGIC_DIE = ITEMS.register("bare_logic_die",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CERAMIC_CHIP_PACKAGE = ITEMS.register("ceramic_chip_package",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BASIC_LOGIC_CHIP = ITEMS.register("basic_logic_chip",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BLANK_TABLE_PAPER = ITEMS.register("blank_table_paper",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BASIC_PROCESSING_CURVE_TABLE = ITEMS.register("basic_processing_curve_table",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLANK_PUNCHED_CARD = ITEMS.register("blank_punched_card",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MACHINE_CALIBRATION_CARD = ITEMS.register("machine_calibration_card",
            () -> new Item(new Item.Properties()));

    public static final DeferredBlock<Block> FUNCTION_SETTING_DRUM = registerComputationBlock("function_setting_drum");
    public static final DeferredBlock<Block> DIFFERENCE_REGISTER_COLUMN = registerComputationBlock("difference_register_column");
    public static final DeferredBlock<ComputingAssemblyBlock> TABLE_PRINTER = BLOCKS.register("table_printer",
            () -> new ComputingAssemblyBlock(machineProperties(), BLANK_TABLE_PAPER, BASIC_PROCESSING_CURVE_TABLE, 180,
                    "message.createaddon.computation.started_table",
                    List.of(
                            new ComputingAssemblyBlock.ComponentRequirement(FUNCTION_SETTING_DRUM, 1,
                                    "block.createaddon.function_setting_drum"),
                            new ComputingAssemblyBlock.ComponentRequirement(DIFFERENCE_REGISTER_COLUMN, 2,
                                    "block.createaddon.difference_register_column"))));

    public static final DeferredBlock<Block> PUNCHED_CARD_READER = registerComputationBlock("punched_card_reader");
    public static final DeferredBlock<Block> MILL_ARITHMETIC_BLOCK = registerComputationBlock("mill_arithmetic_block");
    public static final DeferredBlock<Block> STORE_MEMORY_COLUMN = registerComputationBlock("store_memory_column");
    public static final DeferredBlock<ComputingAssemblyBlock> MECHANICAL_PRINTER = BLOCKS.register("mechanical_printer",
            () -> new ComputingAssemblyBlock(machineProperties(), BASIC_PROCESSING_CURVE_TABLE, MACHINE_CALIBRATION_CARD, 220,
                    "message.createaddon.computation.started_card",
                    List.of(
                            new ComputingAssemblyBlock.ComponentRequirement(PUNCHED_CARD_READER, 1,
                                    "block.createaddon.punched_card_reader"),
                            new ComputingAssemblyBlock.ComponentRequirement(MILL_ARITHMETIC_BLOCK, 1,
                                    "block.createaddon.mill_arithmetic_block"),
                            new ComputingAssemblyBlock.ComponentRequirement(STORE_MEMORY_COLUMN, 1,
                                    "block.createaddon.store_memory_column"))));

    public static final DeferredItem<BlockItem> FUNCTION_SETTING_DRUM_ITEM = registerMachineItem("function_setting_drum", FUNCTION_SETTING_DRUM);
    public static final DeferredItem<BlockItem> DIFFERENCE_REGISTER_COLUMN_ITEM = registerMachineItem("difference_register_column", DIFFERENCE_REGISTER_COLUMN);
    public static final DeferredItem<BlockItem> TABLE_PRINTER_ITEM = registerMachineItem("table_printer", TABLE_PRINTER);
    public static final DeferredItem<BlockItem> PUNCHED_CARD_READER_ITEM = registerMachineItem("punched_card_reader", PUNCHED_CARD_READER);
    public static final DeferredItem<BlockItem> MILL_ARITHMETIC_BLOCK_ITEM = registerMachineItem("mill_arithmetic_block", MILL_ARITHMETIC_BLOCK);
    public static final DeferredItem<BlockItem> STORE_MEMORY_COLUMN_ITEM = registerMachineItem("store_memory_column", STORE_MEMORY_COLUMN);
    public static final DeferredItem<BlockItem> MECHANICAL_PRINTER_ITEM = registerMachineItem("mechanical_printer", MECHANICAL_PRINTER);

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

    public static final DeferredBlock<GuidanceControllerBlock> GUIDANCE_CONTROLLER = BLOCKS.register("guidance_controller",
            () -> new GuidanceControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> GUIDANCE_CONTROLLER_ITEM = ITEMS.register("guidance_controller",
            () -> new BlockItem(GUIDANCE_CONTROLLER.get(), new Item.Properties()));

    public static final DeferredBlock<ServoMountBlock> SERVO_MOUNT = BLOCKS.register("servo_mount",
            () -> new ServoMountBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> SERVO_MOUNT_ITEM = ITEMS.register("servo_mount",
            () -> new BlockItem(SERVO_MOUNT.get(), new Item.Properties()));

    public static final DeferredItem<SensorBinderItem> SENSOR_BINDER = ITEMS.register("sensor_binder",
            () -> new SensorBinderItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }

    private static <T extends io.github.n013ody.createaddon.content.guidance.sensor.AbstractSensorBlockEntity<?>> DeferredBlock<GuidanceSensorBlock<T>> registerSensorBlock(
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

    private static DeferredBlock<Block> registerMachineBlock(String name) {
        return BLOCKS.register(name,
                () -> new ChipMachineBlock(machineProperties()));
    }

    private static DeferredBlock<Block> registerComputationBlock(String name) {
        return BLOCKS.register(name,
                () -> new Block(machineProperties()));
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 8.0F)
                .sound(SoundType.COPPER)
                .requiresCorrectToolForDrops();
    }

    private static DeferredItem<BlockItem> registerMachineItem(String name, DeferredBlock<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}

