package io.github.itskillerluc.recrafted_creatures;

import io.github.itskillerluc.recrafted_creatures.block.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import io.github.itskillerluc.recrafted_creatures.registries.CreativeModeTabRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RecraftedCreatures.MODID)
public class RecraftedCreatures
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "recrafted_creatures";
    public RecraftedCreatures()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::addEntityAttributes);

        SoundRegistry.SOUNDS.register(modEventBus);
        CreativeModeTabRegistry.CREATIVEMODE_TAB_REGISTRY.register(modEventBus);
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    private void addEntityAttributes(EntityAttributeCreationEvent event){
        event.put(EntityRegistry.GIRAFFE.get(), Giraffe.attributes().build());
        event.put(EntityRegistry.RED_PANDA.get(), RedPanda.attributes().build());
        event.put(EntityRegistry.ZEBRA.get(), Horse.createBaseHorseAttributes().build());
        event.put(EntityRegistry.MAMMOTH.get(), Mammoth.attributes().build());
    }
}
