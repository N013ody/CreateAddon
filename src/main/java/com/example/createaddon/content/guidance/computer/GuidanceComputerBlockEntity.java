package com.example.createaddon.content.guidance.computer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.createaddon.content.guidance.api.ISensor;
import com.example.createaddon.content.guidance.api.SensorReading;
import com.example.createaddon.registry.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GuidanceComputerBlockEntity extends SmartBlockEntity {
    private static final int DEFAULT_SCAN_RADIUS = 6;

    private final Map<String, ISensor<?>> sensors = new LinkedHashMap<>();
    private double desiredTerrainAltitude = 12.0D;
    private double rudderCommand;
    private double elevatorCommand;
    private double throttleCommand;

    public GuidanceComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUIDANCE_COMPUTER.get(), pos, state);
        setLazyTickRate(10);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide)
            return;

        runTerrainFollowingAndAutoRudder();
        if (level.getGameTime() % 5 == 0)
            sendData();
        setChanged();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null)
            return;
        refreshSubscriptions(DEFAULT_SCAN_RADIUS);
    }

    public void refreshSubscriptions(int radius) {
        sensors.clear();
        BlockPos.betweenClosedStream(worldPosition.offset(-radius, -radius, -radius), worldPosition.offset(radius, radius, radius))
                .forEach(pos -> {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof ISensor<?> sensor)
                        sensors.put(sensor.getSensorId(), sensor);
                });
    }

    public ISensor<?> getSensor(String sensorId) {
        return sensors.get(sensorId);
    }

    public Map<String, SensorReading<?>> readAllSensors() {
        Map<String, SensorReading<?>> readings = new LinkedHashMap<>();
        sensors.forEach((id, sensor) -> readings.put(id, sensor.readSensor()));
        return readings;
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

    public double getDesiredTerrainAltitude() {
        return desiredTerrainAltitude;
    }

    public void setDesiredTerrainAltitude(double desiredTerrainAltitude) {
        this.desiredTerrainAltitude = Math.max(1.0D, desiredTerrainAltitude);
        setChanged();
    }

    private void runTerrainFollowingAndAutoRudder() {
        double terrainAltitude = readDouble("terrain_altimeter", desiredTerrainAltitude);
        double bearingError = readDouble("relative_bearing", 0.0D);
        double driftAngle = readDouble("inertial_drift", 0.0D);
        double closingSpeed = readDouble("closing_speed", 0.0D);
        double structuralStress = readDouble("structural_stress", 0.0D);

        elevatorCommand = clamp((desiredTerrainAltitude - terrainAltitude) / 16.0D, -1.0D, 1.0D);
        rudderCommand = clamp((bearingError * 0.025D) - (driftAngle * 0.015D), -1.0D, 1.0D);
        throttleCommand = clamp(1.0D - Math.max(0.0D, closingSpeed) / 80.0D - structuralStress * 0.75D, 0.0D, 1.0D);
    }

    private double readDouble(String sensorId, double fallback) {
        ISensor<?> sensor = sensors.get(sensorId);
        if (sensor == null || !sensor.isReadingValid())
            return fallback;
        Object value = sensor.getValue();
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("DesiredTerrainAltitude", desiredTerrainAltitude);
        tag.putDouble("RudderCommand", rudderCommand);
        tag.putDouble("ElevatorCommand", elevatorCommand);
        tag.putDouble("ThrottleCommand", throttleCommand);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("DesiredTerrainAltitude"))
            desiredTerrainAltitude = tag.getDouble("DesiredTerrainAltitude");
        rudderCommand = tag.getDouble("RudderCommand");
        elevatorCommand = tag.getDouble("ElevatorCommand");
        throttleCommand = tag.getDouble("ThrottleCommand");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
