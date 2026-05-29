package com.example.createaddon.registry;

import com.example.createaddon.CreateAddon;
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
