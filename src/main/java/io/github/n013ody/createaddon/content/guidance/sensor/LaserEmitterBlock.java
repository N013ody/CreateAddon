package io.github.n013ody.createaddon.content.guidance.sensor;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import io.github.n013ody.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LaserEmitterBlock extends DirectionalKineticBlock implements IBE<LaserRangeFinderBlockEntity> {
    public LaserEmitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<LaserRangeFinderBlockEntity> getBlockEntityClass() {
        return LaserRangeFinderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LaserRangeFinderBlockEntity> getBlockEntityType() {
        return ModBlockEntities.LASER_RANGE_FINDER.get();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        IBE.onRemove(state, level, pos, newState);
    }
}

