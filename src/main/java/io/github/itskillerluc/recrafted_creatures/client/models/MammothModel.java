package io.github.itskillerluc.recrafted_creatures.client.models;

import com.mojang.math.Vector3f;
import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

public class MammothModel extends AnimatableDucModel<Mammoth> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "mammoth.png"), "all");

    public MammothModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }

    @Override
    public void setupAnim(@NotNull Mammoth pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        if (this.young){
            root().offsetScale(new Vector3f(-0.35f, -0.35f, -0.35f));
            root().offsetPos(new Vector3f(0f, 7f, 0f));
        }
        ((Ducling) getAnyDescendantWithName("Head").orElseThrow()).xRot = pHeadPitch * ((float) Math.PI / 180F) + (pEntity.hasPose(Pose.CROUCHING) ? 0.610865f : 0);
        ((Ducling) getAnyDescendantWithName("Head").orElseThrow()).yRot = pNetHeadYaw * ((float) Math.PI / 180F);

        ((Ducling) getAnyDescendantWithName("leg1").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F * 0.35f) * 1.4F * pLimbSwingAmount;
        ((Ducling) getAnyDescendantWithName("leg3").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F * 0.35f + (float)Math.PI) * 1.4F * pLimbSwingAmount;
        ((Ducling) getAnyDescendantWithName("leg0").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F * 0.35f + (float)Math.PI) * 1.4F * pLimbSwingAmount;
        ((Ducling) getAnyDescendantWithName("leg2").orElseThrow()).xRot = Mth.cos(pLimbSwing * 0.6662F * 0.35f) * 1.4F * pLimbSwingAmount;
    }
}
