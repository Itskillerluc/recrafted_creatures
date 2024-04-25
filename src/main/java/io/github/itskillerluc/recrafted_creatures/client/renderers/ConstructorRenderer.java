package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class ConstructorRenderer implements BlockEntityRenderer<ConstructorBlockEntity> {
    public ConstructorRenderer(BlockEntityRendererProvider.Context pContext) {
    }

    public void render(ConstructorBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockPos blockpos = pBlockEntity.getStructurePos();
        Vec3i vec3i = pBlockEntity.getStructureSize();
        if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1 && pBlockEntity.getShowBoundingBox()) {
            double d0 = blockpos.getX();
            double d1 = blockpos.getZ();
            double d5 = blockpos.getY();
            double d8 = d5 + (double) vec3i.getY();
            double d2 = vec3i.getX();
            double d3 = vec3i.getZ();

            double d4 = d2 < 0.0D ? d0 + 1.0D : d0;
            double d6 = d3 < 0.0D ? d1 + 1.0D : d1;
            double d7 = d4 + d2;
            double d9 = d6 + d3;
            if (pBlockEntity.getCorner() != null) {
                VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.lines());
                LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, d4, d5, d6, d7, d8, d9, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
            }
        }

    }

    @Override
    public boolean shouldRenderOffScreen(ConstructorBlockEntity pBlockEntity) {
        return true;
    }

    public int getViewDistance() {
        return 96;
    }
}
