package io.github.n013ody.createaddon.content.guidance.sensor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class Vec3SensorBlockEntity extends AbstractSensorBlockEntity<Vec3> {
    protected Vec3SensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, Vec3.ZERO);
    }

    @Override
    public Class<?> getValueType() {
        return Vec3.class;
    }

    @Override
    protected void writeSensorValue(CompoundTag tag, Vec3 value) {
        tag.putDouble("ValueX", value.x);
        tag.putDouble("ValueY", value.y);
        tag.putDouble("ValueZ", value.z);
    }

    @Override
    protected Vec3 readSensorValue(CompoundTag tag) {
        return new Vec3(tag.getDouble("ValueX"), tag.getDouble("ValueY"), tag.getDouble("ValueZ"));
    }

    @Override
    protected double computeNeedleAngle(Vec3 sampledValue) {
        return GuidanceMath.yawDegrees(sampledValue);
    }

    @Override
    protected double computeLampIntensity(Vec3 sampledValue, boolean valid) {
        return valid ? GuidanceMath.clamp01(sampledValue.length() / 80.0D) : 0.0D;
    }
}

