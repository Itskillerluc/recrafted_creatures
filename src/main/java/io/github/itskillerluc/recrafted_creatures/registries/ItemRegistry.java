package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.BlockRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RecraftedCreatures.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> GIRAFFE_SPAWN_EGG = ITEMS.register("giraffe_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GIRAFFE, 0xeab676, 0x21130d, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> RED_PANDA_SPAWN_EGG = ITEMS.register("red_panda_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.RED_PANDA, 0xBE4826, 0x3C251E, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> ZEBRA_SPAWN_EGG = ITEMS.register("zebra_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.ZEBRA, 0xFFFFFF, 0x000000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> MAMMOTH_SPAWN_EGG = ITEMS.register("mammoth_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.MAMMOTH, 0x6b4832, 0x473123, new Item.Properties()));

    public static final RegistryObject<BlockItem> ELEPHANT_MEAT = ITEMS.register("raw_mammoth_meat",
            () -> new BlockItem(BlockRegistry.RAW_ELEPHANT_MEAT.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> COOKED_ELEPHANT_MEAT = ITEMS.register("cooked_mammoth_meat",
            () -> new BlockItem(BlockRegistry.COOKED_ELEPHANT_MEAT.get(), new Item.Properties()));

}
