package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class RedPandaModel extends AnimatableDucModel<RedPanda> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "red_panda.png"), "all");

    public RedPandaModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    public void setupAnim(@NotNull RedPanda pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (this.young){
            if (pEntity.hasPose(Pose.SITTING)) {
              this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
              this.root().offsetPos(new Vector3f(0, 9f, 6));
            } else if (!pEntity.getEntityData().get(RedPanda.SLEEPING)) {
                this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
                this.root().offsetPos(new Vector3f(0, 7f, 0));
            } else if (pEntity.getEntityData().get(RedPanda.SLEEPING)){
                this.root().offsetScale(new Vector3f(-0.3f, -0.3f, -0.3f));
                this.root().offsetPos(new Vector3f(0, 6f, 0));
            }
        }
        if (!pEntity.getEntityData().get(RedPanda.SLEEPING)){
            ((Ducling) getAnyDescendantWithName("tail").orElseThrow()).yRot = Mth.sin(pAgeInTicks * (this.young ? 0.2f : 0.03f) * Mth.PI) * 0.15f;
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).xRot = pHeadPitch * ((float) Math.PI / 180F) + (pEntity.hasPose(Pose.SITTING) ? 0.610865f : 0);
            ((Ducling) getAnyDescendantWithName("head").orElseThrow()).yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        }
        if (pEntity.hasPose(Pose.SITTING) && !this.young){
            this.root().offsetPos(new Vector3f(0, 5, 10));
            return;
        }

        if (!pEntity.getEntityData().get(RedPanda.SLEEPING)) {
            ((Ducling) getAnyDescendantWithName("leg1").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg3").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg0").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
            ((Ducling) getAnyDescendantWithName("leg2").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
        }
    }
}
