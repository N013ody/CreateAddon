package io.github.n013ody.createaddon.content.guidance.sensor;

import io.github.n013ody.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TerrainAltimeterBlockEntity extends DoubleSensorBlockEntity {
    private AltitudeMode altitudeMode = AltitudeMode.TERRAIN;

    public TerrainAltimeterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TERRAIN_ALTIMETER.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "terrain_altimeter";
    }

    public AltitudeMode getAltitudeMode() {
        return altitudeMode;
    }

    public void setAltitudeMode(AltitudeMode altitudeMode) {
        this.altitudeMode = altitudeMode;
        setChanged();
    }

    @Override
    public InteractionResult onUse(Player player) {
        if (level == null || level.isClientSide)
            return InteractionResult.SUCCESS;
        AltitudeMode next = altitudeMode == AltitudeMode.TERRAIN ? AltitudeMode.SEA_LEVEL : AltitudeMode.TERRAIN;
        setAltitudeMode(next);
        notifyUpdate();
        player.displayClientMessage(Component.translatable("message.createaddon.altimeter_mode." + next.name().toLowerCase(java.util.Locale.ROOT)), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    protected Double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        if (level == null)
            return 0.0D;
        if (altitudeMode == AltitudeMode.SEA_LEVEL)
            return selfPosition.y - level.getSeaLevel();

        double sensorBottomY = worldPosition.getY();
        for (BlockPos scanPos = worldPosition.below(); scanPos.getY() >= level.getMinBuildHeight(); scanPos = scanPos.below()) {
            BlockState state = level.getBlockState(scanPos);
            VoxelShape shape = state.getCollisionShape(level, scanPos, CollisionContext.empty());
            if (shape.isEmpty())
                continue;

            double surfaceY = scanPos.getY() + shape.max(Axis.Y);
            return Math.max(0.0D, sensorBottomY - surfaceY);
        }

        return sensorBottomY - level.getMinBuildHeight();
    }

    @Override
    protected boolean isSampleValid(TargetData target) {
        return level != null;
    }

    @Override
    protected double computeLampIntensity(Double sampledValue, boolean valid) {
        return valid ? 1.0D - GuidanceMath.clamp01(sampledValue / 64.0D) : 0.0D;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("AltitudeMode", altitudeMode.name());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("AltitudeMode")) {
            try {
                altitudeMode = AltitudeMode.valueOf(tag.getString("AltitudeMode"));
            } catch (IllegalArgumentException ignored) {
                altitudeMode = AltitudeMode.TERRAIN;
            }
        }
    }
}

