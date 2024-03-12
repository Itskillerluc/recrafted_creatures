package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import io.github.itskillerluc.recrafted_creatures.entity.Secretarybird;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SecretaryPotionLayer<M extends EntityModel<Secretarybird>> extends RenderLayer<Secretarybird, M> {
    public static final ResourceLocation MARKINGS_LAYER = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/secretary_potion.png");

    public SecretaryPotionLayer(RenderLayerParent<Secretarybird, M> p_117507_) {
        super(p_117507_);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, Secretarybird pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        Vec3 effectRGB = pLivingEntity.getEffectRGB(true);
        if (!pLivingEntity.isInvisible() && effectRGB != null) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucent(MARKINGS_LAYER));
            this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), (float) effectRGB.x(), (float) effectRGB.y(), (float) effectRGB.z(), 1.0F);
        }
    }
}
