package com.example.createaddon.client.ponder;

import com.example.createaddon.CreateAddon;
import com.example.createaddon.registry.ModBlocks;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public final class GuidancePonderScenes {
    private GuidancePonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(item -> ResourceLocation.fromNamespaceAndPath(
                CreateAddon.MOD_ID, item.asItem().builtInRegistryHolder().key().location().getPath()));

        itemHelper.forComponents(ModBlocks.GUIDANCE_COMPUTER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::guidanceComputer, GuidancePonderTags.GUIDANCE_CONTROL);

        itemHelper.forComponents(ModBlocks.RELATIVE_BEARING_SENSOR.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::relativeBearing, GuidancePonderTags.SENSOR_INPUTS);
        itemHelper.forComponents(ModBlocks.CLOSING_SPEED_SENSOR.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::closingSpeed, GuidancePonderTags.SENSOR_INPUTS);
        itemHelper.forComponents(ModBlocks.TERRAIN_ALTIMETER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::terrainAltimeter, GuidancePonderTags.SENSOR_INPUTS);
        itemHelper.forComponents(ModBlocks.INERTIAL_DRIFT_SENSOR.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::inertialDrift, GuidancePonderTags.SENSOR_INPUTS);
        itemHelper.forComponents(ModBlocks.LOCK_ON_PROBABILITY_SENSOR.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::lockOnProbability, GuidancePonderTags.SENSOR_INPUTS);
        itemHelper.forComponents(ModBlocks.STRUCTURAL_STRESS_SENSOR.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::structuralStress, GuidancePonderTags.SENSOR_INPUTS);
        itemHelper.forComponents(ModBlocks.RELATIVE_VELOCITY_VECTOR_SENSOR.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::relativeVelocityVector, GuidancePonderTags.SENSOR_INPUTS);

        itemHelper.forComponents(ModBlocks.LASER_RANGE_FINDER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::laserRangeFinder, GuidancePonderTags.SENSOR_INPUTS);
        itemHelper.forComponents(ModBlocks.RADAR_INDEXER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::radarIndexer, GuidancePonderTags.SENSOR_INPUTS);
    }
}
