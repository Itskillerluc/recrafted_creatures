package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itskillerluc.recrafted_creatures.client.models.MarmotModel;
import io.github.itskillerluc.recrafted_creatures.client.models.OwlModel;
import io.github.itskillerluc.recrafted_creatures.entity.Marmot;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OwlHoldsItemLayer extends RenderLayer<Owl, OwlModel> {
    private final ItemInHandRenderer itemInHandRenderer;

    public OwlHoldsItemLayer(RenderLayerParent<Owl, OwlModel> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, Owl pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (!pLivingEntity.canDeliver()) return;
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.PI, new Vector3f(1, 0, 0)));
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.translate(0, -2.05, 0.4);
        var tag = new CompoundTag();
        new ItemStack(Items.STONE).save(tag);
        this.itemInHandRenderer.renderItem(pLivingEntity, new ItemStack(ItemRegistry.OWL_ENVELOPE.get(), 1, tag), ItemDisplayContext.GROUND, false, poseStack, pBuffer, pPackedLight);
        poseStack.popPose();
    }
}
