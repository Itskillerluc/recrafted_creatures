package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.ZebraModel;
import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ZebraRenderer extends MobRenderer<Zebra, ZebraModel> {
    public ZebraRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ZebraModel(((Ducling) pContext.bakeLayer(ZebraModel.LAYER_LOCATION))), 1.1F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Zebra pEntity) {
        return pEntity.getVariant().getTexture(pEntity.isSaddled(), pEntity.hasChest());
    }
}
