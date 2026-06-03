package io.github.n013ody.createaddon.content.guidance.sensor;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TargetSpec {
    private UUID targetEntity;
    private Vec3 targetPoint;
    private double searchRange = 128.0D;

    public TargetData resolve(Level level, Vec3 origin) {
        if (targetPoint != null)
            return new TargetData(targetPoint, Vec3.ZERO, true);

        Entity entity = resolveEntity(level, origin);
        if (entity == null)
            return TargetData.INVALID;
        return new TargetData(entity.position(), entity.getDeltaMovement().scale(20.0D), true,
                entity.getUUID(), entity.getDisplayName().getString());
    }

    public void setTargetEntity(UUID targetEntity) {
        this.targetEntity = targetEntity;
        this.targetPoint = null;
    }

    public void setTargetPoint(Vec3 targetPoint) {
        this.targetPoint = targetPoint;
        this.targetEntity = null;
    }

    public void clear() {
        targetEntity = null;
        targetPoint = null;
    }

    public double getSearchRange() {
        return searchRange;
    }

    public void setSearchRange(double searchRange) {
        this.searchRange = Math.max(1.0D, searchRange);
    }

    public void write(CompoundTag tag) {
        tag.putDouble("SearchRange", searchRange);
        if (targetEntity != null)
            tag.putString("TargetEntity", targetEntity.toString());
        if (targetPoint != null) {
            tag.putDouble("TargetX", targetPoint.x);
            tag.putDouble("TargetY", targetPoint.y);
            tag.putDouble("TargetZ", targetPoint.z);
        }
    }

    public void read(CompoundTag tag) {
        if (tag.contains("SearchRange"))
            searchRange = tag.getDouble("SearchRange");

        targetEntity = null;
        targetPoint = null;
        if (tag.contains("TargetEntity")) {
            try {
                targetEntity = UUID.fromString(tag.getString("TargetEntity"));
            } catch (IllegalArgumentException ignored) {
                targetEntity = null;
            }
        }
        if (tag.contains("TargetX") && tag.contains("TargetY") && tag.contains("TargetZ"))
            targetPoint = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
    }

    public Entity resolveEntity(Level level, Vec3 origin) {
        if (targetEntity != null && level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(targetEntity);
            if (entity != null && entity.isAlive())
                return entity;
        }

        AABB box = new AABB(origin.x - searchRange, origin.y - searchRange, origin.z - searchRange,
                origin.x + searchRange, origin.y + searchRange, origin.z + searchRange);
        List<Entity> candidates = level.getEntitiesOfClass(Entity.class, box, entity -> entity.isAlive() && !entity.isSpectator());
        return candidates.stream()
                .min(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(origin)))
                .orElse(null);
    }
}

