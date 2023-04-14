package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class MarkingsLayer<T extends Mammoth,M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final RenderType MARKINGS = RenderType.entityCutoutNoCull(new ResourceLocation("textures/entity/spider_eyes.png"));

    public MarkingsLayer(RenderLayerParent<T, M> p_117507_) {
        super(p_117507_);
    }

    public RenderType renderType() {
        return MARKINGS;
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        ResourceLocation resourcelocation = MammothRenderer.MARKINGS_LAYER;
        if (!pLivingEntity.isInvisible() && pLivingEntity.isTame()) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucent(resourcelocation));
            this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), ((float) (0.00392156862D * Byte.toUnsignedInt(pLivingEntity.color[0]))), ((float) (0.00392156862D * Byte.toUnsignedInt(pLivingEntity.color[1]))), ((float) (0.00392156862D * Byte.toUnsignedInt(pLivingEntity.color[2]))), 1.0F);
        }
    }
}
