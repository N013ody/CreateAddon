package io.github.n013ody.createaddon.content.computation;

import java.util.List;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ComputingMachineBlockEntity extends KineticBlockEntity {
    public static final int WORKING_EVENT = 1;
    public static final float MINIMUM_OPERATING_SPEED = 4.0F;

    private long workingUntilGameTime;
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private ItemStack resultStack = ItemStack.EMPTY;
    private int processingTicks;
    private int processingTicksTotal;
    private float activeStressImpact;

    public ComputingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTING_MACHINE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || processingTicks <= 0)
            return;

        if (!canProcessKinetically()) {
            if (!level.isClientSide && level.getGameTime() % 10 == 0) {
                sendData();
                setChanged();
            }
            return;
        }

        processingTicks--;

        if (!level.isClientSide) {
            if (processingTicks == 0) {
                finishProcessing();
            } else if (level.getGameTime() % 5 == 0) {
                sendData();
            }
            setChanged();
        }
    }

    public boolean tryInsert(ItemStack stack, ItemStack result, int ticks, float stressImpact, boolean consumeInput) {
        if (stack.isEmpty() || !inputStack.isEmpty() || !outputStack.isEmpty() || isProcessing())
            return false;

        inputStack = stack.copyWithCount(1);
        if (consumeInput)
            stack.shrink(1);
        startProcessing(result.copy(), ticks, stressImpact);
        setChanged();
        sendData();
        return true;
    }

    public ItemStack tryExtract() {
        if (!outputStack.isEmpty()) {
            ItemStack extracted = outputStack.copy();
            outputStack = ItemStack.EMPTY;
            processingTicksTotal = 0;
            setChanged();
            sendData();
            return extracted;
        }

        if (!inputStack.isEmpty() && !isProcessing()) {
            ItemStack extracted = inputStack.copy();
            inputStack = ItemStack.EMPTY;
            resultStack = ItemStack.EMPTY;
            processingTicksTotal = 0;
            clearActiveStress();
            setChanged();
            sendData();
            return extracted;
        }

        return ItemStack.EMPTY;
    }

    public void startProcessing(ItemStack result, int ticks, float stressImpact) {
        resultStack = result;
        processingTicksTotal = Math.max(1, ticks);
        processingTicks = processingTicksTotal;
        activeStressImpact = Math.max(0.0F, stressImpact);
        refreshStress();
        startWorking(processingTicksTotal);
    }

    private void finishProcessing() {
        outputStack = resultStack.copy();
        inputStack = ItemStack.EMPTY;
        resultStack = ItemStack.EMPTY;
        clearActiveStress();
        sendData();
    }

    private void clearActiveStress() {
        activeStressImpact = 0.0F;
        refreshStress();
    }

    public boolean isProcessing() {
        return processingTicks > 0;
    }

    public boolean canProcessKinetically() {
        return Math.abs(getSpeed()) >= MINIMUM_OPERATING_SPEED && !isOverStressed();
    }

    public float getProcessingProgress(float partialTick) {
        if (processingTicksTotal <= 0)
            return outputStack.isEmpty() ? 0.0F : 1.0F;

        float remaining = processingTicks;
        if (isProcessing() && level != null && level.isClientSide && canProcessKinetically())
            remaining = Math.max(0.0F, remaining - partialTick);

        return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / processingTicksTotal));
    }

    public ItemStack getDisplayStack() {
        if (!outputStack.isEmpty())
            return outputStack;
        return inputStack;
    }

    public void dropContents(Level level, BlockPos pos) {
        dropStack(level, pos, outputStack);
        dropStack(level, pos, inputStack);
        outputStack = ItemStack.EMPTY;
        inputStack = ItemStack.EMPTY;
        resultStack = ItemStack.EMPTY;
        processingTicks = 0;
        processingTicksTotal = 0;
        clearActiveStress();
    }

    private static void dropStack(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty())
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
    }

    public void startWorking(int ticks) {
        if (level == null)
            return;

        workingUntilGameTime = Math.max(workingUntilGameTime, level.getGameTime() + Math.max(1, ticks));
    }

    public boolean isWorking(float partialTick) {
        if (level == null)
            return false;

        return level.getGameTime() + partialTick < workingUntilGameTime;
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = isProcessing() ? activeStressImpact : 0.0F;
        return lastStressApplied;
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        refreshStress();
    }

    private void refreshStress() {
        if (level == null || level.isClientSide || !hasNetwork())
            return;

        getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == WORKING_EVENT) {
            startWorking(type);
            return true;
        }

        return super.triggerEvent(id, type);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Input", inputStack.saveOptional(registries));
        tag.put("Output", outputStack.saveOptional(registries));
        tag.put("Result", resultStack.saveOptional(registries));
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putInt("ProcessingTicksTotal", processingTicksTotal);
        tag.putLong("WorkingUntilGameTime", workingUntilGameTime);
        tag.putFloat("ActiveStressImpact", activeStressImpact);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inputStack = ItemStack.parseOptional(registries, tag.getCompound("Input"));
        outputStack = ItemStack.parseOptional(registries, tag.getCompound("Output"));
        resultStack = ItemStack.parseOptional(registries, tag.getCompound("Result"));
        processingTicks = tag.getInt("ProcessingTicks");
        processingTicksTotal = tag.getInt("ProcessingTicksTotal");
        workingUntilGameTime = tag.getLong("WorkingUntilGameTime");
        activeStressImpact = tag.getFloat("ActiveStressImpact");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
