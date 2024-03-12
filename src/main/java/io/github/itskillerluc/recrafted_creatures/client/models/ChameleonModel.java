package io.github.itskillerluc.recrafted_creatures.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Chameleon;
import io.github.itskillerluc.recrafted_creatures.entity.Marmot;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Set;

public class ChameleonModel extends AnimatableDucModel<Chameleon> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "chameleon"), "accents");

    public ChameleonModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Set<String> excludeAnimations() {
        return Set.of("animation.chameleon.walk");
    }

    @Override
    public void setupAnim(@NotNull Chameleon pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

        animateWalk(pEntity.getAnimation().getAnimations().get("animation.chameleon.walk").animation(), pLimbSwing, pLimbSwingAmount, 10, 2);

        if (this.young){
            root().offsetScale(new Vector3f(-0.35f, -0.35f, -0.35f));
            root().offsetPos(new Vector3f(0f, 7f, 0f));
        }

        ((Ducling) getAnyDescendantWithName("head").orElseThrow()).xRot -= pHeadPitch * ((float) Math.PI / 180F);
        ((Ducling) getAnyDescendantWithName("head").orElseThrow()).yRot += pNetHeadYaw * ((float) Math.PI / 180F);
    }
}
