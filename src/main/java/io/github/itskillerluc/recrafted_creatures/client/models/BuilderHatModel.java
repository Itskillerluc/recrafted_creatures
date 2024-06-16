package io.github.itskillerluc.recrafted_creatures.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class BuilderHatModel extends SkullModelBase {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "builder_hat"), "main");
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "builder_hat");

    Ducling root;
    public BuilderHatModel(Ducling pRoot) {
        super();
        this.root = pRoot;
    }

    @Override
    public void setupAnim(float pMouthAnimation, float pYRot, float pXRot) {
        this.root.yRot = pYRot * ((float)Math.PI / 180F);
        this.root.xRot = pXRot * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        pPoseStack.pushPose();
        pPoseStack.translate(0, -0.2, 0);
        pPoseStack.scale(0.8f, 0.8f, 0.8f);
        this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        pPoseStack.popPose();
    }
}
