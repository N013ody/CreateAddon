package io.github.n013ody.createaddon.content.semiconductor;

import io.github.n013ody.createaddon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WaferSawBlock extends ChipMachineBlock {
    public WaferSawBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModBlocks.SILICON_BOULE.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            triggerAnimation(level, pos, state, 45);
            return ItemInteractionResult.SUCCESS;
        }

        triggerAnimation(level, pos, state, 45);
        completeProcess(stack, player, ModBlocks.ROUGH_WAFER.get(), 4,
                Component.translatable("message.createaddon.machine.wafer_saw.sliced"));
        return ItemInteractionResult.SUCCESS;
    }
}
