package io.github.n013ody.createaddon.content.semiconductor;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ChipMachineBlockEntity extends SmartBlockEntity {
    public static final int WORKING_EVENT = 1;

    private long workingUntilGameTime;
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private ItemStack resultStack = ItemStack.EMPTY;
    private int processingTicks;
    private int processingTicksTotal;

    public ChipMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHIP_MACHINE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || processingTicks <= 0)
            return;

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

    public boolean tryInsert(ItemStack stack, ItemStack result, int ticks) {
        return tryInsert(stack, result, ticks, true);
    }

    public boolean tryInsert(ItemStack stack, ItemStack result, int ticks, boolean consumeInput) {
        if (stack.isEmpty() || !inputStack.isEmpty() || !outputStack.isEmpty() || isProcessing())
            return false;

        inputStack = stack.copyWithCount(1);
        if (consumeInput)
            stack.shrink(1);
        startProcessing(result.copy(), ticks);
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
            setChanged();
            sendData();
            return extracted;
        }

        return ItemStack.EMPTY;
    }

    public void startProcessing(ItemStack result, int ticks) {
        resultStack = result;
        processingTicksTotal = Math.max(1, ticks);
        processingTicks = processingTicksTotal;
        startWorking(processingTicksTotal);
    }

    private void finishProcessing() {
        outputStack = resultStack.copy();
        inputStack = ItemStack.EMPTY;
        resultStack = ItemStack.EMPTY;
        sendData();
    }

    public boolean isProcessing() {
        return processingTicks > 0;
    }

    public float getProcessingProgress(float partialTick) {
        if (processingTicksTotal <= 0)
            return outputStack.isEmpty() ? 0.0F : 1.0F;

        float remaining = processingTicks;
        if (isProcessing() && level != null && level.isClientSide)
            remaining = Math.max(0.0F, remaining - partialTick);

        return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / processingTicksTotal));
    }

    public ItemStack getInputStack() {
        return inputStack;
    }

    public ItemStack getOutputStack() {
        return outputStack;
    }

    public ItemStack getDisplayStack() {
        if (!outputStack.isEmpty())
            return outputStack;
        return inputStack;
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

    public float workingProgress(float partialTick, int durationTicks) {
        if (level == null || durationTicks <= 0)
            return 0.0F;

        float remaining = workingUntilGameTime - (level.getGameTime() + partialTick);
        return Math.max(0.0F, Math.min(1.0F, remaining / durationTicks));
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
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
