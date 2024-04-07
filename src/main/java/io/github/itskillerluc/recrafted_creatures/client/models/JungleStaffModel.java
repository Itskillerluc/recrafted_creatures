package io.github.itskillerluc.recrafted_creatures.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.duclib.client.model.BaseDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class JungleStaffModel extends BaseDucModel implements IAnimatableModel{
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "jungle_staff"), "main");

    public Ducling root;
    public JungleStaffModel(Ducling ducling) {
        super(RenderType::entityCutout);
        this.root = ducling;
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public Vector3f getVectorCache() {
        return ANIMATION_VECTOR_CACHE;
    }
}
