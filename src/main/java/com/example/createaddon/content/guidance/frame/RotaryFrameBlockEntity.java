package com.example.createaddon.content.guidance.frame;

import java.util.List;

import com.example.createaddon.registry.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RotaryFrameBlockEntity extends SmartBlockEntity {
    private BlockState filledState = Blocks.AIR.defaultBlockState();
    private float yawDegrees;
    private float pitchDegrees;

    public RotaryFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROTARY_FRAME.get(), pos, state);
    }

    public BlockState getFilledState() {
        return filledState;
    }

    public boolean hasFilledState() {
        return !filledState.isAir();
    }

    public void setFilledState(BlockState filledState) {
        this.filledState = filledState;
        setChanged();
        sendData();
    }

    public void clearFilledState() {
        this.filledState = Blocks.AIR.defaultBlockState();
        setChanged();
        sendData();
    }

    public float getYawDegrees() {
        return yawDegrees;
    }

    public float getPitchDegrees() {
        return pitchDegrees;
    }

    public void rotateYaw(float amount) {
        yawDegrees = wrap(yawDegrees + amount);
        setChanged();
        sendData();
    }

    public void rotatePitch(float amount) {
        pitchDegrees = wrap(pitchDegrees + amount);
        setChanged();
        sendData();
    }

    private static float wrap(float value) {
        value %= 360.0F;
        return value < 0 ? value + 360.0F : value;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Yaw", yawDegrees);
        tag.putFloat("Pitch", pitchDegrees);
        if (!filledState.isAir())
            tag.put("FilledState", net.minecraft.nbt.NbtUtils.writeBlockState(filledState));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        yawDegrees = tag.getFloat("Yaw");
        pitchDegrees = tag.getFloat("Pitch");
        filledState = tag.contains("FilledState")
                ? net.minecraft.nbt.NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("FilledState"))
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
