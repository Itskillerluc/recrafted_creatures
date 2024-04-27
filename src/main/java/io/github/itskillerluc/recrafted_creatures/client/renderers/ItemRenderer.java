package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.recrafted_creatures.client.models.JungleStaffModel;
import io.github.itskillerluc.recrafted_creatures.item.JungleStaff;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import io.github.itskillerluc.recrafted_creatures.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

public class ItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ItemRenderer INSTANCE = new ItemRenderer();
    public final Map<ResourceLocation, Model> models = new HashMap<>();
    public ItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (pStack.is(ItemRegistry.JUNGLE_STAFF.get())) {
            JungleStaffModel model = (JungleStaffModel) models.get(JungleStaff.LOCATION);
            VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(JungleStaff.TEXTURE));
            pPoseStack.pushPose();
            switch (pDisplayContext) {
                case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                    pPoseStack.mulPose(new Quaternionf().rotateX(Mth.PI));
                    pPoseStack.translate(0.5, -2, -0.6);
                }
                case FIRST_PERSON_LEFT_HAND -> {
                    pPoseStack.mulPose(new Quaternionf().rotateX(Mth.PI));
                    pPoseStack.translate(0, -1.3, 0);
                }
                case FIRST_PERSON_RIGHT_HAND -> {
                    pPoseStack.mulPose(new Quaternionf().rotateX(Mth.PI));
                    pPoseStack.translate(1, -1.3, 0);
                }
                case HEAD -> {
                }
                case GUI -> {
                    pPoseStack.mulPose(new Quaternionf().rotateZ(Mth.PI * 0.75f));
                    pPoseStack.translate(0, -1.1, -0.6);
                    pPoseStack.scale(0.5f, 0.5f, 0.5f);
                }
                case GROUND -> {
                    pPoseStack.mulPose(new Quaternionf().rotateZ(Mth.PI * 0.5f));
                    pPoseStack.scale(0.5f, 0.5f, 0.5f);
                    pPoseStack.translate(1, -2, 1);
                }
                case FIXED -> {
                    pPoseStack.mulPose(new Quaternionf().rotateZ(Mth.PI * 0.75f));
                    pPoseStack.translate(0, -1.1, 0.5);
                    pPoseStack.scale(0.5f, 0.5f, 0.5f);
                }
            }
            model.root.getAllParts().forEach(ModelPart::resetPose);
            if (Minecraft.getInstance().level != null) {
                if (pStack.getOrCreateTag().contains("use")) {
                    Util.animate(model, JungleStaff.ANIMATION.getAnimations().get("animation.jungle_staff.use").animation(), Minecraft.getInstance().level.getGameTime() * 10, 1, model.getVectorCache());
                } else {
                    Util.animate(model, JungleStaff.ANIMATION.getAnimations().get("animation.jungle_staff.idle").animation(), Minecraft.getInstance().level.getGameTime() * 100, 1, model.getVectorCache());
                }
            }
            model.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            pPoseStack.popPose();
        }
    }
}
