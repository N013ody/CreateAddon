package com.example.createaddon.content.guidance.link;

import java.util.List;

import com.example.createaddon.content.guidance.api.ISensor;
import com.example.createaddon.content.guidance.threshold.ThresholdControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class SensorBinderItem extends Item {
    private static final String SOURCE_X = "SourceX";
    private static final String SOURCE_Y = "SourceY";
    private static final String SOURCE_Z = "SourceZ";
    private static final String SOURCE_ID = "SourceSensorId";
    private static final String SOURCE_DIMENSION = "SourceDimension";

    public SensorBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide)
            return InteractionResult.SUCCESS;

        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof ISensor<?> sensor) {
            CompoundTag tag = getOrCreateTag(context.getItemInHand());
            BlockPos pos = context.getClickedPos();
            tag.putInt(SOURCE_X, pos.getX());
            tag.putInt(SOURCE_Y, pos.getY());
            tag.putInt(SOURCE_Z, pos.getZ());
            tag.putString(SOURCE_ID, sensor.getSensorId());
            tag.putString(SOURCE_DIMENSION, context.getLevel().dimension().location().toString());
            context.getItemInHand().set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            if (context.getPlayer() != null)
                context.getPlayer().displayClientMessage(Component.translatable("message.createaddon.sensor_binder.source", sensor.getSensorId()), true);
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = getTag(context.getItemInHand());
        if (tag == null || !tag.contains(SOURCE_ID)) {
            if (context.getPlayer() != null)
                context.getPlayer().displayClientMessage(Component.translatable("message.createaddon.sensor_binder.no_source"), true);
            return InteractionResult.SUCCESS;
        }

        if (be instanceof ThresholdControllerBlockEntity threshold) {
            ResourceLocation sourceDimension = ResourceLocation.tryParse(tag.getString(SOURCE_DIMENSION));
            if (sourceDimension != null && !sourceDimension.equals(context.getLevel().dimension().location())) {
                if (context.getPlayer() != null)
                    context.getPlayer().displayClientMessage(Component.translatable("message.createaddon.sensor_binder.wrong_dimension"), true);
                return InteractionResult.SUCCESS;
            }

            BlockPos sourcePos = new BlockPos(tag.getInt(SOURCE_X), tag.getInt(SOURCE_Y), tag.getInt(SOURCE_Z));
            BlockEntity sourceBe = context.getLevel().getBlockEntity(sourcePos);
            if (!(sourceBe instanceof ISensor<?> sensor) || !sensor.getSensorId().equals(tag.getString(SOURCE_ID))) {
                if (context.getPlayer() != null)
                    context.getPlayer().displayClientMessage(Component.translatable("message.createaddon.sensor_binder.missing_source"), true);
                return InteractionResult.SUCCESS;
            }

            ThresholdControllerBlockEntity.ValueType valueType = sensor.getValueType() == Vec3.class
                    ? ThresholdControllerBlockEntity.ValueType.VEC3_MAGNITUDE
                    : ThresholdControllerBlockEntity.ValueType.DOUBLE;
            threshold.bindSensor(sourcePos, tag.getString(SOURCE_ID), valueType);
            if (context.getPlayer() != null)
                context.getPlayer().displayClientMessage(Component.translatable("message.createaddon.sensor_binder.bound", tag.getString(SOURCE_ID)), true);
            return InteractionResult.SUCCESS;
        }

        if (context.getPlayer() != null)
            context.getPlayer().displayClientMessage(Component.translatable("message.createaddon.sensor_binder.invalid_target"), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = getTag(stack);
        if (tag == null || !tag.contains(SOURCE_ID)) {
            tooltip.add(Component.translatable("item.createaddon.sensor_binder.tooltip.empty"));
            return;
        }

        BlockPos sourcePos = new BlockPos(tag.getInt(SOURCE_X), tag.getInt(SOURCE_Y), tag.getInt(SOURCE_Z));
        tooltip.add(Component.translatable("item.createaddon.sensor_binder.tooltip.bound", tag.getString(SOURCE_ID), sourcePos.toShortString()));
    }

    private static CompoundTag getOrCreateTag(net.minecraft.world.item.ItemStack stack) {
        CompoundTag tag = getTag(stack);
        return tag == null ? new CompoundTag() : tag.copy();
    }

    private static CompoundTag getTag(net.minecraft.world.item.ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }
}
