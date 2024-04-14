package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.animation.AnimationHolder;
import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;

public class BeaverModel extends AnimatableDucModel<Beaver> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "beaver"), "main");

    public BeaverModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Set<String> excludeAnimations() {
        return Set.of("animation.beaver.walk_biped", "animation.beaver.walk_quadroped");
    }

    @Override
    public void setupAnim(@NotNull Beaver pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (pEntity.isSwimming()) {
            this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.beaver.walk_quadroped").animation(), pLimbSwing, pLimbSwingAmount, 1, 1);
        } else {
            this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.beaver.walk_biped").animation(), pLimbSwing, pLimbSwingAmount, 3, 2);
        }
        if (this.young){
            root().offsetScale(new Vector3f(-0.35f, -0.35f, -0.35f));
            root().offsetPos(new Vector3f(0f, 7f, 0f));
        }

        ((Ducling) getAnyDescendantWithName("Head").orElseThrow()).xRot += pHeadPitch * ((float)Math.PI / 180F);
        ((Ducling) getAnyDescendantWithName("Head").orElseThrow()).yRot += pNetHeadYaw * ((float)Math.PI / 180F);
    }
}
