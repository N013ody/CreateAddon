package com.example.createaddon.content.guidance.sensor;

import com.example.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StructuralStressSensorBlockEntity extends DoubleSensorBlockEntity {
    private Vec3 previousSelfVelocity = Vec3.ZERO;

    public StructuralStressSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRUCTURAL_STRESS_SENSOR.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "structural_stress";
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    protected Double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        double networkLoad = capacity <= 0.0F ? 0.0D : stress / capacity;
        double linearLoad = selfVelocity.length() / 80.0D;
        double angularLoad = Math.abs(getTheoreticalSpeed()) / 256.0D;
        double accelerationLoad = selfVelocity.subtract(previousSelfVelocity).scale(20.0D).length() / 60.0D;
        previousSelfVelocity = selfVelocity;
        return GuidanceMath.clamp01(Math.max(Math.max(networkLoad, linearLoad), Math.max(angularLoad, accelerationLoad)));
    }

    @Override
    protected boolean isSampleValid(TargetData target) {
        return true;
    }

    @Override
    protected double computeNeedleAngle(Double sampledValue) {
        return sampledValue * 180.0D - 90.0D;
    }

    @Override
    protected double computeLampIntensity(Double sampledValue, boolean valid) {
        return sampledValue;
    }
}
