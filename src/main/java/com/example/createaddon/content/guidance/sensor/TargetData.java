package com.example.createaddon.content.guidance.sensor;

import java.util.UUID;

import net.minecraft.world.phys.Vec3;

public record TargetData(Vec3 position, Vec3 velocity, boolean valid, UUID entityId, String entityName) {
    public static final TargetData INVALID = new TargetData(Vec3.ZERO, Vec3.ZERO, false, null, "");

    public TargetData(Vec3 position, Vec3 velocity, boolean valid) {
        this(position, velocity, valid, null, "");
    }
}
