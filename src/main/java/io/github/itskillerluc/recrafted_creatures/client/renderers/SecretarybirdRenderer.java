package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.MammothModel;
import io.github.itskillerluc.recrafted_creatures.client.models.SecretarybirdModel;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import io.github.itskillerluc.recrafted_creatures.entity.Secretarybird;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SecretarybirdRenderer extends MobRenderer<Secretarybird, SecretarybirdModel> {
    public SecretarybirdRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SecretarybirdModel((Ducling) pContext.bakeLayer(SecretarybirdModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new SecretaryPotionLayer<>(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(Secretarybird pEntity) {
        return pEntity.getVariant().texture;
    }
}
