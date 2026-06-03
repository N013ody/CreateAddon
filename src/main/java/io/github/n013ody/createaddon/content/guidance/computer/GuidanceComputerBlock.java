package io.github.n013ody.createaddon.content.guidance.computer;

import io.github.n013ody.createaddon.registry.ModBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GuidanceComputerBlock extends Block implements IBE<GuidanceComputerBlockEntity> {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D),
            Block.box(0.0D, 5.0D, 6.0D, 16.0D, 9.0D, 10.0D),
            Block.box(3.0D, 12.0D, 3.0D, 13.0D, 14.5D, 13.0D));

    public GuidanceComputerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<GuidanceComputerBlockEntity> getBlockEntityClass() {
        return GuidanceComputerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GuidanceComputerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.GUIDANCE_COMPUTER.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        IBE.onRemove(state, level, pos, newState);
    }
}

