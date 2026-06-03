package io.github.n013ody.createaddon.client;

import java.util.Map;

import io.github.n013ody.createaddon.CreateAddon;
import io.github.n013ody.createaddon.content.guidance.api.SensorReading;
import io.github.n013ody.createaddon.content.guidance.computer.GuidanceComputerBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.AbstractSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.LaserRangeFinderBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class GuidanceDebugHud {
    public static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(CreateAddon.MOD_ID, "guidance_debug_hud");

    private GuidanceDebugHud() {
    }

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, LAYER_ID, GuidanceDebugHud::render);
    }

    private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.level == null || minecraft.player == null)
            return;
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK)
            return;

        BlockEntity blockEntity = minecraft.level.getBlockEntity(hit.getBlockPos());
        if (blockEntity instanceof GuidanceComputerBlockEntity computer) {
            renderComputer(graphics, computer, 8, 8);
            return;
        }
        if (blockEntity instanceof AbstractSensorBlockEntity<?> sensor)
            renderSensor(graphics, sensor, 8, 8);
    }

    private static void renderComputer(GuiGraphics graphics, GuidanceComputerBlockEntity computer, int x, int y) {
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.computer").withStyle(ChatFormatting.GOLD));
        y += 10;
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.commands",
                format(computer.getRudderCommand()), format(computer.getElevatorCommand()), format(computer.getThrottleCommand())));
        y += 10;
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.desired_altitude", format(computer.getDesiredTerrainAltitude())));
        y += 10;

        Map<String, SensorReading<?>> readings = computer.readAllSensors();
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.subscribed", readings.size()));
        y += 10;

        if (!Screen.hasShiftDown()) {
            drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        for (SensorReading<?> reading : readings.values()) {
            drawLine(graphics, x, y, Component.literal(reading.sensorId() + " = " + formatValue(reading) + " [" + valueType(reading.value()) + ", t=" + reading.gameTime() + "]")
                    .withStyle(reading.valid() ? ChatFormatting.GRAY : ChatFormatting.RED));
            y += 10;
        }
    }

    private static void renderSensor(GuiGraphics graphics, AbstractSensorBlockEntity<?> sensor, int x, int y) {
        SensorReading<?> reading = sensor.readSensor();
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.sensor", reading.sensorId()).withStyle(ChatFormatting.GOLD));
        y += 10;
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.value", formatValue(reading))
                .withStyle(reading.valid() ? ChatFormatting.GRAY : ChatFormatting.RED));
        y += 10;
        drawLine(graphics, x, y, Component.literal("type: " + valueType(reading.value()) + "  tick: " + reading.gameTime()
                + "  valid: " + reading.valid()).withStyle(ChatFormatting.DARK_GRAY));
        y += 10;
        drawLine(graphics, x, y, sensorStatus(sensor, reading));
        y += 10;
        if (sensor instanceof LaserRangeFinderBlockEntity laser) {
            drawLine(graphics, x, y, Component.literal("hit type: " + laser.getLastHitType()).withStyle(ChatFormatting.AQUA));
            y += 10;
            drawLine(graphics, x, y, Component.literal("hit pos: " + formatVec(laser.getLastHitPos())).withStyle(ChatFormatting.AQUA));
            y += 10;
            drawLine(graphics, x, y, Component.literal("hit dist: " + format(laser.getLastHitDistance())).withStyle(ChatFormatting.AQUA));
            y += 10;
        }
        if (sensor.requiresTarget()) {
            drawLine(graphics, x, y, Component.literal("target: " + targetName(sensor)).withStyle(ChatFormatting.AQUA));
            y += 10;
            drawLine(graphics, x, y, Component.literal("target pos: " + formatVec(sensor.getLastTargetPosition())).withStyle(ChatFormatting.AQUA));
            y += 10;
            drawLine(graphics, x, y, Component.literal("target dist: " + format(sensor.getLastTargetDistance())).withStyle(ChatFormatting.AQUA));
            y += 10;
        }
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.needle", format(sensor.getNeedleAngleDegrees())));
        y += 10;
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.lamp", format(sensor.getLampIntensity())));
        y += 10;
        drawLine(graphics, x, y, Component.translatable("gui.createaddon.guidance_debug.velocity", format(sensor.getSelfVelocity().x),
                format(sensor.getSelfVelocity().y), format(sensor.getSelfVelocity().z)));
    }

    private static void drawLine(GuiGraphics graphics, int x, int y, Component text) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(x - 3, y - 2, x + minecraft.font.width(text) + 3, y + 9, 0x90000000);
        graphics.drawString(minecraft.font, text, x, y, 0xFFE6D39A, false);
    }

    private static String formatValue(SensorReading<?> reading) {
        if (!reading.valid())
            return Component.translatable("gui.createaddon.no_lock").getString();
        Object value = reading.value();
        if (value instanceof Number number)
            return format(number.doubleValue());
        return String.valueOf(value);
    }

    private static String valueType(Object value) {
        if (value == null)
            return "null";
        if (value instanceof Number)
            return "double";
        if (value instanceof net.minecraft.world.phys.Vec3)
            return "vec3";
        return value.getClass().getSimpleName();
    }

    private static String targetName(AbstractSensorBlockEntity<?> sensor) {
        if (!sensor.isReadingValid())
            return "NO LOCK";
        String entityName = sensor.getLastTargetEntityName();
        if (entityName != null && !entityName.isBlank())
            return entityName + " (" + shortId(sensor.getLastTargetEntityId()) + ")";
        return "point";
    }

    private static String shortId(String id) {
        if (id == null || id.isBlank())
            return "";
        return id.length() <= 8 ? id : id.substring(0, 8);
    }

    private static String formatVec(net.minecraft.world.phys.Vec3 vec) {
        if (vec == null)
            return "none";
        return String.format(java.util.Locale.ROOT, "(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z);
    }

    private static Component sensorStatus(AbstractSensorBlockEntity<?> sensor, SensorReading<?> reading) {
        if (sensor.requiresTarget() && !sensor.hasResolvedTarget())
            return Component.translatable("gui.createaddon.guidance_debug.status.no_target").withStyle(ChatFormatting.RED);
        if (!reading.valid())
            return Component.translatable("gui.createaddon.guidance_debug.status.invalid").withStyle(ChatFormatting.RED);
        if (reading.value() instanceof Number number && Math.abs(number.doubleValue()) < 1.0E-4D)
            return Component.translatable("gui.createaddon.guidance_debug.status.zero_valid").withStyle(ChatFormatting.DARK_GRAY);
        return Component.translatable("gui.createaddon.guidance_debug.status.valid").withStyle(ChatFormatting.GREEN);
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}

