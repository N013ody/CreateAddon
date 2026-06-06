package io.github.n013ody.createaddon.content.guidance.frame;

import io.github.n013ody.createaddon.registry.ModBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RotaryFrameBlock extends Block implements IBE<RotaryFrameBlockEntity> {
    public RotaryFrameBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<RotaryFrameBlockEntity> getBlockEntityClass() {
        return RotaryFrameBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotaryFrameBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ROTARY_FRAME.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        RotaryFrameBlockEntity frame = getBlockEntity(level, pos);
        if (frame == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (player.isSecondaryUseActive() && !stack.isEmpty()) {
            frame.clearFilledState();
            player.displayClientMessage(Component.translatable("message.createaddon.rotary_frame.cleared"), true);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof BlockItem blockItem) {
            frame.setFilledState(blockItem.getBlock().defaultBlockState());
            player.displayClientMessage(Component.translatable("message.createaddon.rotary_frame.filled", stack.getHoverName()), true);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            if (player.isSecondaryUseActive()) {
                frame.rotatePitch(15.0F);
                player.displayClientMessage(Component.translatable("message.createaddon.rotary_frame.pitch", frame.getPitchDegrees()), true);
            } else {
                frame.rotateYaw(15.0F);
                player.displayClientMessage(Component.translatable("message.createaddon.rotary_frame.yaw", frame.getYawDegrees()), true);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        IBE.onRemove(state, level, pos, newState);
    }
}
