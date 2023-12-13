package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class OwlModel extends AnimatableDucModel<Owl> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "owl.png"), "all");

    public OwlModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Set<String> excludeAnimations() {
        return Set.of("animation.owl.fly", "animation.owl.walk");
    }

    @Override
    public void setupAnim(@NotNull Owl pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (pEntity.isFlying()) {
            this.animate(pEntity.getAnimations().get().get("animation.owl.fly"), pEntity.getAnimation().getAnimations().get("animation.owl.fly").animation(), pAgeInTicks, 1);
        } else {
            this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.owl.walk").animation(), pLimbSwing, pLimbSwingAmount, 2, 1);
        }
    }
}
