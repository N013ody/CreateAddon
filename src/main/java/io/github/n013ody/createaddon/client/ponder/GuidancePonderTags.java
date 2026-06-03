package io.github.n013ody.createaddon.client.ponder;

import io.github.n013ody.createaddon.CreateAddon;
import io.github.n013ody.createaddon.registry.ModBlocks;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public final class GuidancePonderTags {
    public static final ResourceLocation GUIDANCE_CONTROL = ResourceLocation.fromNamespaceAndPath(CreateAddon.MOD_ID, "guidance_control");
    public static final ResourceLocation SENSOR_INPUTS = ResourceLocation.fromNamespaceAndPath(CreateAddon.MOD_ID, "sensor_inputs");
    public static final ResourceLocation COMPUTATION_CHAIN = ResourceLocation.fromNamespaceAndPath(CreateAddon.MOD_ID, "computation_chain");
    public static final ResourceLocation SEMICONDUCTOR_CHAIN = ResourceLocation.fromNamespaceAndPath(CreateAddon.MOD_ID, "semiconductor_chain");

    private GuidancePonderTags() {
    }

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(item -> ResourceLocation.fromNamespaceAndPath(
                CreateAddon.MOD_ID, item.asItem().builtInRegistryHolder().key().location().getPath()));

        helper.registerTag(GUIDANCE_CONTROL)
                .addToIndex()
                .item(ModBlocks.GUIDANCE_COMPUTER.get(), true, true)
                .title("Guidance & Control")
                .description("Mechanical instruments for navigation, fire-control and closed-loop contraptions")
                .register();

        helper.registerTag(SENSOR_INPUTS)
                .item(ModBlocks.RELATIVE_BEARING_SENSOR.get(), true, true)
                .title("Sensor Inputs")
                .description("Blocks that expose measured double, Vec3 or boolean values to a Guidance Computer")
                .register();

        helper.registerTag(COMPUTATION_CHAIN)
                .addToIndex()
                .item(ModBlocks.TABLE_PRINTER.get(), true, true)
                .title("Create: Computation")
                .description("Mechanical computation, punched cards and calibration data for later machines")
                .register();

        helper.registerTag(SEMICONDUCTOR_CHAIN)
                .addToIndex()
                .item(ModBlocks.WAFER_POLISHER.get(), true, true)
                .title("Semiconductor Production")
                .description("Material preparation and wafer processing for electronic computation")
                .register();

        itemHelper.addToTag(GUIDANCE_CONTROL)
                .add(ModBlocks.GUIDANCE_COMPUTER.get())
                .add(ModBlocks.RELATIVE_BEARING_SENSOR.get())
                .add(ModBlocks.CLOSING_SPEED_SENSOR.get())
                .add(ModBlocks.TERRAIN_ALTIMETER.get())
                .add(ModBlocks.INERTIAL_DRIFT_SENSOR.get())
                .add(ModBlocks.LOCK_ON_PROBABILITY_SENSOR.get())
                .add(ModBlocks.STRUCTURAL_STRESS_SENSOR.get())
                .add(ModBlocks.RELATIVE_VELOCITY_VECTOR_SENSOR.get());

        itemHelper.addToTag(SENSOR_INPUTS)
                .add(ModBlocks.RELATIVE_BEARING_SENSOR.get())
                .add(ModBlocks.CLOSING_SPEED_SENSOR.get())
                .add(ModBlocks.TERRAIN_ALTIMETER.get())
                .add(ModBlocks.INERTIAL_DRIFT_SENSOR.get())
                .add(ModBlocks.LOCK_ON_PROBABILITY_SENSOR.get())
                .add(ModBlocks.STRUCTURAL_STRESS_SENSOR.get())
                .add(ModBlocks.RELATIVE_VELOCITY_VECTOR_SENSOR.get());

        itemHelper.addToTag(COMPUTATION_CHAIN)
                .add(ModBlocks.FUNCTION_SETTING_DRUM.get())
                .add(ModBlocks.DIFFERENCE_REGISTER_COLUMN.get())
                .add(ModBlocks.TABLE_PRINTER.get())
                .add(ModBlocks.PUNCHED_CARD_READER.get())
                .add(ModBlocks.MILL_ARITHMETIC_BLOCK.get())
                .add(ModBlocks.STORE_MEMORY_COLUMN.get())
                .add(ModBlocks.MECHANICAL_PRINTER.get());

        itemHelper.addToTag(SEMICONDUCTOR_CHAIN)
                .add(ModBlocks.TIN_ORE.get())
                .add(ModBlocks.DEEPSLATE_TIN_ORE.get())
                .add(ModBlocks.DEEPSLATE_TUNGSTEN_ORE.get())
                .add(ModBlocks.CRYSTAL_PULL_FURNACE.get())
                .add(ModBlocks.WAFER_SAW.get())
                .add(ModBlocks.WAFER_POLISHER.get())
                .add(ModBlocks.OXIDATION_DIFFUSION_FURNACE.get())
                .add(ModBlocks.PHOTOLITHOGRAPHY_TABLE.get())
                .add(ModBlocks.PACKAGING_TEST_BENCH.get());
    }
}

