package io.github.n013ody.createaddon.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import io.github.n013ody.createaddon.content.guidance.sensor.AbstractSensorBlockEntity;
import io.github.n013ody.createaddon.content.guidance.sensor.LaserRangeFinderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SensorDialBlockEntityRenderer<T extends AbstractSensorBlockEntity<?>> implements BlockEntityRenderer<T> {
    private static final RenderType TYPE = RenderType.solid();

    public SensorDialBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T sensor) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(T sensor) {
        if (sensor instanceof LaserRangeFinderBlockEntity) {
            double ox = sensor.getBlockPos().getX();
            double oy = sensor.getBlockPos().getY();
            double oz = sensor.getBlockPos().getZ();
            return new AABB(ox - 256, oy - 256, oz - 256, ox + 257, oy + 257, oz + 257);
        }
        return BlockEntityRenderer.super.getRenderBoundingBox(sensor);
    }

    @Override
    public void render(T sensor, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        Direction.Axis axis;
        if (sensor.getBlockState().hasProperty(BlockStateProperties.FACING)) {
            axis = sensor.getBlockState().getValue(BlockStateProperties.FACING).getAxis();
        } else if (sensor.getBlockState().hasProperty(BlockStateProperties.AXIS)) {
            axis = sensor.getBlockState().getValue(BlockStateProperties.AXIS);
        } else {
            axis = Direction.Axis.Y;
        }

        double needleAngle = SensorDialRenderer.pointerAngle(sensor);
        int lampColor = SensorDialRenderer.lampColor(sensor);

        VertexConsumer consumer = bufferSource.getBuffer(TYPE);

        poseStack.pushPose();
        if (axis == Direction.Axis.Y) {
            poseStack.translate(0.5D, 0.92D, 0.5D);
            poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(needleAngle)));
            drawNeedle(consumer, poseStack, packedOverlay);
            poseStack.popPose();
        } else if (axis == Direction.Axis.X) {
            poseStack.translate(0.92D, 0.5D, 0.5D);
            poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(needleAngle)));
            drawNeedle(consumer, poseStack, packedOverlay);
            poseStack.popPose();
        } else {
            poseStack.translate(0.5D, 0.5D, 0.92D);
            poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(needleAngle)));
            drawNeedle(consumer, poseStack, packedOverlay);
            poseStack.popPose();
        }

        poseStack.pushPose();
        drawLamp(consumer, poseStack, lampColor, axis, packedOverlay);
        poseStack.popPose();

        if (sensor instanceof LaserRangeFinderBlockEntity laser) {
            drawLaserBeam(laser, partialTick, poseStack, bufferSource, packedOverlay);
        }
    }

    private static void drawLaserBeam(LaserRangeFinderBlockEntity laser, float partialTick, PoseStack poseStack,
                                       MultiBufferSource bufferSource, int overlay) {
        Direction facing = laser.getBlockState().hasProperty(BlockStateProperties.FACING)
                ? laser.getBlockState().getValue(BlockStateProperties.FACING)
                : Direction.UP;

        double beamLength = laser.getLastHitDistance();
        if (beamLength <= 0.01D)
            return;

        double fx = facing.getStepX();
        double fy = facing.getStepY();
        double fz = facing.getStepZ();

        double cx = 0.5D;
        double cy = 0.5D;
        double cz = 0.5D;

        double sx = cx + fx * 0.55D;
        double sy = cy + fy * 0.55D;
        double sz = cz + fz * 0.55D;

        double ex = cx + fx * (0.55D + beamLength);
        double ey = cy + fy * (0.55D + beamLength);
        double ez = cz + fz * (0.55D + beamLength);

        VertexConsumer beam = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f m = poseStack.last().pose();

        float hs = 0.045F;
        float hs2 = hs * 0.7071F;

        double time = laser.getLevel() == null ? 0 : laser.getLevel().getGameTime() + partialTick;
        double pulse = 0.85D + 0.15D * Math.sin(time * 0.35D + beamLength * 0.1D);

        int sr = 255, sg = 35, sb = 20, sa = (int) (190 + 45 * pulse);
        int er = 230, eg = 0, eb = 0, ea = (int) (60 + 55 * pulse);

        // 4 quads with double-sided rendering
        drawBeamQuad(beam, m, overlay, sx, sy, sz, ex, ey, ez, hs, sr, sg, sb, sa, er, eg, eb, ea, true);
        drawBeamQuad(beam, m, overlay, sx, sy, sz, ex, ey, ez, hs, sr, sg, sb, sa, er, eg, eb, ea, false);
        drawBeamQuad(beam, m, overlay, sx, sy, sz, ex, ey, ez, hs2, sr, sg, sb, sa, er, eg, eb, ea, true);
        drawBeamQuad(beam, m, overlay, sx, sy, sz, ex, ey, ez, hs2, sr, sg, sb, sa, er, eg, eb, ea, false);

        drawHitEffect(laser, partialTick, poseStack, bufferSource, overlay);
    }

    private static void drawBeamQuad(VertexConsumer beam, Matrix4f m, int overlay,
                                      double sx, double sy, double sz,
                                      double ex, double ey, double ez,
                                      float halfSize,
                                      int sr, int sg, int sb, int sa,
                                      int er, int eg, int eb, int ea,
                                      boolean horizontal) {
        float x1, y1, z1, x2, y2, z2;
        float ex1, ey1, ez1, ex2, ey2, ez2;

        if (horizontal) {
            x1 = (float) sx; y1 = (float) (sy - halfSize); z1 = (float) sz;
            x2 = (float) sx; y2 = (float) (sy + halfSize); z2 = (float) sz;
            ex1 = (float) ex; ey1 = (float) (ey - halfSize); ez1 = (float) ez;
            ex2 = (float) ex; ey2 = (float) (ey + halfSize); ez2 = (float) ez;
        } else {
            x1 = (float) (sx - halfSize); y1 = (float) sy; z1 = (float) sz;
            x2 = (float) (sx + halfSize); y2 = (float) sy; z2 = (float) sz;
            ex1 = (float) (ex - halfSize); ey1 = (float) ey; ez1 = (float) ez;
            ex2 = (float) (ex + halfSize); ey2 = (float) ey; ez2 = (float) ez;
        }

        // Front face
        beam.addVertex(m, x1, y1, z1).setColor(sr, sg, sb, sa).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        beam.addVertex(m, x2, y2, z2).setColor(sr, sg, sb, sa).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        beam.addVertex(m, ex2, ey2, ez2).setColor(er, eg, eb, ea).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        beam.addVertex(m, ex1, ey1, ez1).setColor(er, eg, eb, ea).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);

        // Back face
        beam.addVertex(m, ex1, ey1, ez1).setColor(er, eg, eb, ea).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        beam.addVertex(m, ex2, ey2, ez2).setColor(er, eg, eb, ea).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        beam.addVertex(m, x2, y2, z2).setColor(sr, sg, sb, sa).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
        beam.addVertex(m, x1, y1, z1).setColor(sr, sg, sb, sa).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, -1, 0);
    }

    private static void drawHitEffect(LaserRangeFinderBlockEntity laser, float partialTick, PoseStack poseStack,
                                       MultiBufferSource bufferSource, int overlay) {
        Vec3 hitPos = laser.getLastHitPos();
        if (hitPos == null)
            return;

        Direction facing = laser.getBlockState().hasProperty(BlockStateProperties.FACING)
                ? laser.getBlockState().getValue(BlockStateProperties.FACING)
                : Direction.UP;

        BlockPos blockPos = laser.getBlockPos();
        double hx = hitPos.x - blockPos.getX();
        double hy = hitPos.y - blockPos.getY();
        double hz = hitPos.z - blockPos.getZ();

        VertexConsumer dot = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f m = poseStack.last().pose();

        int ax = Math.abs(facing.getStepX());
        int ay = Math.abs(facing.getStepY());
        int az = Math.abs(facing.getStepZ());

        double time = laser.getLevel() == null ? 0 : laser.getLevel().getGameTime() + partialTick;
        double pulse = 0.85D + 0.15D * Math.sin(time * 0.45D);

        drawHitQuad(dot, m, overlay, hx, hy, hz, (float) (0.18F + 0.025F * pulse), ax, ay, az, 220, 0, 0, 135);
        drawHitQuad(dot, m, overlay, hx, hy, hz, 0.08F, ax, ay, az, 255, 35, 20, 255);
    }

    private static void drawHitQuad(VertexConsumer dot, Matrix4f m, int overlay,
                                     double hx, double hy, double hz, float s,
                                     int ax, int ay, int az,
                                     int r, int g, int b, int a) {
        if (ax == 1) {
            dot.addVertex(m, (float) hx, (float) (hy - s), (float) (hz - s)).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            dot.addVertex(m, (float) hx, (float) (hy + s), (float) (hz - s)).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            dot.addVertex(m, (float) hx, (float) (hy + s), (float) (hz + s)).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
            dot.addVertex(m, (float) hx, (float) (hy - s), (float) (hz + s)).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(1, 0, 0);
        } else if (ay == 1) {
            dot.addVertex(m, (float) (hx - s), (float) hy, (float) (hz - s)).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
            dot.addVertex(m, (float) (hx + s), (float) hy, (float) (hz - s)).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
            dot.addVertex(m, (float) (hx + s), (float) hy, (float) (hz + s)).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
            dot.addVertex(m, (float) (hx - s), (float) hy, (float) (hz + s)).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 1, 0);
        } else {
            dot.addVertex(m, (float) (hx - s), (float) (hy - s), (float) hz).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            dot.addVertex(m, (float) (hx + s), (float) (hy - s), (float) hz).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            dot.addVertex(m, (float) (hx + s), (float) (hy + s), (float) hz).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
            dot.addVertex(m, (float) (hx - s), (float) (hy + s), (float) hz).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        }
    }

    private static void drawNeedle(VertexConsumer consumer, PoseStack poseStack, int overlay) {
        Matrix4f m = poseStack.last().pose();
        drawQuad(consumer, m, -0.04F, 0, -0.3F, 0.04F, 0, -0.3F, 0.04F, 0, 0.3F, -0.04F, 0, 0.3F,
                0xFFFF3333, LightTexture.FULL_BRIGHT, overlay);
    }

    private static void drawLamp(VertexConsumer consumer, PoseStack poseStack, int color,
                                  Direction.Axis axis, int overlay) {
        float cx, cy, cz;
        if (axis == Direction.Axis.Y) { cx = 0.7F; cy = 0.92F; cz = 0.7F; }
        else if (axis == Direction.Axis.X) { cx = 0.92F; cy = 0.7F; cz = 0.7F; }
        else { cx = 0.7F; cy = 0.7F; cz = 0.92F; }
        Matrix4f m = poseStack.last().pose();
        float hs = 0.08F;
        drawQuad(consumer, m, cx - hs, cy, cz - hs, cx + hs, cy, cz - hs, cx + hs, cy, cz + hs, cx - hs, cy, cz + hs,
                color, LightTexture.FULL_BRIGHT, overlay);
    }

    private static void drawQuad(VertexConsumer consumer, Matrix4f m,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float x3, float y3, float z3, float x4, float y4, float z4,
                                 int argb, int light, int overlay) {
        int a = argb >>> 24 & 0xFF;
        int r = argb >>> 16 & 0xFF;
        int g = argb >>> 8 & 0xFF;
        int b = argb & 0xFF;
        consumer.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(m, x4, y4, z4).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }
}

