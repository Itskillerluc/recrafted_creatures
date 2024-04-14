package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itskillerluc.recrafted_creatures.client.models.BeaverModel;
import io.github.itskillerluc.recrafted_creatures.client.models.OrangutanModel;
import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import io.github.itskillerluc.recrafted_creatures.entity.Orangutan;
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

public class BeaverHoldsItemLayer extends RenderLayer<Beaver, BeaverModel> {
    private final ItemInHandRenderer itemInHandRenderer;

    public BeaverHoldsItemLayer(RenderLayerParent<Beaver, BeaverModel> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, Beaver pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
        float f1 = -1.2f;

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.PI, new Vector3f(1, 0, 0)));
        poseStack.translate(0, f1 - (pLivingEntity.isBaby() ? 0.2 : 0),  pLivingEntity.isBaby() ? 0.3 : .5f);
        this.itemInHandRenderer.renderItem(pLivingEntity, itemstack, ItemDisplayContext.GROUND, false, poseStack, pBuffer, pPackedLight);
        poseStack.popPose();
    }
}
