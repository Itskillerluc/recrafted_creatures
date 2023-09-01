package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Set;

public class GiraffeModel extends AnimatableDucModel<Giraffe> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "giraffe.png"), "all");

    public GiraffeModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Set<String> excludeAnimations() {
        return Collections.emptySet();
    }

    @Override
    public void setupAnim(@NotNull Giraffe pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (pEntity.hasControllingPassenger()) {
            this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.giraffe.run").animation(), pLimbSwing, pLimbSwingAmount, 2, 2f);
        } else {
            this.animateWalk(GiraffeAnimations.GIRAFFE_WALK, pLimbSwing, pLimbSwingAmount, 2, 2f);
        }
        if (this.young){
            root().offsetScale(new Vector3f(-0.35f, -0.35f, -0.35f));
            root().offsetPos(new Vector3f(0f, 7f, 0f));
        }
        ((Ducling) getAnyDescendantWithName("head").orElseThrow()).xRot = pHeadPitch * ((float)Math.PI / 180F);
        ((Ducling) getAnyDescendantWithName("head").orElseThrow()).yRot = pNetHeadYaw * ((float)Math.PI / 180F);
    }
}
