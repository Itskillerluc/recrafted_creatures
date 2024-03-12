package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Secretarybird;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SecretarybirdModel extends AnimatableDucModel<Secretarybird> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "secretary"), "main");

    public SecretarybirdModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Set<String> excludeAnimations() {
        return Set.of("animation.secretary_bird.walk");
    }

    @Override
    public void setupAnim(@NotNull Secretarybird pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.secretary_bird.walk").animation(), pLimbSwing, pLimbSwingAmount, 4, 10);
        ((Ducling) getAnyDescendantWithName("Head").orElseThrow()).xRot += pHeadPitch * ((float) Math.PI / 180F) + (pEntity.hasPose(Pose.SITTING) ? 0.610865f : 0);
        ((Ducling) getAnyDescendantWithName("Head").orElseThrow()).yRot += pNetHeadYaw * ((float) Math.PI / 180F);
    }
}
