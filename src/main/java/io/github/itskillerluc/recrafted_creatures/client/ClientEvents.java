package io.github.itskillerluc.recrafted_creatures.client;

import io.github.itskillerluc.duclib.client.model.BaseDucModel;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.client.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.client.renderers.GiraffeRenderer;
import io.github.itskillerluc.recrafted_creatures.client.renderers.RedPandaRenderer;
import io.github.itskillerluc.recrafted_creatures.client.renderers.ZebraArmorLayer;
import io.github.itskillerluc.recrafted_creatures.client.renderers.ZebraRenderer;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = RecraftedCreatures.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(EntityRegistry.GIRAFFE.get(), GiraffeRenderer::new);
        EntityRenderers.register(EntityRegistry.RED_PANDA.get(), RedPandaRenderer::new);
        EntityRenderers.register(EntityRegistry.ZEBRA.get(), ZebraRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(GiraffeModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Giraffe.LOCATION));
        event.registerLayerDefinition(RedPandaModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(RedPanda.LOCATION));
        event.registerLayerDefinition(ZebraRenderer.LAYER, () -> LayerDefinition.create(HorseModel.createBodyMesh(CubeDeformation.NONE), 64, 64));
        event.registerLayerDefinition(ZebraArmorLayer.LAYER, () -> LayerDefinition.create(HorseModel.createBodyMesh(new CubeDeformation(0.1F)), 64, 64));
    }
}
