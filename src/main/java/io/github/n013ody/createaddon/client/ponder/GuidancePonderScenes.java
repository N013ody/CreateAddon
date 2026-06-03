package io.github.n013ody.createaddon.client.ponder;

import io.github.n013ody.createaddon.CreateAddon;
import io.github.n013ody.createaddon.registry.ModBlocks;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public final class GuidancePonderScenes {
    private GuidancePonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(item -> ResourceLocation.fromNamespaceAndPath(
                CreateAddon.MOD_ID, item.asItem().builtInRegistryHolder().key().location().getPath()));

        itemHelper.forComponents(ModBlocks.TIN_ORE.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::materialOres, GuidancePonderTags.SEMICONDUCTOR_CHAIN);
        itemHelper.forComponents(ModBlocks.DEEPSLATE_TIN_ORE.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::materialOres, GuidancePonderTags.SEMICONDUCTOR_CHAIN);
        itemHelper.forComponents(ModBlocks.DEEPSLATE_TUNGSTEN_ORE.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::materialOres, GuidancePonderTags.SEMICONDUCTOR_CHAIN);

        itemHelper.forComponents(ModBlocks.CRYSTAL_PULL_FURNACE.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::semiconductorMachines, GuidancePonderTags.SEMICONDUCTOR_CHAIN);
        itemHelper.forComponents(ModBlocks.WAFER_SAW.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::semiconductorMachines, GuidancePonderTags.SEMICONDUCTOR_CHAIN);
        itemHelper.forComponents(ModBlocks.WAFER_POLISHER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::semiconductorMachines, GuidancePonderTags.SEMICONDUCTOR_CHAIN);
        itemHelper.forComponents(ModBlocks.OXIDATION_DIFFUSION_FURNACE.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::semiconductorMachines, GuidancePonderTags.SEMICONDUCTOR_CHAIN);
        itemHelper.forComponents(ModBlocks.PHOTOLITHOGRAPHY_TABLE.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::semiconductorMachines, GuidancePonderTags.SEMICONDUCTOR_CHAIN);
        itemHelper.forComponents(ModBlocks.PACKAGING_TEST_BENCH.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::semiconductorMachines, GuidancePonderTags.SEMICONDUCTOR_CHAIN);

        itemHelper.forComponents(ModBlocks.FUNCTION_SETTING_DRUM.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::differenceEngine, GuidancePonderTags.COMPUTATION_CHAIN);
        itemHelper.forComponents(ModBlocks.DIFFERENCE_REGISTER_COLUMN.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::differenceEngine, GuidancePonderTags.COMPUTATION_CHAIN);
        itemHelper.forComponents(ModBlocks.TABLE_PRINTER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::differenceEngine, GuidancePonderTags.COMPUTATION_CHAIN);
        itemHelper.forComponents(ModBlocks.PUNCHED_CARD_READER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::analyticalEngine, GuidancePonderTags.COMPUTATION_CHAIN);
        itemHelper.forComponents(ModBlocks.MILL_ARITHMETIC_BLOCK.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::analyticalEngine, GuidancePonderTags.COMPUTATION_CHAIN);
        itemHelper.forComponents(ModBlocks.STORE_MEMORY_COLUMN.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::analyticalEngine, GuidancePonderTags.COMPUTATION_CHAIN);
        itemHelper.forComponents(ModBlocks.MECHANICAL_PRINTER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::analyticalEngine, GuidancePonderTags.COMPUTATION_CHAIN);
        itemHelper.forComponents(ModBlocks.TESTBLOCK.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::testBlock, GuidancePonderTags.COMPUTATION_CHAIN);

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

        itemHelper.forComponents(ModBlocks.THRESHOLD_CONTROLLER.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::thresholdController, GuidancePonderTags.GUIDANCE_CONTROL);
        itemHelper.forComponents(ModBlocks.ROTARY_FRAME.get())
                .addStoryBoard("guidance/empty", GuidanceScenes::rotaryFrame, GuidancePonderTags.GUIDANCE_CONTROL);
    }
}

