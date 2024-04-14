package io.github.itskillerluc.recrafted_creatures.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.duclib.client.model.BaseDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.joml.Quaternionf;

public class BuilderHatModel extends BaseDucModel{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "builder_hat"), "main");
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "builder_hat");

    public Ducling root;
    public Vec2 headRotation;
    public BuilderHatModel(Ducling ducling) {
        super(RenderType::entityCutout);
        this.root = ducling;
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        pPoseStack.pushPose();
        pPoseStack.mulPose(new Quaternionf().rotateY(headRotation.y).rotateX(headRotation.x));
        pPoseStack.translate(0, -1.8, 0);
        root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        pPoseStack.popPose();
    }
}
