package com.example.createaddon.content.guidance.sensor;

import java.util.List;

import com.example.createaddon.content.guidance.api.ISensor;
import com.example.createaddon.content.guidance.api.SensorReading;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractSensorBlockEntity<T> extends KineticBlockEntity implements ISensor<T> {
    protected final TargetSpec targetSpec = new TargetSpec();
    protected T value;
    protected boolean readingValid;
    protected Vec3 selfVelocity = Vec3.ZERO;
    protected Vec3 previousPosition;
    protected double desiredHeadingDegrees;
    protected double lastNeedleAngleDegrees;
    protected double lastLampIntensity;
    protected boolean lastTargetValid;
    protected Vec3 lastTargetPosition = Vec3.ZERO;
    protected double lastTargetDistance;
    protected String lastTargetEntityName = "";
    protected String lastTargetEntityId = "";

    protected AbstractSensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, T initialValue) {
        super(type, pos, state);
        this.value = initialValue;
        this.previousPosition = Vec3.atCenterOf(pos);
        setLazyTickRate(5);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;

        Vec3 currentPosition = sensorPosition();
        selfVelocity = currentPosition.subtract(previousPosition).scale(20.0D);
        previousPosition = currentPosition;

        if (!level.isClientSide) {
            TargetData target = targetSpec.resolve(level, currentPosition);
            lastTargetValid = target.valid();
            lastTargetPosition = target.position();
            lastTargetDistance = target.valid() ? currentPosition.distanceTo(target.position()) : 0.0D;
            lastTargetEntityName = target.entityName() == null ? "" : target.entityName();
            lastTargetEntityId = target.entityId() == null ? "" : target.entityId().toString();
            value = sample(currentPosition, selfVelocity, target);
            readingValid = isSampleValid(target);
            lastNeedleAngleDegrees = computeNeedleAngle(value);
            lastLampIntensity = computeLampIntensity(value, readingValid);

            if (level.getGameTime() % 5 == 0)
                sendData();
            setChanged();
        }
    }

    @Override
    public SensorReading<T> readSensor() {
        long time = level == null ? 0L : level.getGameTime();
        return new SensorReading<>(getSensorId(), value, time, readingValid);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isReadingValid() {
        return readingValid;
    }

    public TargetSpec getTargetSpec() {
        return targetSpec;
    }

    public boolean requiresTarget() {
        return true;
    }

    public boolean hasResolvedTarget() {
        return targetSpec != null;
    }

    public Vec3 getSelfVelocity() {
        return selfVelocity;
    }

    public Vec3 getLastTargetPosition() {
        return lastTargetPosition;
    }

    public double getLastTargetDistance() {
        return lastTargetDistance;
    }

    public String getLastTargetEntityName() {
        return lastTargetEntityName;
    }

    public String getLastTargetEntityId() {
        return lastTargetEntityId;
    }

    public double getDesiredHeadingDegrees() {
        return desiredHeadingDegrees;
    }

    public void setDesiredHeadingDegrees(double desiredHeadingDegrees) {
        this.desiredHeadingDegrees = GuidanceMath.wrapDegrees(desiredHeadingDegrees);
        setChanged();
    }

    public double getNeedleAngleDegrees() {
        return lastNeedleAngleDegrees;
    }

    public double getLampIntensity() {
        return lastLampIntensity;
    }

    public InteractionResult onUse(Player player) {
        if (level == null || level.isClientSide)
            return InteractionResult.SUCCESS;
        if (player.isSecondaryUseActive()) {
            targetSpec.clear();
            player.displayClientMessage(Component.translatable("message.createaddon.sensor.target_cleared"), true);
            return InteractionResult.SUCCESS;
        }
        Vec3 origin = sensorPosition();
        Entity nearest = targetSpec.resolveEntity(level, origin);
        if (nearest == null) {
            player.displayClientMessage(Component.translatable("message.createaddon.sensor.no_target"), true);
            return InteractionResult.SUCCESS;
        }
        targetSpec.setTargetEntity(nearest.getUUID());
        player.displayClientMessage(Component.translatable("message.createaddon.sensor.target_set", nearest.getDisplayName()), true);
        return InteractionResult.SUCCESS;
    }

    protected Vec3 sensorPosition() {
        return Vec3.atCenterOf(worldPosition);
    }

    protected double currentYawDegrees() {
        Vec3 horizontalVelocity = GuidanceMath.horizontal(selfVelocity);
        if (GuidanceMath.horizontalLength(horizontalVelocity) > 0.01D)
            return GuidanceMath.yawDegrees(horizontalVelocity);
        return desiredHeadingDegrees;
    }

    protected boolean isSampleValid(TargetData target) {
        return target.valid();
    }

    protected double computeNeedleAngle(T sampledValue) {
        return 0.0D;
    }

    protected double computeLampIntensity(T sampledValue, boolean valid) {
        return valid ? 1.0D : 0.0D;
    }

    protected abstract T sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target);

    protected abstract void writeSensorValue(CompoundTag tag, T value);

    protected abstract T readSensorValue(CompoundTag tag);

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("ReadingValid", readingValid);
        tag.putDouble("DesiredHeading", desiredHeadingDegrees);
        tag.putDouble("NeedleAngle", lastNeedleAngleDegrees);
        tag.putDouble("LampIntensity", lastLampIntensity);
        tag.putBoolean("LastTargetValid", lastTargetValid);
        tag.putDouble("SelfVelocityX", selfVelocity.x);
        tag.putDouble("SelfVelocityY", selfVelocity.y);
        tag.putDouble("SelfVelocityZ", selfVelocity.z);
        tag.putDouble("TargetX", lastTargetPosition.x);
        tag.putDouble("TargetY", lastTargetPosition.y);
        tag.putDouble("TargetZ", lastTargetPosition.z);
        tag.putDouble("TargetDistance", lastTargetDistance);
        tag.putString("TargetEntityName", lastTargetEntityName);
        tag.putString("TargetEntityId", lastTargetEntityId);
        writeSensorValue(tag, value);
        if (!clientPacket)
            targetSpec.write(tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        readingValid = tag.getBoolean("ReadingValid");
        desiredHeadingDegrees = tag.getDouble("DesiredHeading");
        lastNeedleAngleDegrees = tag.getDouble("NeedleAngle");
        lastLampIntensity = tag.getDouble("LampIntensity");
        lastTargetValid = tag.getBoolean("LastTargetValid");
        selfVelocity = new Vec3(tag.getDouble("SelfVelocityX"), tag.getDouble("SelfVelocityY"), tag.getDouble("SelfVelocityZ"));
        lastTargetPosition = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        lastTargetDistance = tag.getDouble("TargetDistance");
        lastTargetEntityName = tag.getString("TargetEntityName");
        lastTargetEntityId = tag.getString("TargetEntityId");
        value = readSensorValue(tag);
        if (!clientPacket)
            targetSpec.read(tag);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
