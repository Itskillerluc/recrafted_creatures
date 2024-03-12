package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Set;

public class ZebraModel extends AnimatableDucModel<Zebra> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "zebra"), "main");

    public ZebraModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    protected Set<String> excludeAnimations() {
        return Set.of("animation.zebra.walk", "animation.zebra.gallop");
    }

    @Override
    public void setupAnim(@NotNull Zebra pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (pEntity.hasControllingPassenger()) {
            this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.zebra.gallop").animation(), pLimbSwing, pLimbSwingAmount, 1, 2f);
        } else {
            this.animateWalk(pEntity.getAnimation().getAnimations().get("animation.zebra.walk").animation(), pLimbSwing, pLimbSwingAmount, 5    , 2);
        }
        if (this.young){
            root().offsetScale(new Vector3f(-0.35f, -0.35f, -0.35f));
            root().offsetPos(new Vector3f(0f, 7f, 0f));
        }
        ((Ducling) getAnyDescendantWithName("Neck").orElseThrow()).xRot += pHeadPitch * ((float)Math.PI / 180F);
        ((Ducling) getAnyDescendantWithName("Neck").orElseThrow()).yRot += pNetHeadYaw * ((float)Math.PI / 180F);
    }
}
