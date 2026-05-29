package com.example.createaddon.content.guidance.sensor;

import com.example.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RelativeVelocityVectorSensorBlockEntity extends Vec3SensorBlockEntity {
    public RelativeVelocityVectorSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RELATIVE_VELOCITY_VECTOR_SENSOR.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "relative_velocity_vector";
    }

    @Override
    protected Vec3 sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        if (!target.valid())
            return Vec3.ZERO;
        return target.velocity().subtract(selfVelocity);
    }
}
