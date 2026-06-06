package io.github.n013ody.createaddon.client;

import org.joml.Matrix4f;

import io.github.n013ody.createaddon.content.guidance.controller.ServoMountBlockEntity;
import io.github.n013ody.createaddon.content.guidance.controller.ServoMountBlockEntity.CommandChannel;
import io.github.n013ody.createaddon.content.guidance.controller.ServoMountBlockEntity.TargetType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * 伺服安装座渲染器
 * 显示伺服的执行状态和目标类型
 */
public class ServoMountRenderer implements BlockEntityRenderer<ServoMountBlockEntity> {

    // 通道颜色
    private static final int RUDDER_COLOR = 0xFF00FF00;      // 绿色
    private static final int ELEVATOR_COLOR = 0xFF0088FF;    // 蓝色
    private static final int THROTTLE_COLOR = 0xFFFF8800;    // 橙色

    // 目标类型颜色
    private static final int BEARING_COLOR = 0xFFFFFF00;    // 黄色
    private static final int LINEAR_COLOR = 0xFF00FFFF;      // 青色

    public ServoMountRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ServoMountBlockEntity servo) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public void render(ServoMountBlockEntity servo, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!servo.isActive())
            return;

        // 渲染通道指示器
        renderChannelIndicator(poseStack, bufferSource, packedOverlay, servo.getChannel(), servo.getCurrentAngle(), servo.getCurrentOffset());

        // 渲染目标类型指示器
        renderTargetTypeIndicator(poseStack, bufferSource, packedOverlay, servo.getTargetType());

        // 渲染范围指示器
        renderRangeIndicator(poseStack, bufferSource, packedOverlay, servo.getAngleRange(), servo.getLinearRange(), servo.getTargetType());

        // 渲染反转状态
        if (servo.isInverted() && servo.getLevel() != null) {
            renderInvertedIndicator(poseStack, bufferSource, packedOverlay, servo.getLevel().getGameTime() + partialTick);
        }

        // 渲染活动状态
        if (servo.getLevel() != null) {
            renderActiveIndicator(poseStack, bufferSource, packedOverlay, servo.getLevel().getGameTime() + partialTick);
        }
    }

    /**
     * 渲染通道指示器
     */
    private void renderChannelIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay,
                                         CommandChannel channel, float currentAngle, float currentOffset) {
        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 获取通道颜色
        int color = switch (channel) {
            case RUDDER -> RUDDER_COLOR;
            case ELEVATOR -> ELEVATOR_COLOR;
            case THROTTLE -> THROTTLE_COLOR;
        };

        // 计算指示器位置
        float x, y, z;
        if (channel == CommandChannel.RUDDER) {
            x = 0.5F;
            y = 0.9F;
            z = 0.5F;
        } else if (channel == CommandChannel.ELEVATOR) {
            x = 0.5F;
            y = 0.8F;
            z = 0.5F;
        } else {
            x = 0.5F;
            y = 0.7F;
            z = 0.5F;
        }

        // 计算指示器大小
        float size;
        if (channel == CommandChannel.THROTTLE) {
            size = currentOffset * 0.1F;
        } else {
            size = Math.abs(currentAngle) / 180.0F * 0.15F;
        }

        // 渲染指示器
        int alpha = (int) (180 + 75 * Math.abs(size / 0.15F));
        int colorWithAlpha = color & 0x00FFFFFF | (alpha << 24);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        drawChannelBar(consumer, m, overlay, x, y, z, size, colorWithAlpha, channel);

        poseStack.popPose();
    }

    /**
     * 渲染目标类型指示器
     */
    private void renderTargetTypeIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay,
                                            TargetType targetType) {
        if (targetType == TargetType.NONE)
            return;

        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 获取目标类型颜色
        int color = switch (targetType) {
            case BEARING -> BEARING_COLOR;
            case LINEAR -> LINEAR_COLOR;
            case NONE -> 0;
        };

        // 渲染目标类型指示器（小方块）
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        int alpha = 200;
        int colorWithAlpha = color & 0x00FFFFFF | (alpha << 24);

        drawSmallCube(consumer, m, overlay, 0.15F, 0.15F, 0.15F, 0.25F, 0.25F, 0.25F, colorWithAlpha);

        poseStack.popPose();
    }

    /**
     * 渲染范围指示器
     */
    private void renderRangeIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay,
                                       float angleRange, float linearRange, TargetType targetType) {
        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 计算范围指示器大小
        float range;
        if (targetType == TargetType.LINEAR) {
            range = linearRange / 6.0F * 0.1F;
        } else {
            range = angleRange / 180.0F * 0.15F;
        }

        // 渲染范围指示器（环形）
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        int alpha = 100;
        int color = 0xFFFFFFFF & 0x00FFFFFF | (alpha << 24);

        drawRangeRing(consumer, m, overlay, 0.5F, 0.5F, 0.5F, range, color);

        poseStack.popPose();
    }

    /**
     * 渲染反转状态指示器
     */
    private void renderInvertedIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay, double time) {
        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 闪烁效果
        float pulse = (float) (0.5F + 0.5F * Math.sin(time * 0.2));
        int alpha = (int) (150 * pulse);

        // 渲染反转指示器（红色闪烁）
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        int color = 0xFFFF0000 & 0x00FFFFFF | (alpha << 24);

        drawSmallCube(consumer, m, overlay, 0.85F, 0.15F, 0.15F, 0.95F, 0.25F, 0.25F, color);

        poseStack.popPose();
    }

    /**
     * 渲染活动状态指示器
     */
    private void renderActiveIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay, double time) {
        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 脉冲效果
        float pulse = (float) (0.7F + 0.3F * Math.sin(time * 0.15));
        int alpha = (int) (200 * pulse);

        // 渲染活动指示器（绿色）
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        int color = 0xFF00FF00 & 0x00FFFFFF | (alpha << 24);

        drawQuad(consumer, m, overlay,
                0.45F, 1.0F, 0.45F,
                0.55F, 1.0F, 0.45F,
                0.55F, 1.0F, 0.55F,
                0.45F, 1.0F, 0.55F,
                color);

        poseStack.popPose();
    }

    /**
     * 绘制通道条
     */
    private void drawChannelBar(VertexConsumer consumer, Matrix4f m, int overlay,
                                 float cx, float cy, float cz, float size, int color,
                                 CommandChannel channel) {
        int a = color >>> 24 & 0xFF;
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;

        // 根据通道类型绘制不同方向的条
        float x1, y1, z1, x2, y2, z2;
        if (channel == CommandChannel.RUDDER) {
            // 水平条
            x1 = cx - size;
            y1 = cy;
            z1 = cz - 0.02F;
            x2 = cx + size;
            y2 = cy;
            z2 = cz + 0.02F;
        } else if (channel == CommandChannel.ELEVATOR) {
            // 垂直条
            x1 = cx - 0.02F;
            y1 = cy - size;
            z1 = cz - 0.02F;
            x2 = cx + 0.02F;
            y2 = cy + size;
            z2 = cz + 0.02F;
        } else {
            // 深度条
            x1 = cx - 0.02F;
            y1 = cy;
            z1 = cz - size;
            x2 = cx + 0.02F;
            y2 = cy;
            z2 = cz + size;
        }

        // 绘制条
        consumer.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x1, y2, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
    }

    /**
     * 绘制小立方体
     */
    private void drawSmallCube(VertexConsumer consumer, Matrix4f m, int overlay,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                int color) {
        int a = color >>> 24 & 0xFF;
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;

        // 前面
        consumer.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        consumer.addVertex(m, x2, y1, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        consumer.addVertex(m, x2, y2, z1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        consumer.addVertex(m, x1, y2, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);

        // 后面
        consumer.addVertex(m, x2, y1, z2).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
        consumer.addVertex(m, x1, y1, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
        consumer.addVertex(m, x1, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
        consumer.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, -1);
    }

    /**
     * 绘制范围环
     */
    private void drawRangeRing(VertexConsumer consumer, Matrix4f m, int overlay,
                                float cx, float cy, float cz, float radius, int color) {
        int a = color >>> 24 & 0xFF;
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;

        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1 = cx + (float) Math.cos(angle1) * radius;
            float z1 = cz + (float) Math.sin(angle1) * radius;
            float x2 = cx + (float) Math.cos(angle2) * radius;
            float z2 = cz + (float) Math.sin(angle2) * radius;

            // 绘制线段
            consumer.addVertex(m, x1, cy, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
            consumer.addVertex(m, x2, cy, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        }
    }

    /**
     * 绘制四边形
     */
    private void drawQuad(VertexConsumer consumer, Matrix4f m, int overlay,
                           float x1, float y1, float z1,
                           float x2, float y2, float z2,
                           float x3, float y3, float z3,
                           float x4, float y4, float z4,
                           int color) {
        int a = color >>> 24 & 0xFF;
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;

        consumer.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x4, y4, z4).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
    }
}
