package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.models.OwlModel;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class OwlRenderer extends MobRenderer<Owl, OwlModel> {
    public OwlRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new OwlModel((Ducling) pContext.bakeLayer(OwlModel.LAYER_LOCATION)), 0.5f);
        addLayer(new OwlHoldsItemLayer(this, pContext.getItemInHandRenderer()));
    }
    @Override
    public @NotNull ResourceLocation getTextureLocation(Owl pEntity) {
        return pEntity.getVariant().getTexture(pEntity);
    }
}
