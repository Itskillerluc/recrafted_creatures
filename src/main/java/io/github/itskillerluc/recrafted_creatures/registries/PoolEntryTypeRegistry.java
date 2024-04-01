package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.util.ModLoadedItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PoolEntryTypeRegistry {
    public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, RecraftedCreatures.MODID);

    public static final RegistryObject<LootPoolEntryType> MOD_LOADED_ITEM = LOOT_POOL_ENTRY_TYPES.register("mod_loaded_item",
            () -> new LootPoolEntryType(new ModLoadedItem.Serializer()));
}
