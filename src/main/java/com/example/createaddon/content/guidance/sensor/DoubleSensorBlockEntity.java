package com.example.createaddon.content.guidance.sensor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DoubleSensorBlockEntity extends AbstractSensorBlockEntity<Double> {
    protected DoubleSensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0.0D);
    }

    @Override
    public Class<?> getValueType() {
        return Double.class;
    }

    @Override
    protected void writeSensorValue(CompoundTag tag, Double value) {
        tag.putDouble("Value", value);
    }

    @Override
    protected Double readSensorValue(CompoundTag tag) {
        return tag.getDouble("Value");
    }

    @Override
    protected double computeNeedleAngle(Double sampledValue) {
        return sampledValue;
    }
}
