package io.github.n013ody.createaddon.content.guidance.sensor;

import io.github.n013ody.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RelativeBearingSensorBlockEntity extends DoubleSensorBlockEntity {
    public RelativeBearingSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RELATIVE_BEARING_SENSOR.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "relative_bearing";
    }

    @Override
    protected Double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        if (!target.valid())
            return 0.0D;
        Vec3 toTarget = target.position().subtract(selfPosition);
        double targetYaw = GuidanceMath.yawDegrees(toTarget);
        return GuidanceMath.wrapDegrees(targetYaw - currentYawDegrees());
    }

    @Override
    protected double computeLampIntensity(Double sampledValue, boolean valid) {
        return valid ? 1.0D - GuidanceMath.clamp01(Math.abs(sampledValue) / 180.0D) : 0.0D;
    }
}

