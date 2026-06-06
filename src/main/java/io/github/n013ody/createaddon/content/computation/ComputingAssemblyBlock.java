package io.github.n013ody.createaddon.content.computation;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ComputingAssemblyBlock extends RotatedPillarKineticBlock implements IBE<ComputingMachineBlockEntity> {
    private static final int SCAN_RADIUS = 4;
    private static final int MIN_PROCESSING_TICKS = 60;
    private static final float BASE_SPEED_RPM = 32.0F;
    private static final float MAX_SPEED_FACTOR = 4.0F;
    private static final float BASE_STRESS_IMPACT = 1.0F;

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
    public Class<ComputingMachineBlockEntity> getBlockEntityClass() {
        return ComputingMachineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ComputingMachineBlockEntity> getBlockEntityType() {
        return ModBlockEntities.COMPUTING_MACHINE.get();
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
        if (!stack.is(inputItem.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        ComponentScan scan = scanComponents(level, pos);
        if (scan.missingMessage() != null) {
            player.displayClientMessage(scan.missingMessage(), true);
            return ItemInteractionResult.SUCCESS;
        }

        ComputingMachineBlockEntity machine = getBlockEntity(level, pos);
        if (machine == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        float speed = Math.abs(machine.getSpeed());
        if (speed < ComputingMachineBlockEntity.MINIMUM_OPERATING_SPEED) {
            player.displayClientMessage(Component.translatable("message.createaddon.computation.no_kinetic_power"), true);
            return ItemInteractionResult.SUCCESS;
        }

        if (machine.isOverStressed()) {
            player.displayClientMessage(Component.translatable("message.createaddon.computation.overstressed"), true);
            return ItemInteractionResult.SUCCESS;
        }

        float speedFactor = speedFactor(speed);
        int adjustedTicks = adjustedProcessingTicks(scan.capabilityScore(), scan.minimumCapabilityScore(), speedFactor);
        float stressImpact = stressImpact(scan.capabilityScore());
        boolean started = machine.tryInsert(stack, new ItemStack(resultItem.get()), adjustedTicks, stressImpact,
                !player.getAbilities().instabuild);
        if (started) {
            player.displayClientMessage(Component.translatable(startedMessageKey, scan.capabilityScore(),
                    formatSeconds(adjustedTicks), formatStressImpact(stressImpact)), true);
            level.blockEvent(pos, state.getBlock(), ComputingMachineBlockEntity.WORKING_EVENT, adjustedTicks);
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

        ComputingMachineBlockEntity machine = getBlockEntity(level, pos);
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

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof ComputingMachineBlockEntity machine)
            machine.dropContents(level, pos);
        IBE.onRemove(state, level, pos, newState);
    }

    private ComponentScan scanComponents(Level level, BlockPos pos) {
        int capabilityScore = 0;
        int minimumCapabilityScore = 0;

        for (ComponentRequirement requirement : requirements) {
            int count = countNearby(level, pos, requirement.block());
            capabilityScore += count * requirement.computationUnits();
            minimumCapabilityScore += requirement.requiredComponentCount() * requirement.computationUnits();

            if (count < requirement.requiredComponentCount()) {
                Component missingMessage = Component.translatable("message.createaddon.computation.missing_components",
                        Component.translatable(requirement.nameKey()), requirement.requiredComponentCount(), count);
                return new ComponentScan(capabilityScore, minimumCapabilityScore, missingMessage);
            }
        }

        return new ComponentScan(capabilityScore, minimumCapabilityScore, null);
    }

    private int adjustedProcessingTicks(int capabilityScore, int minimumCapabilityScore, float speedFactor) {
        if (minimumCapabilityScore <= 0 || capabilityScore <= minimumCapabilityScore) {
            return Math.max(MIN_PROCESSING_TICKS, (int) Math.ceil(processingTicks / speedFactor));
        }

        int scaledTicks = (int) Math.ceil((double) processingTicks * minimumCapabilityScore / capabilityScore);
        return Math.max(MIN_PROCESSING_TICKS, (int) Math.ceil(scaledTicks / speedFactor));
    }

    private float speedFactor(float speed) {
        return Math.max(0.25F, Math.min(MAX_SPEED_FACTOR, speed / BASE_SPEED_RPM));
    }

    private float stressImpact(int capabilityScore) {
        return BASE_STRESS_IMPACT + capabilityScore / 80.0F;
    }

    private static String formatSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0D);
    }

    private static String formatStressImpact(float stressImpact) {
        return String.format(Locale.ROOT, "%.2f", stressImpact);
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

    private record ComponentScan(int capabilityScore, int minimumCapabilityScore, Component missingMessage) {
    }

    public record ComponentRequirement(Supplier<? extends Block> block, int requiredComponentCount, int computationUnits,
                                       String nameKey) {
    }
}
