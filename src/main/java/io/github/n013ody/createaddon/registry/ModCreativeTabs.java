package io.github.n013ody.createaddon.registry;

import io.github.n013ody.createaddon.CreateAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateAddon.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> ModBlocks.GUIDANCE_COMPUTER_ITEM.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.createaddon"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.RAW_TIN.get());
                        output.accept(ModBlocks.TIN_INGOT.get());
                        output.accept(ModBlocks.RAW_TUNGSTEN.get());
                        output.accept(ModBlocks.TUNGSTEN_INGOT.get());
                        output.accept(ModBlocks.TIN_ORE_ITEM.get());
                        output.accept(ModBlocks.DEEPSLATE_TIN_ORE_ITEM.get());
                        output.accept(ModBlocks.DEEPSLATE_TUNGSTEN_ORE_ITEM.get());
                        output.accept(ModBlocks.CRYSTAL_PULL_FURNACE_ITEM.get());
                        output.accept(ModBlocks.WAFER_SAW_ITEM.get());
                        output.accept(ModBlocks.WAFER_POLISHER_ITEM.get());
                        output.accept(ModBlocks.OXIDATION_DIFFUSION_FURNACE_ITEM.get());
                        output.accept(ModBlocks.PHOTOLITHOGRAPHY_TABLE_ITEM.get());
                        output.accept(ModBlocks.PACKAGING_TEST_BENCH_ITEM.get());
                        output.accept(ModBlocks.SILICA_DUST.get());
                        output.accept(ModBlocks.RAW_SILICON.get());
                        output.accept(ModBlocks.SILICON_BOULE.get());
                        output.accept(ModBlocks.ROUGH_WAFER.get());
                        output.accept(ModBlocks.POLISHED_WAFER.get());
                        output.accept(ModBlocks.OXIDIZED_WAFER.get());
                        output.accept(ModBlocks.PHOTORESIST_WAFER.get());
                        output.accept(ModBlocks.ETCHED_WAFER.get());
                        output.accept(ModBlocks.DOPED_WAFER.get());
                        output.accept(ModBlocks.METALLIZED_WAFER.get());
                        output.accept(ModBlocks.FINISHED_WAFER.get());
                        output.accept(ModBlocks.BARE_LOGIC_DIE.get());
                        output.accept(ModBlocks.CERAMIC_CHIP_PACKAGE.get());
                        output.accept(ModBlocks.BASIC_LOGIC_CHIP.get());
                        output.accept(ModBlocks.BLANK_TABLE_PAPER.get());
                        output.accept(ModBlocks.BASIC_PROCESSING_CURVE_TABLE.get());
                        output.accept(ModBlocks.BLANK_PUNCHED_CARD.get());
                        output.accept(ModBlocks.MACHINE_CALIBRATION_CARD.get());
                        output.accept(ModBlocks.FUNCTION_SETTING_DRUM_ITEM.get());
                        output.accept(ModBlocks.DIFFERENCE_REGISTER_COLUMN_ITEM.get());
                        output.accept(ModBlocks.TABLE_PRINTER_ITEM.get());
                        output.accept(ModBlocks.PUNCHED_CARD_READER_ITEM.get());
                        output.accept(ModBlocks.MILL_ARITHMETIC_BLOCK_ITEM.get());
                        output.accept(ModBlocks.STORE_MEMORY_COLUMN_ITEM.get());
                        output.accept(ModBlocks.MECHANICAL_PRINTER_ITEM.get());
                        output.accept(ModBlocks.TESTBLOCK_ITEM.get());
                        output.accept(ModBlocks.SENSOR_BINDER.get());
                        output.accept(ModBlocks.GUIDANCE_COMPUTER_ITEM.get());
                        output.accept(ModBlocks.RELATIVE_BEARING_SENSOR_ITEM.get());
                        output.accept(ModBlocks.CLOSING_SPEED_SENSOR_ITEM.get());
                        output.accept(ModBlocks.TERRAIN_ALTIMETER_ITEM.get());
                        output.accept(ModBlocks.INERTIAL_DRIFT_SENSOR_ITEM.get());
                        output.accept(ModBlocks.LOCK_ON_PROBABILITY_SENSOR_ITEM.get());
                        output.accept(ModBlocks.STRUCTURAL_STRESS_SENSOR_ITEM.get());
                        output.accept(ModBlocks.RELATIVE_VELOCITY_VECTOR_SENSOR_ITEM.get());
                        output.accept(ModBlocks.LASER_RANGE_FINDER_ITEM.get());
                        output.accept(ModBlocks.RADAR_INDEXER_ITEM.get());
                        output.accept(ModBlocks.THRESHOLD_CONTROLLER_ITEM.get());
                        output.accept(ModBlocks.ROTARY_FRAME_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}

