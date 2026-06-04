package io.github.n013ody.createaddon.content.guidance.controller;

import java.util.List;

import io.github.n013ody.createaddon.content.guidance.api.ISensor;
import io.github.n013ody.createaddon.content.guidance.computer.GuidanceComputerBlockEntity;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GuidanceControllerBlockEntity extends KineticBlockEntity {

    private static final int SCAN_RADIUS = 5;

    private double rudderCommand;
    private double elevatorCommand;
    private double throttleCommand;
    private BlockPos boundSensorPos;
    private String boundSensorId = "";
    private String inputSourceType = "none";

    public GuidanceControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUIDANCE_CONTROLLER.get(), pos, state);
        setLazyTickRate(8);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide)
            return;
        if (level.getGameTime() % 5 == 0)
            sendData();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null)
            return;

        if (getSpeed() == 0) {
            rudderCommand = 0;
            elevatorCommand = 0;
            throttleCommand = 0;
            inputSourceType = "none";
            setChanged();
            sendData();
            return;
        }

        GuidanceComputerBlockEntity computer = findNearby(GuidanceComputerBlockEntity.class);
        if (computer != null) {
            rudderCommand = computer.getRudderCommand();
            elevatorCommand = computer.getElevatorCommand();
            throttleCommand = computer.getThrottleCommand();
            inputSourceType = "computer";
        } else if (boundSensorPos != null && !boundSensorId.isEmpty()) {
            double sensorValue = readBoundSensor();
            rudderCommand = sensorValue;
            elevatorCommand = 0;
            throttleCommand = 0;
            inputSourceType = "sensor";
        } else {
            rudderCommand = 0;
            elevatorCommand = 0;
            throttleCommand = 0;
            inputSourceType = "none";
        }

        setChanged();
        sendData();
    }

    public double getRudderCommand() {
        return rudderCommand;
    }

    public double getElevatorCommand() {
        return elevatorCommand;
    }

    public double getThrottleCommand() {
        return throttleCommand;
    }

    public boolean isActive() {
        return getSpeed() != 0;
    }

    public String getInputSourceType() {
        return inputSourceType;
    }

    public InteractionResult handleUse(Player player, boolean sneaking) {
        if (level == null || level.isClientSide)
            return InteractionResult.SUCCESS;

        String status = String.format("Source: %s | Speed: %.0f | R: %.2f E: %.2f T: %.2f",
                inputSourceType, getSpeed(), rudderCommand, elevatorCommand, throttleCommand);
        player.displayClientMessage(Component.literal("§6[Controller] §r" + status), true);
        return InteractionResult.SUCCESS;
    }

    public void bindSensor(BlockPos sensorPos, String sensorId, Class<?> sensorValueType) {
        this.boundSensorPos = sensorPos;
        this.boundSensorId = sensorId;
        setChanged();
        sendData();
    }

    public boolean acceptsSensorBinding(BlockPos sensorPos, String sensorId, Class<?> sensorValueType) {
        return Number.class.isAssignableFrom(sensorValueType);
    }

    private double readBoundSensor() {
        if (boundSensorPos == null || level == null)
            return 0;
        BlockEntity be = level.getBlockEntity(boundSensorPos);
        if (!(be instanceof ISensor<?> sensor) || !sensor.getSensorId().equals(boundSensorId))
            return 0;
        if (!sensor.isReadingValid())
            return 0;
        Object value = sensor.getValue();
        if (value instanceof Number number)
            return number.doubleValue();
        return 0;
    }

    private <T extends BlockEntity> T findNearby(Class<T> type) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    mutable.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    BlockEntity be = level.getBlockEntity(mutable);
                    if (type.isInstance(be))
                        return type.cast(be);
                }
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("RudderCommand", rudderCommand);
        tag.putDouble("ElevatorCommand", elevatorCommand);
        tag.putDouble("ThrottleCommand", throttleCommand);
        tag.putString("InputSourceType", inputSourceType);
        tag.putString("BoundSensorId", boundSensorId);
        if (boundSensorPos != null) {
            tag.putInt("BoundSensorX", boundSensorPos.getX());
            tag.putInt("BoundSensorY", boundSensorPos.getY());
            tag.putInt("BoundSensorZ", boundSensorPos.getZ());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        rudderCommand = tag.getDouble("RudderCommand");
        elevatorCommand = tag.getDouble("ElevatorCommand");
        throttleCommand = tag.getDouble("ThrottleCommand");
        inputSourceType = tag.getString("InputSourceType");
        boundSensorId = tag.getString("BoundSensorId");
        if (tag.contains("BoundSensorX"))
            boundSensorPos = new BlockPos(tag.getInt("BoundSensorX"), tag.getInt("BoundSensorY"), tag.getInt("BoundSensorZ"));
        else
            boundSensorPos = null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
