package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ZebraRenderer extends AbstractHorseRenderer<Zebra, HorseModel<Zebra>> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_pattern.png");
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "horse_armor"), "main");
    public ZebraRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new HorseModel<>(pContext.bakeLayer(ModelLayers.HORSE)), 1.1F);
        this.addLayer(new ZebraArmorLayer(this, pContext.getModelSet()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Zebra pEntity) {
        return LOCATION;
    }
}
