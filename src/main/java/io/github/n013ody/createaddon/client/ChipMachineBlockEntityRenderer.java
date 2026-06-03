package io.github.n013ody.createaddon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.n013ody.createaddon.content.semiconductor.ChipMachineBlockEntity;
import io.github.n013ody.createaddon.registry.ModBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ChipMachineBlockEntityRenderer implements BlockEntityRenderer<ChipMachineBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ChipMachineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ChipMachineBlockEntity machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (machine.getLevel() == null)
            return;

        Block block = machine.getBlockState().getBlock();
        if (block == ModBlocks.WAFER_POLISHER.get()) {
            renderWaferPolisher(machine, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private void renderWaferPolisher(ChipMachineBlockEntity machine, float partialTick, PoseStack poseStack,
                                     MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = machine.getDisplayStack();
        if (stack.isEmpty())
            return;

        float progress = machine.getProcessingProgress(partialTick);
        float idleAngle = machine.getLevel().getGameTime() % 360;
        float activeAngle = (machine.getLevel().getGameTime() + partialTick) * 28.0F;
        float angle = machine.isProcessing() ? activeAngle : idleAngle + progress * 90.0F;
        float pressOffset = machine.isProcessing() ? (float) Math.sin((machine.getLevel().getGameTime() + partialTick) * 0.35F) * 0.025F : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.62D + pressOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.42F, 0.42F, 0.42F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, machine.getLevel(), 0);
        poseStack.popPose();
    }
}
