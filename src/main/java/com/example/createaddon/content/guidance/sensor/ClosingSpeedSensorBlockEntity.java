package com.example.createaddon.content.guidance.sensor;

import com.example.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ClosingSpeedSensorBlockEntity extends DoubleSensorBlockEntity {
    public ClosingSpeedSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CLOSING_SPEED_SENSOR.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "closing_speed";
    }

    @Override
    protected Double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        if (!target.valid())
            return 0.0D;
        Vec3 lineOfSight = target.position().subtract(selfPosition);
        if (lineOfSight.lengthSqr() < 1.0E-6D)
            return 0.0D;
        Vec3 relativeVelocity = target.velocity().subtract(selfVelocity);
        return -relativeVelocity.dot(lineOfSight.normalize());
    }

    @Override
    protected double computeLampIntensity(Double sampledValue, boolean valid) {
        return valid ? GuidanceMath.clamp01(Math.max(0.0D, sampledValue) / 40.0D) : 0.0D;
    }
}
