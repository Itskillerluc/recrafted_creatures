package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

public class RedPandaRenderer extends MobRenderer<RedPanda, RedPandaModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/red_panda.png");
    public static final ResourceLocation LOCATION_SLEEPING = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/red_panda_sleeping.png");

    public RedPandaRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new RedPandaModel((Ducling) pContext.bakeLayer(RedPandaModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    protected void setupRotations(RedPanda pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        if (this.isShaking(pEntityLiving)) {
            pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
        }

        pMatrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - pRotationYaw));


        if (pEntityLiving.deathTime > 0) {
            float f = ((float)pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            pMatrixStack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees(pEntityLiving)));
        } else if (pEntityLiving.isAutoSpinAttack()) {
            pMatrixStack.mulPose(Axis.XP.rotationDegrees(-90.0F - pEntityLiving.getXRot()));
            pMatrixStack.mulPose(Axis.YP.rotationDegrees(((float)pEntityLiving.tickCount + pPartialTicks) * -75.0F));
        } else if (isEntityUpsideDown(pEntityLiving)) {
            pMatrixStack.translate(0.0F, pEntityLiving.getBbHeight() + 0.1F, 0.0F);
            pMatrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RedPanda pEntity) {
        return pEntity.isRedPandaSleeping() ? LOCATION_SLEEPING : LOCATION;
    }
}
