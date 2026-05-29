package com.example.createaddon.client;

import java.util.Map;

import com.example.createaddon.content.guidance.api.SensorReading;
import com.example.createaddon.content.guidance.computer.GuidanceComputerBlockEntity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class GuidanceHudOverlay {
    private GuidanceHudOverlay() {
    }

    public static void renderPanel(GuiGraphics graphics, GuidanceComputerBlockEntity computer, int x, int y) {
        Map<String, SensorReading<?>> readings = computer.readAllSensors();
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                Component.translatable("gui.createaddon.guidance_computer"), x, y, 0xE6D39A, false);

        int row = y + 12;
        for (SensorReading<?> reading : readings.values()) {
            String state = reading.valid() ? formatValue(reading.value()) : Component.translatable("gui.createaddon.no_lock").getString();
            graphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                    Component.literal(reading.sensorId() + ": " + state), x, row, 0xD7C7A0, false);
            row += 10;
        }

        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                Component.translatable("gui.createaddon.commands", String.format("%.2f", computer.getRudderCommand()),
                        String.format("%.2f", computer.getElevatorCommand()), String.format("%.2f", computer.getThrottleCommand())),
                x, row + 4, 0x8EE68E, false);
    }

    private static String formatValue(Object value) {
        if (value instanceof Number number)
            return String.format(java.util.Locale.ROOT, "%.2f", number.doubleValue());
        if (value instanceof net.minecraft.world.phys.Vec3 vec3)
            return String.format(java.util.Locale.ROOT, "(%.2f, %.2f, %.2f)", vec3.x, vec3.y, vec3.z);
        return String.valueOf(value);
    }
}
