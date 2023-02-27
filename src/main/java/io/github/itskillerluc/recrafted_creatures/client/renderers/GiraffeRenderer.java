package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.duclib.client.render.AnimatableDucRenderer;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GiraffeRenderer extends AnimatableDucRenderer<Giraffe, GiraffeModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/giraffe.png");

    public GiraffeRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, () -> new GiraffeModel((Ducling) pContext.bakeLayer(GiraffeModel.LAYER_LOCATION)), entity -> LOCATION, 2);
    }
}
