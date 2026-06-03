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

public class PhotolithographyTableBlock extends ChipMachineBlock {
    public PhotolithographyTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModBlocks.OXIDIZED_WAFER.get())) {
            return process(level, pos, state, player, stack, ModBlocks.PHOTORESIST_WAFER.get(),
                    "message.createaddon.machine.photolithography_table.coated");
        }

        if (stack.is(ModBlocks.PHOTORESIST_WAFER.get())) {
            return process(level, pos, state, player, stack, ModBlocks.ETCHED_WAFER.get(),
                    "message.createaddon.machine.photolithography_table.exposed");
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult process(Level level, BlockPos pos, BlockState state, Player player, ItemStack input,
                                          net.minecraft.world.item.Item output, String messageKey) {
        triggerAnimation(level, pos, state, 50);
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        completeProcess(input, player, output, 1, Component.translatable(messageKey));
        return ItemInteractionResult.SUCCESS;
    }
}
