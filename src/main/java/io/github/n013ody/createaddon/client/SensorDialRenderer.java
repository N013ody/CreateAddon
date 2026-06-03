package io.github.n013ody.createaddon.client;

import io.github.n013ody.createaddon.content.guidance.sensor.AbstractSensorBlockEntity;

public final class SensorDialRenderer {
    private SensorDialRenderer() {
    }

    public static double pointerAngle(AbstractSensorBlockEntity<?> sensor) {
        return sensor.getNeedleAngleDegrees();
    }

    public static int lampColor(AbstractSensorBlockEntity<?> sensor) {
        int base = sensor.isReadingValid() ? 60 : 20;
        int intensity = (int) Math.round(Math.max(0.0D, Math.min(1.0D, sensor.getLampIntensity())) * 195.0D) + base;
        return 0xFF000000 | Math.min(255, intensity) << 16 | Math.min(255, intensity / 2) << 8;
    }
}

