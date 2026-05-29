package com.example.createaddon.content.guidance.sensor;

import com.example.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class InertialDriftSensorBlockEntity extends DoubleSensorBlockEntity {
    public InertialDriftSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INERTIAL_DRIFT_SENSOR.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "inertial_drift";
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    protected Double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        Vec3 horizontalVelocity = GuidanceMath.horizontal(selfVelocity);
        if (GuidanceMath.horizontalLength(horizontalVelocity) < 0.01D)
            return 0.0D;
        return GuidanceMath.wrapDegrees(GuidanceMath.yawDegrees(horizontalVelocity) - desiredHeadingDegrees);
    }

    @Override
    protected boolean isSampleValid(TargetData target) {
        return true;
    }

    @Override
    protected double computeLampIntensity(Double sampledValue, boolean valid) {
        return GuidanceMath.clamp01(Math.abs(sampledValue) / 45.0D);
    }
}
