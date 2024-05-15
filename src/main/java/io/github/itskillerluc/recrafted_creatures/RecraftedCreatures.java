package io.github.itskillerluc.recrafted_creatures;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.itskillerluc.recrafted_creatures.advancement.BeaverBuildTrigger;
import io.github.itskillerluc.recrafted_creatures.advancement.OwlDeliveryTrigger;
import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.config.Configs;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.entity.*;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.registries.*;
import io.github.itskillerluc.recrafted_creatures.util.StreamUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        StreamUtils.setup(this);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::addEntityAttributes);
        modEventBus.addListener(this::registerSpawnPlacement);
        modEventBus.addListener(this::postInit);
        modEventBus.addListener(this::commonSetup);

        BannerPatternRegistry.BANNER_PATTERNS.register(modEventBus);
        SoundRegistry.SOUNDS.register(modEventBus);
        SensorRegistry.SENSORS.register(modEventBus);
        MemoryModuleRegistry.MEMORIES.register(modEventBus);
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        PaintingRegistry.PAINTINGS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        BlockEntityRegistry.BLOCK_ENTITY_TYPES.register(modEventBus);
        InstrumentRegistry.INSTRUMENTS.register(modEventBus);
        CreativeModeTabRegistry.CREATIVEMODE_TAB_REGISTRY.register(modEventBus);
        PoolEntryTypeRegistry.LOOT_POOL_ENTRY_TYPES.register(modEventBus);
        FeatureRegistry.FEATURES.register(modEventBus);
        PlacementModifierTypeRegistry.PLACEMENT_MODIFIERS.register(modEventBus);
        MenuRegistry.MENU_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configs.SERVER_SPEC);
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
        event.register(EntityRegistry.MARMOT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityType.CAMEL, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityRegistry.CHAMELEON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityRegistry.OWL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityRegistry.SECRETARYBIRD.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(EntityRegistry.ORANGUTAN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
    private void addEntityAttributes(EntityAttributeCreationEvent event){
        event.put(EntityRegistry.GIRAFFE.get(), Giraffe.attributes().build());
        event.put(EntityRegistry.RED_PANDA.get(), RedPanda.attributes().build());
        event.put(EntityRegistry.ZEBRA.get(), Zebra.attributes().build());
        event.put(EntityRegistry.MAMMOTH.get(), Mammoth.attributes().build());
        event.put(EntityRegistry.MARMOT.get(), Marmot.attributes().build());
        event.put(EntityRegistry.CHAMELEON.get(), Chameleon.attributes().build());
        event.put(EntityRegistry.OWL.get(), Owl.attributes().build());
        event.put(EntityRegistry.SECRETARYBIRD.get(), Secretarybird.attributes().build());
        event.put(EntityRegistry.ORANGUTAN.get(), Orangutan.attributes().build());
        event.put(EntityRegistry.BEAVER.get(), Beaver.attributes().build());
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

    private void commonSetup(FMLCommonSetupEvent event) {
        NetworkChannel.register();
        event.enqueueWork(() -> {
            OwlDeliveryTrigger.INSTANCE = CriteriaTriggers.register(new OwlDeliveryTrigger());
            BeaverBuildTrigger.INSTANCE = CriteriaTriggers.register(new BeaverBuildTrigger());
            ConstructorBlockEntity.PALETTE.put(BlockRegistry.MUDDY_STICK_BUNDLE.get(), 0x614e35);
            ConstructorBlockEntity.PALETTE.put(BlockRegistry.STICK_BUNDLE.get(), 0x755b36);
            ConstructorBlockEntity.PALETTE.put(Blocks.ACACIA_PLANKS, 0xa85a32);
            ConstructorBlockEntity.PALETTE.put(Blocks.ACACIA_WOOD, 0x666056);
            ConstructorBlockEntity.PALETTE.put(Blocks.BAMBOO_BLOCK, 0x7f9039);
            ConstructorBlockEntity.PALETTE.put(Blocks.BAMBOO_MOSAIC, 0xbeaa4e);
            ConstructorBlockEntity.PALETTE.put(Blocks.BAMBOO_PLANKS, 0xc2ad50);
            ConstructorBlockEntity.PALETTE.put(Blocks.BIRCH_PLANKS, 0xc0af79);
            ConstructorBlockEntity.PALETTE.put(Blocks.BIRCH_WOOD, 0xd8d7d2);
            ConstructorBlockEntity.PALETTE.put(Blocks.BLACK_WOOL, 0x141519);
            ConstructorBlockEntity.PALETTE.put(Blocks.BLUE_WOOL, 0x35399d);
            ConstructorBlockEntity.PALETTE.put(Blocks.BROWN_WOOL, 0x724728);
            ConstructorBlockEntity.PALETTE.put(Blocks.CHERRY_PLANKS, 0xe2b2ac);
            ConstructorBlockEntity.PALETTE.put(Blocks.CHERRY_WOOD, 0x36202c);
            ConstructorBlockEntity.PALETTE.put(Blocks.CLAY, 0xa0a6b3);
            ConstructorBlockEntity.PALETTE.put(Blocks.COARSE_DIRT, 0x77553b);
            ConstructorBlockEntity.PALETTE.put(Blocks.CRIMSON_HYPHAE, 0x5b1a1e);
            ConstructorBlockEntity.PALETTE.put(Blocks.CRIMSON_PLANKS, 0x643046);
            ConstructorBlockEntity.PALETTE.put(Blocks.CYAN_WOOL, 0x158991);
            ConstructorBlockEntity.PALETTE.put(Blocks.DARK_OAK_PLANKS, 0x422b14);
            ConstructorBlockEntity.PALETTE.put(Blocks.DARK_OAK_WOOD, 0x3c2e1a);
            ConstructorBlockEntity.PALETTE.put(Blocks.DIRT, 0x866042);
            ConstructorBlockEntity.PALETTE.put(Blocks.GRAY_WOOL, 0x3e4447);
            ConstructorBlockEntity.PALETTE.put(Blocks.GREEN_WOOL, 0x546d1b);
            ConstructorBlockEntity.PALETTE.put(Blocks.JUNGLE_PLANKS, 0xa07351);
            ConstructorBlockEntity.PALETTE.put(Blocks.JUNGLE_WOOD, 0x554319);
            ConstructorBlockEntity.PALETTE.put(Blocks.LIGHT_BLUE_WOOL, 0x3aafd9);
            ConstructorBlockEntity.PALETTE.put(Blocks.LIGHT_GRAY_WOOL, 0x8e8e86);
            ConstructorBlockEntity.PALETTE.put(Blocks.LIME_WOOL, 0x70b919);
            ConstructorBlockEntity.PALETTE.put(Blocks.MAGENTA_WOOL, 0xbd44b4);
            ConstructorBlockEntity.PALETTE.put(Blocks.MANGROVE_PLANKS, 0x763630);
            ConstructorBlockEntity.PALETTE.put(Blocks.MANGROVE_ROOTS, 0x4f3f28);
            ConstructorBlockEntity.PALETTE.put(Blocks.MANGROVE_WOOD, 0x534229);
            ConstructorBlockEntity.PALETTE.put(Blocks.MOSS_BLOCK, 0x596d2d);
            ConstructorBlockEntity.PALETTE.put(Blocks.MUD, 0x3c393c);
            ConstructorBlockEntity.PALETTE.put(Blocks.MUDDY_MANGROVE_ROOTS, 0x443a30);
            ConstructorBlockEntity.PALETTE.put(Blocks.MUD_BRICKS, 0x89674f);
            ConstructorBlockEntity.PALETTE.put(Blocks.OAK_PLANKS, 0xa2834e);
            ConstructorBlockEntity.PALETTE.put(Blocks.OAK_WOOD, 0x6c5532);
            ConstructorBlockEntity.PALETTE.put(Blocks.ORANGE_WOOL, 0xf07613);
            ConstructorBlockEntity.PALETTE.put(Blocks.PACKED_MUD, 0x8e6a4f);
            ConstructorBlockEntity.PALETTE.put(Blocks.PINK_WOOL, 0xee8dac);
            ConstructorBlockEntity.PALETTE.put(Blocks.PURPLE_WOOL, 0x792aac);
            ConstructorBlockEntity.PALETTE.put(Blocks.RED_WOOL, 0xa12722);
            ConstructorBlockEntity.PALETTE.put(Blocks.ROOTED_DIRT, 0x8f674c);
            ConstructorBlockEntity.PALETTE.put(Blocks.SPRUCE_PLANKS, 0x725430);
            ConstructorBlockEntity.PALETTE.put(Blocks.SPRUCE_WOOD, 0x3a2510);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_ACACIA_WOOD, 0xae5c3b);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_BAMBOO_BLOCK, 0x0c1ad50);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_BIRCH_WOOD, 0xc5b076);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_CHERRY_WOOD, 0xd79195);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_CRIMSON_HYPHAE, 0x89395a);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_DARK_OAK_WOOD, 0x483824);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_JUNGLE_WOOD, 0xab8454);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_MANGROVE_WOOD, 0x773630);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_OAK_WOOD, 0xb19056);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_SPRUCE_WOOD, 0x735a34);
            ConstructorBlockEntity.PALETTE.put(Blocks.STRIPPED_WARPED_HYPHAE, 0x399793);
            ConstructorBlockEntity.PALETTE.put(Blocks.WARPED_HYPHAE, 0x393b4d);
            ConstructorBlockEntity.PALETTE.put(Blocks.WARPED_PLANKS, 0x2b6963);
            ConstructorBlockEntity.PALETTE.put(Blocks.WHITE_WOOL, 0xe9ecec);
            ConstructorBlockEntity.PALETTE.put(Blocks.YELLOW_WOOL, 0xf8c627);
        });
    }
}
