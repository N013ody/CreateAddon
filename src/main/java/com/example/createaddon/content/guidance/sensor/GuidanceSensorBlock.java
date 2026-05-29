package com.example.createaddon.content.guidance.sensor;

import java.util.function.Supplier;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GuidanceSensorBlock<T extends AbstractSensorBlockEntity<?>> extends RotatedPillarKineticBlock implements IBE<T> {
    private final Class<T> blockEntityClass;
    private final Supplier<? extends BlockEntityType<? extends T>> blockEntityType;

    public GuidanceSensorBlock(Properties properties, Class<T> blockEntityClass,
                               Supplier<? extends BlockEntityType<? extends T>> blockEntityType) {
        super(properties);
        this.blockEntityClass = blockEntityClass;
        this.blockEntityType = blockEntityType;
    }

    @Override
    public Class<T> getBlockEntityClass() {
        return blockEntityClass;
    }

    @Override
    public BlockEntityType<? extends T> getBlockEntityType() {
        return blockEntityType.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof AbstractSensorBlockEntity<?> sensor))
            return InteractionResult.PASS;
        return sensor.onUse(player);
    }
}
