package com.example.createaddon.client;

import com.example.createaddon.content.guidance.frame.RotaryFrameBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.math.Axis;

public class RotaryFrameBlockEntityRenderer implements BlockEntityRenderer<RotaryFrameBlockEntity> {
    public RotaryFrameBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RotaryFrameBlockEntity frame, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!frame.hasFilledState())
            return;

        BlockState state = frame.getFilledState();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(frame.getYawDegrees()));
        poseStack.mulPose(Axis.XP.rotationDegrees(frame.getPitchDegrees()));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
