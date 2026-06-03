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

public class OxidationDiffusionFurnaceBlock extends ChipMachineBlock {
    public OxidationDiffusionFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModBlocks.POLISHED_WAFER.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        triggerAnimation(level, pos, state, 70);
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        completeProcess(stack, player, ModBlocks.OXIDIZED_WAFER.get(), 1,
                Component.translatable("message.createaddon.machine.oxidation_diffusion_furnace.oxidized"));
        return ItemInteractionResult.SUCCESS;
    }
}
