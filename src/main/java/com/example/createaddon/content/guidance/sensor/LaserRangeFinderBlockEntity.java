package com.example.createaddon.content.guidance.sensor;

import java.util.List;

import com.example.createaddon.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class LaserRangeFinderBlockEntity extends DoubleSensorBlockEntity {
    private double lastHitDistance;
    private double lastMaxRange;
    private Vec3 lastHitPos;
    private boolean hitEntity;
    private String lastHitType = "none";

    public LaserRangeFinderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LASER_RANGE_FINDER.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide && level.getGameTime() % 2 == 0)
            sendData();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        if (level != null)
            level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        write(tag, registries, true);
        return tag;
    }

    @Override
    public String getSensorId() {
        return "laser_range";
    }

    public double getLastHitDistance() {
        return lastHitDistance;
    }

    public double getLastMaxRange() {
        return lastMaxRange;
    }

    public Vec3 getLastHitPos() {
        return lastHitPos;
    }

    public boolean isHitEntity() {
        return hitEntity;
    }

    public String getLastHitType() {
        return lastHitType;
    }

    @Override
    protected Double sample(Vec3 selfPosition, Vec3 selfVelocity, TargetData target) {
        if (level == null)
            return 0.0D;

        double speed = Math.abs(getSpeed());
        if (speed < 1.0D) {
            lastMaxRange = 0.0D;
            lastHitDistance = 0.0D;
            lastHitPos = null;
            hitEntity = false;
            lastHitType = "none";
            return 0.0D;
        }

        double maxRange = speed * 0.5D;

        lastMaxRange = maxRange;

        Direction facing = getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)
                ? getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)
                : Direction.UP;

        Vec3 direction = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vec3 start = selfPosition.add(direction.scale(0.55D));
        Vec3 end = selfPosition.add(direction.scale(0.55D + maxRange));

        BlockHitResult blockHit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        double blockDist = blockHit.getType() == HitResult.Type.MISS ? maxRange : start.distanceTo(blockHit.getLocation());
        Vec3 blockHitPos = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();

        Vec3 entityHitPos = null;
        double entityDist = Double.MAX_VALUE;
        double beamRadius = 0.1D;
        AABB beamAABB = new AABB(start, blockHitPos);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, beamAABB.inflate(2.0D),
                e -> e instanceof LivingEntity && !(e instanceof Player) && e.isAlive() && !e.isSpectator());
        for (Entity entity : entities) {
            AABB entityBox = entity.getBoundingBox().inflate(beamRadius);
            var entityHit = entityBox.clip(start, blockHitPos);
            if (entityHit.isPresent()) {
                double dist = start.distanceTo(entityHit.get());
                if (dist < entityDist) {
                    entityDist = dist;
                    entityHitPos = entityHit.get();
                }
            }
        }

        double distance;
        if (entityDist < blockDist) {
            distance = entityDist;
            lastHitPos = entityHitPos;
            hitEntity = true;
            lastHitType = "entity";
        } else {
            distance = blockDist;
            lastHitPos = blockHitPos;
            hitEntity = false;
            lastHitType = blockHit.getType() == HitResult.Type.MISS ? "none" : "block";
        }

        lastHitDistance = distance;
        return distance;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    protected boolean isSampleValid(TargetData target) {
        return level != null && Math.abs(getSpeed()) >= 1.0D;
    }

    @Override
    protected double computeLampIntensity(Double sampledValue, boolean valid) {
        if (!valid)
            return 0.0D;
        double speed = Math.abs(getSpeed());
        double maxRange = Math.max(1.0D, speed * 0.5D);
        return 1.0D - GuidanceMath.clamp01(sampledValue / maxRange);
    }

    @Override
    protected void writeSensorValue(net.minecraft.nbt.CompoundTag tag, Double value) {
        super.writeSensorValue(tag, value);
        tag.putDouble("LastHitDistance", lastHitDistance);
        tag.putDouble("LastMaxRange", lastMaxRange);
        tag.putBoolean("HitEntity", hitEntity);
        tag.putString("HitType", lastHitType);
        if (lastHitPos != null) {
            tag.putDouble("HitX", lastHitPos.x);
            tag.putDouble("HitY", lastHitPos.y);
            tag.putDouble("HitZ", lastHitPos.z);
        }
    }

    @Override
    protected Double readSensorValue(net.minecraft.nbt.CompoundTag tag) {
        lastHitDistance = tag.getDouble("LastHitDistance");
        lastMaxRange = tag.getDouble("LastMaxRange");
        hitEntity = tag.getBoolean("HitEntity");
        lastHitType = tag.getString("HitType");
        if (tag.contains("HitX")) {
            lastHitPos = new Vec3(tag.getDouble("HitX"), tag.getDouble("HitY"), tag.getDouble("HitZ"));
        }
        return super.readSensorValue(tag);
    }
}
