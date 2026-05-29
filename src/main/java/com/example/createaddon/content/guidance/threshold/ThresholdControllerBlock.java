package com.example.createaddon.content.guidance.threshold;

import com.example.createaddon.registry.ModBlocks;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ThresholdControllerBlock extends Block implements IBE<ThresholdControllerBlockEntity> {

    public ThresholdControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<ThresholdControllerBlockEntity> getBlockEntityClass() {
        return ThresholdControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThresholdControllerBlockEntity> getBlockEntityType() {
        return com.example.createaddon.registry.ModBlockEntities.THRESHOLD_CONTROLLER.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModBlocks.SENSOR_BINDER.get()))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;
        InteractionResult result = onBlockEntityUse(level, pos, be -> be.handleUse(player, player.isSecondaryUseActive()));
        return switch (result) {
            case SUCCESS, CONSUME -> ItemInteractionResult.SUCCESS;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ThresholdControllerBlockEntity be)
            return be.getOutputSignal();
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }
}
