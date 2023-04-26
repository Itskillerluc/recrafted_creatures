package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;

public class RedPandaRenderer extends MobRenderer<RedPanda, RedPandaModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/red_panda.png");
    public static final ResourceLocation LOCATION_SLEEPING = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/red_panda_sleeping.png");

    public RedPandaRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new RedPandaModel((Ducling) pContext.bakeLayer(RedPandaModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(RedPanda pEntity) {
        return pEntity.getPose() == Pose.SLEEPING ? LOCATION_SLEEPING : LOCATION;
    }
}
