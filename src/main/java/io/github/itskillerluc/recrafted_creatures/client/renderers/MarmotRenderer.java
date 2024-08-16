package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.models.MarmotModel;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import io.github.itskillerluc.recrafted_creatures.entity.Marmot;
import io.github.itskillerluc.recrafted_creatures.entity.Secretarybird;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MarmotRenderer extends MobRenderer<Marmot, MarmotModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/marmot.png");
    public static final ResourceLocation MARMOTTUS = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/marmottus.png");

    public MarmotRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new MarmotModel((Ducling) pContext.bakeLayer(MarmotModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new MarmotHoldsItemLayer(this, pContext.getItemInHandRenderer()));
    }
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Marmot pEntity) {
        var name = pEntity.getCustomName();
        if (name != null && name.getString().equalsIgnoreCase("marmottus")) {
            return MARMOTTUS;
        }
        return LOCATION;
    }
}
