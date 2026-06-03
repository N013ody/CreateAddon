package io.github.n013ody.createaddon.content.computation;

import java.util.List;
import java.util.function.Supplier;

import io.github.n013ody.createaddon.content.semiconductor.ChipMachineBlock;
import io.github.n013ody.createaddon.content.semiconductor.ChipMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ComputingAssemblyBlock extends ChipMachineBlock {
    private static final int SCAN_RADIUS = 4;

    private final Supplier<? extends Item> inputItem;
    private final Supplier<? extends Item> resultItem;
    private final int processingTicks;
    private final String startedMessageKey;
    private final List<ComponentRequirement> requirements;

    public ComputingAssemblyBlock(Properties properties, Supplier<? extends Item> inputItem,
                                  Supplier<? extends Item> resultItem, int processingTicks,
                                  String startedMessageKey, List<ComponentRequirement> requirements) {
        super(properties);
        this.inputItem = inputItem;
        this.resultItem = resultItem;
        this.processingTicks = processingTicks;
        this.startedMessageKey = startedMessageKey;
        this.requirements = List.copyOf(requirements);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(inputItem.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        Component missing = missingRequirementMessage(level, pos);
        if (missing != null) {
            player.displayClientMessage(missing, true);
            return ItemInteractionResult.SUCCESS;
        }

        ChipMachineBlockEntity machine = getBlockEntity(level, pos);
        if (machine == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean started = machine.tryInsert(stack, new ItemStack(resultItem.get()), processingTicks,
                !player.getAbilities().instabuild);
        if (started) {
            player.displayClientMessage(Component.translatable(startedMessageKey), true);
        } else {
            player.displayClientMessage(Component.translatable("message.createaddon.computation.busy"), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ChipMachineBlockEntity machine = getBlockEntity(level, pos);
        if (machine == null) {
            return InteractionResult.PASS;
        }

        ItemStack extracted = machine.tryExtract();
        if (extracted.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.createaddon.computation.empty"), true);
            return InteractionResult.SUCCESS;
        }

        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }

        player.displayClientMessage(Component.translatable("message.createaddon.computation.extracted",
                extracted.getHoverName()), true);
        return InteractionResult.SUCCESS;
    }

    private Component missingRequirementMessage(Level level, BlockPos pos) {
        for (ComponentRequirement requirement : requirements) {
            int count = countNearby(level, pos, requirement.block());
            if (count < requirement.requiredComponentCount()) {
                return Component.translatable("message.createaddon.computation.missing_components",
                        Component.translatable(requirement.nameKey()), requirement.requiredComponentCount(), count);
            }
        }

        return null;
    }

    private int countNearby(Level level, BlockPos center, Supplier<? extends Block> block) {
        int count = 0;
        for (BlockPos checked : BlockPos.betweenClosed(center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (checked.equals(center)) {
                continue;
            }
            if (level.getBlockState(checked).is(block.get())) {
                count++;
            }
        }
        return count;
    }

    public record ComponentRequirement(Supplier<? extends Block> block, int requiredComponentCount,
                                       String nameKey) {
    }
}
