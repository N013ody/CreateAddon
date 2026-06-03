package io.github.n013ody.createaddon.content.guidance.sensor;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class GuidanceMath {
    private GuidanceMath() {
    }

    public static double yawDegrees(Vec3 vector) {
        return Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90.0D;
    }

    public static double horizontalLength(Vec3 vector) {
        return Math.sqrt(vector.x * vector.x + vector.z * vector.z);
    }

    public static double wrapDegrees(double degrees) {
        return Mth.wrapDegrees(degrees);
    }

    public static double clamp01(double value) {
        return Mth.clamp(value, 0.0D, 1.0D);
    }

    public static Vec3 horizontal(Vec3 vector) {
        return new Vec3(vector.x, 0.0D, vector.z);
    }
}

