package io.github.n013ody.createaddon.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import io.github.n013ody.createaddon.content.guidance.controller.GuidanceControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 制导控制器渲染器
 * 显示控制指令的方向和强度
 */
public class GuidanceControllerRenderer implements BlockEntityRenderer<GuidanceControllerBlockEntity> {

    public GuidanceControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(GuidanceControllerBlockEntity controller) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public void render(GuidanceControllerBlockEntity controller, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!controller.isActive())
            return;

        Direction.Axis axis = controller.getBlockState().hasProperty(BlockStateProperties.AXIS)
                ? controller.getBlockState().getValue(BlockStateProperties.AXIS)
                : Direction.Axis.Y;

        // 获取控制指令
        double rudder = controller.getRudderCommand();
        double elevator = controller.getElevatorCommand();
        double throttle = controller.getThrottleCommand();

        // 渲染方向舵指示器
        renderCommandIndicator(poseStack, bufferSource, packedOverlay, axis, rudder, 0xFF00FF00, 0.0F);

        // 渲染升降舵指示器
        renderCommandIndicator(poseStack, bufferSource, packedOverlay, axis, elevator, 0xFF0088FF, 0.25F);

        // 渲染油门指示器
        renderThrottleIndicator(poseStack, bufferSource, packedOverlay, axis, throttle, 0xFFFF8800);

        // 渲染活动指示灯
        if (controller.getLevel() != null) {
            renderActiveIndicator(poseStack, bufferSource, packedOverlay, controller.getLevel().getGameTime() + partialTick);
        }
    }

    /**
     * 渲染控制指令指示器（方向舵/升降舵）
     */
    private void renderCommandIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay,
                                         Direction.Axis axis, double command, int color, float offset) {
        if (Math.abs(command) < 0.01)
            return;

        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 根据轴向设置位置
        float cx, cy, cz;
        if (axis == Direction.Axis.Y) {
            cx = 0.5F;
            cy = 0.85F + offset;
            cz = 0.5F;
        } else if (axis == Direction.Axis.X) {
            cx = 0.85F + offset;
            cy = 0.5F;
            cz = 0.5F;
        } else {
            cx = 0.5F;
            cy = 0.5F;
            cz = 0.85F + offset;
        }

        // 计算指示器长度
        float length = (float) (command * 0.3);
        float width = 0.02F;

        // 渲染指示器条
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        int alpha = (int) (180 + 75 * Math.abs(command));

        if (axis == Direction.Axis.Y) {
            // Y轴：水平指示器
            drawIndicatorBar(consumer, m, overlay,
                    cx - length, cy, cz - width,
                    cx + length, cy, cz + width,
                    color & 0x00FFFFFF | (alpha << 24));
        } else if (axis == Direction.Axis.X) {
            // X轴：垂直指示器
            drawIndicatorBar(consumer, m, overlay,
                    cx, cy - length, cz - width,
                    cx, cy + length, cz + width,
                    color & 0x00FFFFFF | (alpha << 24));
        } else {
            // Z轴：深度指示器
            drawIndicatorBar(consumer, m, overlay,
                    cx - width, cy - length, cz,
                    cx + width, cy + length, cz,
                    color & 0x00FFFFFF | (alpha << 24));
        }

        poseStack.popPose();
    }

    /**
     * 渲染油门指示器
     */
    private void renderThrottleIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay,
                                          Direction.Axis axis, double throttle, int color) {
        if (throttle < 0.01)
            return;

        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 根据轴向设置位置
        float cx, cy, cz;
        if (axis == Direction.Axis.Y) {
            cx = 0.5F;
            cy = 0.95F;
            cz = 0.5F;
        } else if (axis == Direction.Axis.X) {
            cx = 0.95F;
            cy = 0.5F;
            cz = 0.5F;
        } else {
            cx = 0.5F;
            cy = 0.5F;
            cz = 0.95F;
        }

        // 计算油门指示器大小
        float size = (float) (throttle * 0.15);
        int alpha = (int) (128 + 127 * throttle);

        // 渲染油门指示器（圆形）
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        int colorWithAlpha = color & 0x00FFFFFF | (alpha << 24);

        drawThrottleCircle(consumer, m, overlay, cx, cy, cz, size, colorWithAlpha, axis);

        poseStack.popPose();
    }

    /**
     * 渲染活动指示灯
     */
    private void renderActiveIndicator(PoseStack poseStack, MultiBufferSource bufferSource, int overlay, double time) {
        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();

        // 闪烁效果
        float pulse = (float) (0.7F + 0.3F * Math.sin(time * 0.15));
        int alpha = (int) (200 * pulse);

        // 渲染活动指示灯（绿色）
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
     * 绘制指示器条
     */
    private void drawIndicatorBar(VertexConsumer consumer, Matrix4f m, int overlay,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   int color) {
        int a = color >>> 24 & 0xFF;
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;

        // 上面
        consumer.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x1, y1, z2).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        consumer.addVertex(m, x2, y2, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
    }

    /**
     * 绘制油门圆形指示器
     */
    private void drawThrottleCircle(VertexConsumer consumer, Matrix4f m, int overlay,
                                     float cx, float cy, float cz, float size, int color,
                                     Direction.Axis axis) {
        int a = color >>> 24 & 0xFF;
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;

        int segments = 12;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1, y1, z1, x2, y2, z2;
            if (axis == Direction.Axis.Y) {
                x1 = cx + (float) Math.cos(angle1) * size;
                y1 = cy;
                z1 = cz + (float) Math.sin(angle1) * size;
                x2 = cx + (float) Math.cos(angle2) * size;
                y2 = cy;
                z2 = cz + (float) Math.sin(angle2) * size;
            } else if (axis == Direction.Axis.X) {
                x1 = cx;
                y1 = cy + (float) Math.cos(angle1) * size;
                z1 = cz + (float) Math.sin(angle1) * size;
                x2 = cx;
                y2 = cy + (float) Math.cos(angle2) * size;
                z2 = cz + (float) Math.sin(angle2) * size;
            } else {
                x1 = cx + (float) Math.cos(angle1) * size;
                y1 = cy + (float) Math.sin(angle1) * size;
                z1 = cz;
                x2 = cx + (float) Math.cos(angle2) * size;
                y2 = cy + (float) Math.sin(angle2) * size;
                z2 = cz;
            }

            // 绘制三角形
            consumer.addVertex(m, cx, cy, cz).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
            consumer.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
            consumer.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
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
