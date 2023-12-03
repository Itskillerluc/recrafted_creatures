package io.github.itskillerluc.recrafted_creatures.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.ChameleonModel;
import io.github.itskillerluc.recrafted_creatures.client.models.MarmotModel;
import io.github.itskillerluc.recrafted_creatures.entity.Chameleon;
import io.github.itskillerluc.recrafted_creatures.entity.Marmot;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ChameleonRenderer extends MobRenderer<Chameleon, ChameleonModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/chameleon_accents.png");

    public ChameleonRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ChameleonModel((Ducling) pContext.bakeLayer(ChameleonModel.LAYER_LOCATION)), 0.5f);
        addLayer(new ChameleonLayer<>(this));
    }

    @Override
    protected void setupRotations(Chameleon pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        if (pEntityLiving.isClimbing()) {
            BlockPos north = pEntityLiving.blockPosition().north();
            BlockPos east = pEntityLiving.blockPosition().east();
            BlockPos south = pEntityLiving.blockPosition().south();
            BlockPos west = pEntityLiving.blockPosition().west();

            Level level = pEntityLiving.level();
            if (level.getBlockState(north).isSolidRender(level, north)) {
                pMatrixStack.mulPose(Direction.SOUTH.getRotation());
                pMatrixStack.translate(0, -0.5, 0);
            } else if (level.getBlockState(east).isSolidRender(level, east)) {
                pMatrixStack.mulPose(Direction.WEST.getRotation());
                pMatrixStack.translate(0, -0.5, 0);
            } else if (level.getBlockState(south).isSolidRender(level, south)) {
                pMatrixStack.mulPose(Direction.NORTH.getRotation());
                pMatrixStack.translate(0, -0.5, 0);
            } else if (level.getBlockState(west).isSolidRender(level, west)) {
                pMatrixStack.mulPose(Direction.EAST.getRotation());
                pMatrixStack.translate(0, -0.5, 0);
            }
        }
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Chameleon pEntity) {
        return LOCATION;
    }
}
