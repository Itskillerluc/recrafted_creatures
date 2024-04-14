package io.github.itskillerluc.recrafted_creatures.client;

import io.github.itskillerluc.duclib.client.model.BaseDucModel;
import io.github.itskillerluc.duclib.client.model.Ducling;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.*;
import io.github.itskillerluc.recrafted_creatures.entity.*;
import io.github.itskillerluc.recrafted_creatures.item.JungleStaff;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.client.renderers.*;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RecraftedCreatures.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(EntityRegistry.GIRAFFE.get(), GiraffeRenderer::new);
        EntityRenderers.register(EntityRegistry.RED_PANDA.get(), RedPandaRenderer::new);
        EntityRenderers.register(EntityRegistry.ZEBRA.get(), ZebraRenderer::new);
        EntityRenderers.register(EntityRegistry.MAMMOTH.get(), MammothRenderer::new);
        EntityRenderers.register(EntityRegistry.MARMOT.get(), MarmotRenderer::new);
        EntityRenderers.register(EntityRegistry.CHAMELEON.get(), ChameleonRenderer::new);
        EntityRenderers.register(EntityRegistry.OWL.get(), OwlRenderer::new);
        EntityRenderers.register(EntityRegistry.SECRETARYBIRD.get(), SecretarybirdRenderer::new);
        EntityRenderers.register(EntityRegistry.ORANGUTAN.get(), OrangutanRenderer::new);
        EntityRenderers.register(EntityRegistry.BEAVER.get(), BeaverRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(GiraffeModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Giraffe.LOCATION));
        event.registerLayerDefinition(RedPandaModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(RedPanda.LOCATION));
        event.registerLayerDefinition(MammothModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Mammoth.LOCATION));
        event.registerLayerDefinition(MarmotModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Marmot.LOCATION));
        event.registerLayerDefinition(ChameleonModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Chameleon.LOCATION));
        event.registerLayerDefinition(OwlModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Owl.LOCATION));
        event.registerLayerDefinition(ZebraModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Zebra.LOCATION));
        event.registerLayerDefinition(SecretarybirdModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Secretarybird.LOCATION));
        event.registerLayerDefinition(OrangutanModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Orangutan.LOCATION));
        event.registerLayerDefinition(JungleStaffModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(JungleStaff.LOCATION));
        event.registerLayerDefinition(BeaverModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(Beaver.LOCATION));
        event.registerLayerDefinition(BuilderHatModel.LAYER_LOCATION, () -> BaseDucModel.getLakeDefinition(BuilderHatModel.LOCATION));
    }

    @SubscribeEvent
    public static void addLayers(final EntityRenderersEvent.AddLayers event) {
        ItemRenderer.INSTANCE.models.put(JungleStaff.LOCATION, new JungleStaffModel(((Ducling) event.getContext().bakeLayer(JungleStaffModel.LAYER_LOCATION))));
        ItemRegistry.builderHatModel = new BuilderHatModel(((Ducling) event.getContext().bakeLayer(BuilderHatModel.LAYER_LOCATION)));
    }
}
