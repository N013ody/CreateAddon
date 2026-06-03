package io.github.n013ody.createaddon.content.guidance.computer;

public final class GuidanceUseCases {
    private GuidanceUseCases() {
    }

    public record TerrainFollowingAutoRudder(double elevator, double rudder, double throttle) {
    }

    public static TerrainFollowingAutoRudder terrainFollowingAutoRudder(double desiredAltitude, double terrainAltitude,
                                                                        double bearingError, double driftAngle,
                                                                        double closingSpeed, double stressDanger) {
        double elevator = clamp((desiredAltitude - terrainAltitude) / 16.0D, -1.0D, 1.0D);
        double rudder = clamp((bearingError * 0.025D) - (driftAngle * 0.015D), -1.0D, 1.0D);
        double throttle = clamp(1.0D - Math.max(0.0D, closingSpeed) / 80.0D - stressDanger * 0.75D, 0.0D, 1.0D);
        return new TerrainFollowingAutoRudder(elevator, rudder, throttle);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

