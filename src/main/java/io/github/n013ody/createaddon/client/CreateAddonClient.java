package io.github.n013ody.createaddon.client;

import io.github.n013ody.createaddon.client.ponder.GuidancePonderPlugin;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import io.github.n013ody.createaddon.registry.ModBlocks;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper.Palette;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

public final class CreateAddonClient {
    private CreateAddonClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CreateAddonClient::clientSetup);
        modEventBus.addListener(CreateAddonClient::registerRenderers);
        modEventBus.addListener(GuidanceDebugHud::register);
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PonderIndex.addPlugin(new GuidancePonderPlugin());
            registerTooltips();
        });
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RELATIVE_BEARING_SENSOR.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CLOSING_SPEED_SENSOR.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TERRAIN_ALTIMETER.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INERTIAL_DRIFT_SENSOR.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LOCK_ON_PROBABILITY_SENSOR.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STRUCTURAL_STRESS_SENSOR.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RELATIVE_VELOCITY_VECTOR_SENSOR.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LASER_RANGE_FINDER.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADAR_INDEXER.get(), SensorDialBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ROTARY_FRAME.get(), RotaryFrameBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHIP_MACHINE.get(), ChipMachineBlockEntityRenderer::new);
    }

    private static void registerTooltips() {
        registerTooltip(ModBlocks.RAW_TIN.get());
        registerTooltip(ModBlocks.TIN_INGOT.get());
        registerTooltip(ModBlocks.RAW_TUNGSTEN.get());
        registerTooltip(ModBlocks.TUNGSTEN_INGOT.get());
        registerTooltip(ModBlocks.TIN_ORE_ITEM.get());
        registerTooltip(ModBlocks.DEEPSLATE_TIN_ORE_ITEM.get());
        registerTooltip(ModBlocks.DEEPSLATE_TUNGSTEN_ORE_ITEM.get());
        registerTooltip(ModBlocks.CRYSTAL_PULL_FURNACE_ITEM.get());
        registerTooltip(ModBlocks.WAFER_SAW_ITEM.get());
        registerTooltip(ModBlocks.WAFER_POLISHER_ITEM.get());
        registerTooltip(ModBlocks.OXIDATION_DIFFUSION_FURNACE_ITEM.get());
        registerTooltip(ModBlocks.PHOTOLITHOGRAPHY_TABLE_ITEM.get());
        registerTooltip(ModBlocks.PACKAGING_TEST_BENCH_ITEM.get());
        registerTooltip(ModBlocks.SILICA_DUST.get());
        registerTooltip(ModBlocks.RAW_SILICON.get());
        registerTooltip(ModBlocks.SILICON_BOULE.get());
        registerTooltip(ModBlocks.ROUGH_WAFER.get());
        registerTooltip(ModBlocks.POLISHED_WAFER.get());
        registerTooltip(ModBlocks.OXIDIZED_WAFER.get());
        registerTooltip(ModBlocks.PHOTORESIST_WAFER.get());
        registerTooltip(ModBlocks.ETCHED_WAFER.get());
        registerTooltip(ModBlocks.DOPED_WAFER.get());
        registerTooltip(ModBlocks.METALLIZED_WAFER.get());
        registerTooltip(ModBlocks.FINISHED_WAFER.get());
        registerTooltip(ModBlocks.BARE_LOGIC_DIE.get());
        registerTooltip(ModBlocks.CERAMIC_CHIP_PACKAGE.get());
        registerTooltip(ModBlocks.BASIC_LOGIC_CHIP.get());
        registerTooltip(ModBlocks.BLANK_TABLE_PAPER.get());
        registerTooltip(ModBlocks.BASIC_PROCESSING_CURVE_TABLE.get());
        registerTooltip(ModBlocks.BLANK_PUNCHED_CARD.get());
        registerTooltip(ModBlocks.MACHINE_CALIBRATION_CARD.get());
        registerTooltip(ModBlocks.FUNCTION_SETTING_DRUM_ITEM.get());
        registerTooltip(ModBlocks.DIFFERENCE_REGISTER_COLUMN_ITEM.get());
        registerTooltip(ModBlocks.TABLE_PRINTER_ITEM.get());
        registerTooltip(ModBlocks.PUNCHED_CARD_READER_ITEM.get());
        registerTooltip(ModBlocks.MILL_ARITHMETIC_BLOCK_ITEM.get());
        registerTooltip(ModBlocks.STORE_MEMORY_COLUMN_ITEM.get());
        registerTooltip(ModBlocks.MECHANICAL_PRINTER_ITEM.get());
        registerTooltip(ModBlocks.TESTBLOCK_ITEM.get());
        registerTooltip(ModBlocks.GUIDANCE_COMPUTER_ITEM.get());
        registerTooltip(ModBlocks.RELATIVE_BEARING_SENSOR_ITEM.get());
        registerTooltip(ModBlocks.CLOSING_SPEED_SENSOR_ITEM.get());
        registerTooltip(ModBlocks.TERRAIN_ALTIMETER_ITEM.get());
        registerTooltip(ModBlocks.INERTIAL_DRIFT_SENSOR_ITEM.get());
        registerTooltip(ModBlocks.LOCK_ON_PROBABILITY_SENSOR_ITEM.get());
        registerTooltip(ModBlocks.STRUCTURAL_STRESS_SENSOR_ITEM.get());
        registerTooltip(ModBlocks.RELATIVE_VELOCITY_VECTOR_SENSOR_ITEM.get());
        registerTooltip(ModBlocks.LASER_RANGE_FINDER_ITEM.get());
        registerTooltip(ModBlocks.RADAR_INDEXER_ITEM.get());
        registerTooltip(ModBlocks.THRESHOLD_CONTROLLER_ITEM.get());
        registerTooltip(ModBlocks.ROTARY_FRAME_ITEM.get());
        registerTooltip(ModBlocks.GUIDANCE_CONTROLLER_ITEM.get());
        registerTooltip(ModBlocks.SERVO_MOUNT_ITEM.get());
        registerTooltip(ModBlocks.SENSOR_BINDER.get());
    }

    private static void registerTooltip(net.minecraft.world.item.Item item) {
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, Palette.STANDARD_CREATE));
    }
}

