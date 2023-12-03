package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itskillerluc.recrafted_creatures.client.models.MarmotModel;
import io.github.itskillerluc.recrafted_creatures.entity.Marmot;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MarmotHoldsItemLayer extends RenderLayer<Marmot, MarmotModel> {
    private final ItemInHandRenderer itemInHandRenderer;

    public MarmotHoldsItemLayer(RenderLayerParent<Marmot, MarmotModel> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, Marmot pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
        float f1 = -1.5f;
        if (pLivingEntity.isEating()) {
            f1 = Mth.sin(pLivingEntity.getEating() * 0.7f + Mth.PI) * 0.07f -1.3f;
        }

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.PI, new Vector3f(1, 0, 0)));
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.translate(0, f1, 0.5);
        this.itemInHandRenderer.renderItem(pLivingEntity, itemstack, ItemDisplayContext.GROUND, false, poseStack, pBuffer, pPackedLight);
        poseStack.popPose();
    }
}
