package com.example.createaddon.client.ponder;

import com.example.createaddon.CreateAddon;
import com.example.createaddon.registry.ModBlocks;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public final class GuidancePonderTags {
    public static final ResourceLocation GUIDANCE_CONTROL = ResourceLocation.fromNamespaceAndPath(CreateAddon.MOD_ID, "guidance_control");
    public static final ResourceLocation SENSOR_INPUTS = ResourceLocation.fromNamespaceAndPath(CreateAddon.MOD_ID, "sensor_inputs");

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
    }
}
