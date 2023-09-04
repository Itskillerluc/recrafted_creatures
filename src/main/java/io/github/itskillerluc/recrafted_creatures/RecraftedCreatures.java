package io.github.itskillerluc.recrafted_creatures;

import io.github.itskillerluc.recrafted_creatures.block.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import io.github.itskillerluc.recrafted_creatures.entity.Mammoth;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import io.github.itskillerluc.recrafted_creatures.registries.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
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
        modEventBus.addListener(this::registerSpawnPlacement);
        modEventBus.addListener(this::postInit);

        BannerPatternRegistry.BANNER_PATTERNS.register(modEventBus);
        SoundRegistry.SOUNDS.register(modEventBus);
        CreativeModeTabRegistry.CREATIVEMODE_TAB_REGISTRY.register(modEventBus);
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        PaintingRegistry.PAINTINGS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    public void registerSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(EntityRegistry.RED_PANDA.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityRegistry.GIRAFFE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityRegistry.ZEBRA.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityRegistry.MAMMOTH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityType.CAMEL, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
    private void addEntityAttributes(EntityAttributeCreationEvent event){
        event.put(EntityRegistry.GIRAFFE.get(), Giraffe.attributes().build());
        event.put(EntityRegistry.RED_PANDA.get(), RedPanda.attributes().build());
        event.put(EntityRegistry.ZEBRA.get(), Horse.createBaseHorseAttributes().build());
        event.put(EntityRegistry.MAMMOTH.get(), Mammoth.attributes().build());
    }

    private void postInit(FMLLoadCompleteEvent event) {
        DispenserBlock.registerBehavior(ItemRegistry.FROG_BUCKET.get(), new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            /**
             * Dispense the specified stack, play the dispense sound, and spawn particles.
             */
            public ItemStack execute(BlockSource p_123561_, ItemStack p_123562_) {
                DispensibleContainerItem dispensiblecontaineritem = (DispensibleContainerItem)p_123562_.getItem();
                BlockPos blockpos = p_123561_.getPos().relative(p_123561_.getBlockState().getValue(DispenserBlock.FACING));
                Level level = p_123561_.getLevel();
                if (dispensiblecontaineritem.emptyContents((Player)null, level, blockpos, (BlockHitResult)null, p_123562_)) {
                    dispensiblecontaineritem.checkExtraContent((Player)null, level, p_123562_, blockpos);
                    return new ItemStack(Items.BUCKET);
                } else {
                    return this.defaultDispenseItemBehavior.dispense(p_123561_, p_123562_);
                }
            }
        });
    }
}
