package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.duclib.client.render.AnimatableDucRenderer;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GiraffeRenderer extends MobRenderer<Giraffe, GiraffeModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/giraffe.png");
    public static final ResourceLocation LOCATION_SADDLED = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/giraffe_saddled.png");

    public GiraffeRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new GiraffeModel((Ducling) pContext.bakeLayer(GiraffeModel.LAYER_LOCATION)), 1.5f);
    }
    @Override
    public ResourceLocation getTextureLocation(Giraffe pEntity) {
        return pEntity.isSaddled() ? LOCATION_SADDLED : LOCATION;
    }
}
