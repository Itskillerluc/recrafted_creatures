package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.OrangutanModel;
import io.github.itskillerluc.recrafted_creatures.client.models.ZebraModel;
import io.github.itskillerluc.recrafted_creatures.entity.Orangutan;
import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

public class OrangutanRenderer extends MobRenderer<Orangutan, OrangutanModel> {
    private static final ResourceLocation MALE = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/orangutan_male.png");
    private static final ResourceLocation MALE_SLEEP = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/orangutan_male_sleep.png");
    private static final ResourceLocation BABY = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/orangutan_baby.png");
    private static final ResourceLocation BABY_SLEEP = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/orangutan_baby_sleep.png");
    public OrangutanRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new OrangutanModel(((Ducling) pContext.bakeLayer(OrangutanModel.LAYER_LOCATION))), 1.1F);
        addLayer(new OrangutanHoldsItemLayer(this, pContext.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Orangutan pEntity) {
        if (pEntity.isBaby()) {
            return pEntity.isSleeping() ? BABY_SLEEP : BABY;
        } else {
            return pEntity.isSleeping() ? MALE_SLEEP : MALE;
        }
    }
}
