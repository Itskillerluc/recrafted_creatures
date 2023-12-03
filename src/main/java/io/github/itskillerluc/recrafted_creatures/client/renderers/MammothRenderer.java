package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.MammothModel;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MammothRenderer extends MobRenderer<Mammoth, MammothModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/mammoth.png");
    public static final ResourceLocation BABY_LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/baby_mammoth.png");

    public MammothRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new MammothModel((Ducling) pContext.bakeLayer(MammothModel.LAYER_LOCATION)), 1.5f);
        this.addLayer(new MarkingsLayer<>(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(Mammoth pEntity) {
        return pEntity.isBaby() ? BABY_LOCATION : LOCATION;
    }
}
