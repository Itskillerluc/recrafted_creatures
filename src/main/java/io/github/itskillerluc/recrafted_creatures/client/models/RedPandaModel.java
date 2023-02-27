package io.github.itskillerluc.recrafted_creatures.client.models;

import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RedPandaModel extends AnimatableDucModel<RedPanda> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RecraftedCreatures.MODID, "red_panda.png"), "all");

    public RedPandaModel(Ducling ducling) {
        super(ducling, RenderType::entityCutoutNoCull);
    }
}
