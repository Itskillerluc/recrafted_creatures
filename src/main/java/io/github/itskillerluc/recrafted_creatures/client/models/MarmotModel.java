package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Marmot;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;

public class MarmotModel extends AnimatableDucModel<Marmot> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "marmot"), "main");

    public MarmotModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Set<String> excludeAnimations() {
        return Set.of("animation.marmot.walk");
    }

    @Override
    public void setupAnim(@NotNull Marmot pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.marmot.walk").animation(), pLimbSwing, pLimbSwingAmount, 5f, 2f);

        if (this.young){
            root().offsetScale(new Vector3f(-0.35f, -0.35f, -0.35f));
            root().offsetPos(new Vector3f(0f, 8.5f, 0f));
        }
        ((Ducling) getAnyDescendantWithName("head").orElseThrow()).xRot -= pHeadPitch * ((float) Math.PI / 180F);
        ((Ducling) getAnyDescendantWithName("head").orElseThrow()).yRot += pNetHeadYaw * ((float) Math.PI / 180F);
    }
}
