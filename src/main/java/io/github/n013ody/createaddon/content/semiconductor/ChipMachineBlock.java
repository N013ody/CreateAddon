package io.github.n013ody.createaddon.content.semiconductor;

import com.simibubi.create.foundation.block.IBE;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ChipMachineBlock extends Block implements IBE<ChipMachineBlockEntity> {
    public ChipMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<ChipMachineBlockEntity> getBlockEntityClass() {
        return ChipMachineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChipMachineBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CHIP_MACHINE.get();
    }

    protected void triggerAnimation(Level level, BlockPos pos, BlockState state, int ticks) {
        ChipMachineBlockEntity machine = getBlockEntity(level, pos);
        if (machine != null) {
            machine.startWorking(ticks);
        }

        if (!level.isClientSide) {
            level.blockEvent(pos, state.getBlock(), ChipMachineBlockEntity.WORKING_EVENT, ticks);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof ChipMachineBlockEntity machine)
            machine.dropContents(level, pos);
        IBE.onRemove(state, level, pos, newState);
    }

    protected static void completeProcess(ItemStack input, Player player, Item output, int outputCount, Component message) {
        if (!player.getAbilities().instabuild) {
            input.shrink(1);
        }

        ItemStack result = new ItemStack(output, outputCount);
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }

        player.displayClientMessage(message, true);
    }
}
