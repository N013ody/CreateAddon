package io.github.n013ody.createaddon.content.guidance.controller;

import io.github.n013ody.createaddon.registry.ModBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ServoMountBlock extends Block implements IBE<ServoMountBlockEntity> {

    public ServoMountBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<ServoMountBlockEntity> getBlockEntityClass() {
        return ServoMountBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ServoMountBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SERVO_MOUNT.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        IBE.onRemove(state, level, pos, newState);
    }
}
