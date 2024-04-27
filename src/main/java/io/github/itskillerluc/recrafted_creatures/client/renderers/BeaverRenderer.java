package io.github.itskillerluc.recrafted_creatures.client.renderers;

import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.client.models.BeaverModel;
import io.github.itskillerluc.recrafted_creatures.client.models.ZebraModel;
import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BeaverRenderer extends MobRenderer<Beaver, BeaverModel> {
    public BeaverRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new BeaverModel(((Ducling) pContext.bakeLayer(BeaverModel.LAYER_LOCATION))), 0.5F);
        addLayer(new BeaverHoldsItemLayer(this, pContext.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Beaver pEntity) {
        var name = pEntity.getCustomName();
        return pEntity.getEntityData().get(Beaver.VARIANT).getTexture(name != null && name.getString().equalsIgnoreCase("bob"));
    }
}
