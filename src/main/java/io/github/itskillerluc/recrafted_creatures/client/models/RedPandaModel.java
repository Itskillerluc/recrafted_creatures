package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.animation.AnimationHolder;
import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;

public class RedPandaModel extends AnimatableDucModel<RedPanda> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "red_panda.png"), "all");

    public RedPandaModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }


    @Override
    public @NotNull Ducling root() {
        return super.root().getChild("root");
    }

    @Override
    public void setupAnim(@NotNull RedPanda pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (this.young){
            if (pEntity.hasPose(Pose.SITTING)) {
              this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
              this.root().offsetPos(new Vector3f(0, 6f, -2));
            } else if (!pEntity.isRedPandaSleeping()) {
                this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
                this.root().offsetPos(new Vector3f(0, 2f, 0));
            } else if (pEntity.isRedPandaSleeping()){
                this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
                this.root().offsetPos(new Vector3f(0, 6f, 0));
            }
        }

        if (!pEntity.hasPose(Pose.SLEEPING)) {
            ((Ducling) getAnyDescendantWithName("tail").orElseThrow()).yRot = Mth.sin(pAgeInTicks * (this.young ? 0.2f : 0.03f) * Mth.PI) * 0.15f;
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).xRot = pHeadPitch * ((float) Math.PI / 180F) + ((Ducling) getAnyDescendantWithName("fake_head").orElseThrow()).xRot;
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).x = ((Ducling) getAnyDescendantWithName("fake_head").orElseThrow()).x;
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).y = ((Ducling) getAnyDescendantWithName("fake_head").orElseThrow()).y;
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).z = ((Ducling) getAnyDescendantWithName("fake_head").orElseThrow()).z;
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        }

        if (pEntity.hasPose(Pose.SITTING)){
            return;
        }
        if (!pEntity.isRedPandaSleeping() && !pEntity.getAnimationState("stand").get().isStarted()) {
            ((Ducling) getAnyDescendantWithName("leg1").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg3").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg0").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg2").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
        }
    }
}
