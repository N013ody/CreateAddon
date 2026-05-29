package com.example.createaddon.client;

import com.example.createaddon.client.ponder.GuidancePonderPlugin;
import com.example.createaddon.registry.ModBlockEntities;
import com.example.createaddon.registry.ModBlocks;
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
    }

    private static void registerTooltips() {
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
        registerTooltip(ModBlocks.SENSOR_BINDER.get());
    }

    private static void registerTooltip(net.minecraft.world.item.Item item) {
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, Palette.STANDARD_CREATE));
    }
}
