package io.github.n013ody.createaddon.content.guidance.controller;

import java.util.List;
import java.util.Locale;

import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ServoMountBlockEntity extends SmartBlockEntity {

    public enum CommandChannel {
        RUDDER, ELEVATOR, THROTTLE;

        public String displayName() {
            return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT);
        }
    }

    public enum TargetType {
        NONE, BEARING, LINEAR
    }

    private static final int CONTROLLER_SCAN_RADIUS = 8;
    private static final float[] ANGLE_PRESETS = {15f, 30f, 45f, 90f, 180f};
    private static final float[] LINEAR_PRESETS = {1f, 2f, 3f, 4f, 5f, 6f};

    private CommandChannel channel = CommandChannel.RUDDER;
    private int anglePresetIndex = 2;
    private float angleRange = 45f;
    private int linearPresetIndex = 5;
    private float linearRange = 6f;
    private boolean inverted = false;

    private TargetType targetType = TargetType.NONE;
    private float currentAngle;
    private float currentOffset;
    private boolean active;

    private BlockPos targetPos;
    private BlockPos controllerPos;

    public ServoMountBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SERVO_MOUNT.get(), pos, state);
        setLazyTickRate(10);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide)
            return;

        if (targetPos == null || controllerPos == null) {
            active = false;
            return;
        }

        BlockEntity controllerBe = level.getBlockEntity(controllerPos);
        if (!(controllerBe instanceof GuidanceControllerBlockEntity controller) || !controller.isActive()) {
            active = false;
            return;
        }

        double rawCommand = switch (channel) {
            case RUDDER -> controller.getRudderCommand();
            case ELEVATOR -> controller.getElevatorCommand();
            case THROTTLE -> controller.getThrottleCommand();
        };

        if (inverted)
            rawCommand = -rawCommand;

        BlockEntity targetBe = level.getBlockEntity(targetPos);

        if (targetType == TargetType.BEARING) {
            if (!(targetBe instanceof MechanicalBearingBlockEntity bearing)) {
                active = false;
                targetType = TargetType.NONE;
                targetPos = null;
                return;
            }
            if (!bearing.isRunning()) {
                active = false;
                return;
            }
            float targetAngle = (float) (rawCommand * angleRange);
            float compensated = targetAngle - bearing.getAngularSpeed();
            bearing.setAngle(compensated);
            currentAngle = targetAngle;
            active = true;
        } else if (targetType == TargetType.LINEAR) {
            if (!(targetBe instanceof LinearActuatorBlockEntity actuator)) {
                active = false;
                targetType = TargetType.NONE;
                targetPos = null;
                return;
            }
            if (!actuator.running) {
                active = false;
                return;
            }
            int extensionRange = getLinearExtensionRange(actuator);
            float maxExt = Math.min(linearRange, extensionRange);

            float targetOffset;
            if (channel == CommandChannel.THROTTLE) {
                targetOffset = (float) (Math.max(0, Math.min(1, rawCommand)) * maxExt);
            } else {
                targetOffset = (float) ((rawCommand + 1.0) * 0.5 * maxExt);
            }
            actuator.offset = targetOffset;
            currentOffset = targetOffset;
            active = true;
        } else {
            active = false;
        }

        if (level.getGameTime() % 5 == 0)
            sendData();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null)
            return;
        scanForTarget();
        scanForController();
        setChanged();
        sendData();
    }

    private void scanForTarget() {
        targetPos = null;
        targetType = TargetType.NONE;

        for (Direction dir : Direction.values()) {
            BlockPos adjacent = worldPosition.relative(dir);
            BlockEntity be = level.getBlockEntity(adjacent);
            if (be instanceof MechanicalBearingBlockEntity) {
                targetPos = adjacent;
                targetType = TargetType.BEARING;
                return;
            }
            if (be instanceof LinearActuatorBlockEntity) {
                targetPos = adjacent;
                targetType = TargetType.LINEAR;
                return;
            }
        }
    }

    private void scanForController() {
        controllerPos = null;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -CONTROLLER_SCAN_RADIUS; dx <= CONTROLLER_SCAN_RADIUS; dx++) {
            for (int dy = -CONTROLLER_SCAN_RADIUS; dy <= CONTROLLER_SCAN_RADIUS; dy++) {
                for (int dz = -CONTROLLER_SCAN_RADIUS; dz <= CONTROLLER_SCAN_RADIUS; dz++) {
                    mutable.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    BlockEntity be = level.getBlockEntity(mutable);
                    if (be instanceof GuidanceControllerBlockEntity) {
                        controllerPos = mutable.immutable();
                        return;
                    }
                }
            }
        }
    }

    private int getLinearExtensionRange(LinearActuatorBlockEntity actuator) {
        try {
            var method = LinearActuatorBlockEntity.class.getDeclaredMethod("getExtensionRange");
            method.setAccessible(true);
            return (int) method.invoke(actuator);
        } catch (Exception e) {
            return 16;
        }
    }

    public InteractionResult handleUse(Player player, boolean sneaking) {
        if (level == null || level.isClientSide)
            return InteractionResult.SUCCESS;

        if (sneaking) {
            if (targetType == TargetType.BEARING) {
                anglePresetIndex = (anglePresetIndex + 1) % (ANGLE_PRESETS.length + 1);
                if (anglePresetIndex == ANGLE_PRESETS.length) {
                    inverted = !inverted;
                    anglePresetIndex = 0;
                    angleRange = ANGLE_PRESETS[0];
                    player.displayClientMessage(Component.literal(
                            String.format("§6[Servo] §eInverted: %s §7| Range: ±%.0f°", inverted ? "ON" : "OFF", angleRange)), true);
                } else {
                    angleRange = ANGLE_PRESETS[anglePresetIndex];
                    player.displayClientMessage(Component.literal(
                            String.format("§6[Servo] §rRange: ±%.0f° §7| Inverted: %s", angleRange, inverted ? "ON" : "OFF")), true);
                }
            } else if (targetType == TargetType.LINEAR) {
                linearPresetIndex = (linearPresetIndex + 1) % (LINEAR_PRESETS.length + 1);
                if (linearPresetIndex == LINEAR_PRESETS.length) {
                    inverted = !inverted;
                    linearPresetIndex = 0;
                    linearRange = LINEAR_PRESETS[0];
                    player.displayClientMessage(Component.literal(
                            String.format("§6[Servo] §eInverted: %s §7| Range: %.0f blocks", inverted ? "ON" : "OFF", linearRange)), true);
                } else {
                    linearRange = LINEAR_PRESETS[linearPresetIndex];
                    player.displayClientMessage(Component.literal(
                            String.format("§6[Servo] §rRange: %.0f blocks §7| Inverted: %s", linearRange, inverted ? "ON" : "OFF")), true);
                }
            } else {
                inverted = !inverted;
                player.displayClientMessage(Component.literal(
                        String.format("§6[Servo] §eInverted: %s", inverted ? "ON" : "OFF")), true);
            }
        } else {
            CommandChannel[] channels = CommandChannel.values();
            channel = channels[(channel.ordinal() + 1) % channels.length];

            String targetStr = switch (targetType) {
                case BEARING -> "Bearing";
                case LINEAR -> "Piston";
                case NONE -> "None";
            };
            String rangeStr = targetType == TargetType.LINEAR
                    ? String.format("%.0f blocks", linearRange)
                    : String.format("±%.0f°", angleRange);

            player.displayClientMessage(Component.literal(
                    String.format("§6[Servo] §rChannel: %s §7| Range: %s | Inverted: %s | Target: %s",
                            channel.displayName(), rangeStr, inverted ? "ON" : "OFF", targetStr)), true);
        }

        setChanged();
        sendData();
        return InteractionResult.SUCCESS;
    }

    public boolean isActive() { return active; }
    public CommandChannel getChannel() { return channel; }
    public TargetType getTargetType() { return targetType; }
    public boolean isInverted() { return inverted; }
    public float getCurrentAngle() { return currentAngle; }
    public float getCurrentOffset() { return currentOffset; }
    public float getAngleRange() { return angleRange; }
    public float getLinearRange() { return linearRange; }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Channel", channel.name());
        tag.putInt("AnglePresetIndex", anglePresetIndex);
        tag.putFloat("AngleRange", angleRange);
        tag.putInt("LinearPresetIndex", linearPresetIndex);
        tag.putFloat("LinearRange", linearRange);
        tag.putBoolean("Inverted", inverted);
        tag.putString("TargetType", targetType.name());
        tag.putFloat("CurrentAngle", currentAngle);
        tag.putFloat("CurrentOffset", currentOffset);
        tag.putBoolean("Active", active);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        try { channel = CommandChannel.valueOf(tag.getString("Channel")); } catch (Exception e) { channel = CommandChannel.RUDDER; }
        anglePresetIndex = tag.getInt("AnglePresetIndex");
        angleRange = tag.getFloat("AngleRange");
        if (angleRange == 0) angleRange = 45f;
        linearPresetIndex = tag.getInt("LinearPresetIndex");
        linearRange = tag.getFloat("LinearRange");
        if (linearRange == 0) linearRange = 6f;
        inverted = tag.getBoolean("Inverted");
        try { targetType = TargetType.valueOf(tag.getString("TargetType")); } catch (Exception e) { targetType = TargetType.NONE; }
        currentAngle = tag.getFloat("CurrentAngle");
        currentOffset = tag.getFloat("CurrentOffset");
        active = tag.getBoolean("Active");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }
}
