package io.github.n013ody.createaddon.content.guidance.sensor;

import io.github.n013ody.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RadarIndexerBlockEntity extends Vec3SensorBlockEntity {
    private ScanMode scanMode = ScanMode.ENTITY;
    private double searchRadius = 64.0D;

    public RadarIndexerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADAR_INDEXER.get(), pos, state);
    }

    @Override
    public String getSensorId() {
        return "radar_indexer";
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    protected Vec3 sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        if (level == null || level.isClientSide)
            return Vec3.ZERO;

        switch (scanMode) {
            case ENTITY:
                return scanNearestEntity(selfPosition);
            case BLOCK:
                return Vec3.atBottomCenterOf(worldPosition);
            default:
                return Vec3.ZERO;
        }
    }

    private Vec3 scanNearestEntity(Vec3 origin) {
        AABB box = new AABB(origin.x - searchRadius, origin.y - searchRadius, origin.z - searchRadius,
                origin.x + searchRadius, origin.y + searchRadius, origin.z + searchRadius);
        Entity nearest = level.getEntitiesOfClass(Entity.class, box, e -> e.isAlive() && !e.isSpectator())
                .stream().min(java.util.Comparator.comparingDouble(e -> e.position().distanceToSqr(origin)))
                .orElse(null);
        if (nearest == null)
            return Vec3.ZERO;
        return nearest.position();
    }

    public ScanMode getScanMode() {
        return scanMode;
    }

    public void cycleScanMode() {
        scanMode = ScanMode.values()[(scanMode.ordinal() + 1) % ScanMode.values().length];
        setChanged();
    }

    public enum ScanMode {
        ENTITY, BLOCK
    }

    @Override
    protected double computeLampIntensity(Vec3 sampledValue, boolean valid) {
        return valid && sampledValue != Vec3.ZERO ? 1.0D : 0.0D;
    }

    @Override
    protected void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("ScanMode", scanMode.name());
        tag.putDouble("SearchRadius", searchRadius);
    }

    @Override
    protected void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("ScanMode")) {
            try { scanMode = ScanMode.valueOf(tag.getString("ScanMode")); } catch (IllegalArgumentException ignored) {}
        }
        if (tag.contains("SearchRadius"))
            searchRadius = tag.getDouble("SearchRadius");
    }
}

