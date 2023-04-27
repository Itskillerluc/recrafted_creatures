package io.github.itskillerluc.recrafted_creatures.client.models;

import com.mojang.math.Vector3f;
import io.github.itskillerluc.duclib.client.animation.AnimationHolder;
import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RedPandaModel extends AnimatableDucModel<RedPanda> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "red_panda.png"), "all");

    public RedPandaModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    public void setupAnim(@NotNull RedPanda pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (this.young){
            if (pEntity.getPose() != Pose.SLEEPING) {
                this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
                this.root().offsetPos(new Vector3f(0, 7f, 0));
            } else if (pEntity.getPose() == Pose.SLEEPING){
                this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
                this.root().offsetPos(new Vector3f(0, 0f, 6));
            }
        }
        for (Map.Entry<String, AnimationState> stringAnimationStateEntry : pEntity.getAnimations().get().entrySet()) {
            if (!excludeAnimations().contains(stringAnimationStateEntry.getKey())) {
                AnimationHolder animation = pEntity.getAnimation().getAnimations().get(stringAnimationStateEntry.getKey());
                this.animate(stringAnimationStateEntry.getValue(), animation.animation(), pAgeInTicks, animation.speed());
            }
        }//TODO
        /*if (pEntity.getPose() != Pose.SLEEPING){
            ((Ducling) getAnyDescendantWithName("tail").orElseThrow()).yRot = Mth.sin(pAgeInTicks * (this.young ? 0.2f : 0.03f) * Mth.PI) * 0.15f;
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).xRot = pHeadPitch * ((float) Math.PI / 180F) + (pEntity.hasPose(Pose.CROUCHING) ? 0.610865f : 0);
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        }
        if (pEntity.hasPose(Pose.CROUCHING)){
            return;
        }

        if (pEntity.getPose() != Pose.SLEEPING) {

            ((Ducling) getAnyDescendantWithName("leg1").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg3").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg0").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg2").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
        }*/
    }
}
