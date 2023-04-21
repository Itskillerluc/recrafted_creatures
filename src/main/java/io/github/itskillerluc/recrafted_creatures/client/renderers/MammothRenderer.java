package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.duclib.client.render.AnimatableDucRenderer;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.models.MammothModel;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MammothRenderer extends MobRenderer<Mammoth, MammothModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/mammoth.png");
    public static final ResourceLocation BABY_LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/baby_mammoth.png");
    public static final ResourceLocation MARKINGS_LAYER = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/markings.png");
    public static final ResourceLocation BABY_MARKINGS_LAYER = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/baby_markings.png");

    public MammothRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new MammothModel((Ducling) pContext.bakeLayer(MammothModel.LAYER_LOCATION)), 1.5f);
        this.addLayer(new MarkingsLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Mammoth pEntity) {
        return pEntity.isBaby() ? BABY_LOCATION : LOCATION;
    }
}
