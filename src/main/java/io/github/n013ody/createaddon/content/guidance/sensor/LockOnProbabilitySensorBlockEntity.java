package io.github.n013ody.createaddon.content.guidance.sensor;

import io.github.n013ody.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LockOnProbabilitySensorBlockEntity extends DoubleSensorBlockEntity {
    private double effectiveRange = 256.0D;
    private double effectiveFovDegrees = 60.0D;
    private double maxTrackSpeed = 80.0D;

    public LockOnProbabilitySensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOCK_ON_PROBABILITY_SENSOR.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "lock_on_probability";
    }

    @Override
    protected Double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        if (!target.valid())
            return 0.0D;

        double distance = selfPosition.distanceTo(target.position());
        double distanceScore = 1.0D - GuidanceMath.clamp01(distance / effectiveRange);

        double bearing = Math.abs(GuidanceMath.wrapDegrees(GuidanceMath.yawDegrees(target.position().subtract(selfPosition)) - currentYawDegrees()));
        double angleScore = 1.0D - GuidanceMath.clamp01(bearing / effectiveFovDegrees);

        Vec3 relativeVelocity = target.velocity().subtract(selfVelocity);
        Vec3 lineOfSight = target.position().subtract(selfPosition).normalize();
        double lateralSpeed = relativeVelocity.subtract(lineOfSight.scale(relativeVelocity.dot(lineOfSight))).length();
        double speedScore = 1.0D - GuidanceMath.clamp01(lateralSpeed / maxTrackSpeed);

        return GuidanceMath.clamp01(distanceScore * 0.4D + angleScore * 0.4D + speedScore * 0.2D);
    }

    @Override
    protected double computeNeedleAngle(Double sampledValue) {
        return sampledValue * 180.0D - 90.0D;
    }

    @Override
    protected double computeLampIntensity(Double sampledValue, boolean valid) {
        return valid ? sampledValue : 0.0D;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (!clientPacket) {
            tag.putDouble("EffectiveRange", effectiveRange);
            tag.putDouble("EffectiveFov", effectiveFovDegrees);
            tag.putDouble("MaxTrackSpeed", maxTrackSpeed);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (!clientPacket) {
            if (tag.contains("EffectiveRange"))
                effectiveRange = tag.getDouble("EffectiveRange");
            if (tag.contains("EffectiveFov"))
                effectiveFovDegrees = tag.getDouble("EffectiveFov");
            if (tag.contains("MaxTrackSpeed"))
                maxTrackSpeed = tag.getDouble("MaxTrackSpeed");
        }
    }
}

