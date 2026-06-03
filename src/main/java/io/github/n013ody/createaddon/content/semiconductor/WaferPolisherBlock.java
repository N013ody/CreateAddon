package io.github.n013ody.createaddon.content.semiconductor;

import io.github.n013ody.createaddon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WaferPolisherBlock extends ChipMachineBlock {
    private static final int POLISHING_TICKS = 120;

    public WaferPolisherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModBlocks.ROUGH_WAFER.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        ChipMachineBlockEntity machine = getBlockEntity(level, pos);
        if (machine == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean started = machine.tryInsert(stack, new ItemStack(ModBlocks.POLISHED_WAFER.get()), POLISHING_TICKS,
                !player.getAbilities().instabuild);
        if (started) {
            player.displayClientMessage(Component.literal("\u5f00\u59cb\u629b\u5149\u6676\u5706"), true);
        } else {
            player.displayClientMessage(Component.literal("\u673a\u5668\u6b63\u5728\u52a0\u5de5\uff0c\u6216\u8005\u4ea7\u7269\u8fd8\u6ca1\u53d6\u51fa"), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        ChipMachineBlockEntity machine = getBlockEntity(level, pos);
        if (machine == null)
            return InteractionResult.PASS;

        ItemStack extracted = machine.tryExtract();
        if (extracted.isEmpty()) {
            player.displayClientMessage(Component.literal("\u673a\u5668\u91cc\u6ca1\u6709\u53ef\u53d6\u51fa\u7684\u7269\u54c1"), true);
            return InteractionResult.SUCCESS;
        }

        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }

        player.displayClientMessage(Component.literal("\u5df2\u53d6\u51fa\uff1a" + extracted.getHoverName().getString()), true);
        return InteractionResult.SUCCESS;
    }
}
