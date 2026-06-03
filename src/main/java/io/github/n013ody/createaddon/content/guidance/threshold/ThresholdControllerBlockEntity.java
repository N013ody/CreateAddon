package io.github.n013ody.createaddon.content.guidance.threshold;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.n013ody.createaddon.content.guidance.api.ISensor;
import io.github.n013ody.createaddon.content.guidance.api.SensorReading;
import io.github.n013ody.createaddon.registry.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ThresholdControllerBlockEntity extends SmartBlockEntity {

    public enum CompareOp {
        GREATER(">"),
        LESS("<"),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<="),
        EQUAL("==");

        public final String symbol;

        CompareOp(String symbol) {
            this.symbol = symbol;
        }

        public boolean test(double value, double threshold) {
            return switch (this) {
                case GREATER -> value > threshold;
                case LESS -> value < threshold;
                case GREATER_EQUAL -> value >= threshold;
                case LESS_EQUAL -> value <= threshold;
                case EQUAL -> Math.abs(value - threshold) < 0.001;
            };
        }
    }

    public enum ValueType {
        DOUBLE,
        VEC3_MAGNITUDE
    }

    private static final int SCAN_RADIUS = 6;

    private final Map<String, ISensor<?>> sensors = new LinkedHashMap<>();
    private String selectedSensorId = "";
    private CompareOp compareOp = CompareOp.GREATER;
    private ValueType valueType = ValueType.DOUBLE;
    private double threshold = 0.0D;
    private boolean outputSignal;
    private double lastReadValue;
    private BlockPos boundSensorPos;

    public ThresholdControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THRESHOLD_CONTROLLER.get(), pos, state);
        setLazyTickRate(10);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide)
            return;

        boolean oldSignal = outputSignal;
        outputSignal = evaluate();

        if (outputSignal != oldSignal) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            setChanged();
        }

        if (level.getGameTime() % 5 == 0)
            sendData();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null)
            return;
        refreshSubscriptions();
    }

    private void refreshSubscriptions() {
        sensors.clear();
        BlockPos.betweenClosedStream(
                worldPosition.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                worldPosition.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))
                .forEach(pos -> {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ISensor<?> sensor)
                        sensors.put(sensor.getSensorId(), sensor);
                });

        if (boundSensorPos == null && !selectedSensorId.isEmpty() && !sensors.containsKey(selectedSensorId)) {
            selectedSensorId = "";
        }
    }

    private boolean evaluate() {
        if (selectedSensorId.isEmpty())
            return false;

        ISensor<?> sensor = boundSensorPos == null ? sensors.get(selectedSensorId) : getBoundSensor();
        if (sensor == null || !sensor.isReadingValid())
            return false;

        Object rawValue = sensor.getValue();
        double value;

        if (valueType == ValueType.VEC3_MAGNITUDE) {
            if (rawValue instanceof net.minecraft.world.phys.Vec3 vec3) {
                value = vec3.length();
            } else if (rawValue instanceof Number number) {
                value = number.doubleValue();
            } else {
                return false;
            }
        } else {
            if (rawValue instanceof Number number) {
                value = number.doubleValue();
            } else {
                return false;
            }
        }

        lastReadValue = value;
        return compareOp.test(value, threshold);
    }

    public InteractionResult handleUse(Player player, boolean sneaking) {
        if (level == null || level.isClientSide)
            return InteractionResult.SUCCESS;

        if (sneaking) {
            compareOp = CompareOp.values()[(compareOp.ordinal() + 1) % CompareOp.values().length];
            player.displayClientMessage(
                    Component.translatable("message.createaddon.threshold.op", compareOp.symbol), true);
        } else {
            if (sensors.isEmpty()) {
                player.displayClientMessage(
                        Component.translatable("message.createaddon.threshold.no_sensor"), true);
                return InteractionResult.SUCCESS;
            }

            if (ThresholdControllerManager.isPending(player)) {
                ThresholdControllerManager.markPending(player, worldPosition);
                player.displayClientMessage(
                        Component.literal("搂eType a number in chat to set threshold (current: " + threshold + ")"), true);
                return InteractionResult.SUCCESS;
            }

            String[] ids = sensors.keySet().toArray(new String[0]);
            int currentIdx = -1;
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].equals(selectedSensorId)) {
                    currentIdx = i;
                    break;
                }
            }
            int nextIdx = (currentIdx + 1) % ids.length;
            selectedSensorId = ids[nextIdx];
            boundSensorPos = null;

            ISensor<?> sensor = sensors.get(selectedSensorId);
            if (sensor.getValue() instanceof net.minecraft.world.phys.Vec3) {
                valueType = ValueType.VEC3_MAGNITUDE;
            } else {
                valueType = ValueType.DOUBLE;
            }

            player.displayClientMessage(
                    Component.translatable("message.createaddon.threshold.sensor", selectedSensorId), true);
            player.displayClientMessage(
                    Component.literal("搂7Operator: " + compareOp.symbol + " | Threshold: " + threshold + " | Sneak+RMB to change op"), true);
            player.displayClientMessage(
                    Component.literal("搂eSend a number in chat to set threshold"), true);
            ThresholdControllerManager.markPending(player, worldPosition);
        }

        setChanged();
        sendData();
        return InteractionResult.SUCCESS;
    }

    public int getOutputSignal() {
        return outputSignal ? 15 : 0;
    }

    public void bindSensor(BlockPos sensorPos, String sensorId) {
        bindSensor(sensorPos, sensorId, valueType);
    }

    public void bindSensor(BlockPos sensorPos, String sensorId, ValueType valueType) {
        this.boundSensorPos = sensorPos;
        this.selectedSensorId = sensorId;
        this.valueType = valueType;
        setChanged();
        sendData();
    }

    private ISensor<?> getBoundSensor() {
        if (boundSensorPos == null || level == null)
            return null;
        BlockEntity be = level.getBlockEntity(boundSensorPos);
        if (be instanceof ISensor<?> sensor && sensor.getSensorId().equals(selectedSensorId))
            return sensor;
        return null;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
        setChanged();
    }

    public String getSelectedSensorId() {
        return selectedSensorId;
    }

    public CompareOp getCompareOp() {
        return compareOp;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getLastReadValue() {
        return lastReadValue;
    }

    public ValueType getValueType() {
        return valueType;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("SelectedSensor", selectedSensorId);
        tag.putString("CompareOp", compareOp.name());
        tag.putString("ValueType", valueType.name());
        tag.putDouble("Threshold", threshold);
        tag.putBoolean("OutputSignal", outputSignal);
        tag.putDouble("LastReadValue", lastReadValue);
        if (boundSensorPos != null) {
            tag.putInt("BoundSensorX", boundSensorPos.getX());
            tag.putInt("BoundSensorY", boundSensorPos.getY());
            tag.putInt("BoundSensorZ", boundSensorPos.getZ());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        selectedSensorId = tag.getString("SelectedSensor");
        try { compareOp = CompareOp.valueOf(tag.getString("CompareOp")); } catch (Exception ignored) {}
        try { valueType = ValueType.valueOf(tag.getString("ValueType")); } catch (Exception ignored) {}
        threshold = tag.getDouble("Threshold");
        outputSignal = tag.getBoolean("OutputSignal");
        lastReadValue = tag.getDouble("LastReadValue");
        if (tag.contains("BoundSensorX"))
            boundSensorPos = new BlockPos(tag.getInt("BoundSensorX"), tag.getInt("BoundSensorY"), tag.getInt("BoundSensorZ"));
    }

    @Override
    public void addBehaviours(java.util.List<BlockEntityBehaviour> behaviours) {
    }
}

