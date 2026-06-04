package io.github.n013ody.createaddon.content.guidance.controller;

import io.github.n013ody.createaddon.registry.ModBlockEntities;
import io.github.n013ody.createaddon.registry.ModBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GuidanceControllerBlock extends RotatedPillarKineticBlock implements IBE<GuidanceControllerBlockEntity> {

    public GuidanceControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<GuidanceControllerBlockEntity> getBlockEntityClass() {
        return GuidanceControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GuidanceControllerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.GUIDANCE_CONTROLLER.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModBlocks.SENSOR_BINDER.get())) {
            InteractionResult result = stack.useOn(new net.minecraft.world.item.context.UseOnContext(player, hand, hitResult));
            return switch (result) {
                case SUCCESS, CONSUME -> ItemInteractionResult.SUCCESS;
                case FAIL -> ItemInteractionResult.FAIL;
                default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            };
        }

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
}
