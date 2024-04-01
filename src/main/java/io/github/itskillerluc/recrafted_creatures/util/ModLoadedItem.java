package io.github.itskillerluc.recrafted_creatures.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import io.github.itskillerluc.recrafted_creatures.registries.PoolEntryTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class ModLoadedItem extends LootPoolSingletonContainer {
    private final Item exclusiveItem;
    private final Item backupItem;
    private final String mod;

    private ModLoadedItem(Item exclusiveItem, Item backupItem, int weight, int quality, String mod, LootItemCondition[] conditions, LootItemFunction[] functions)
    {
        super(weight, quality, conditions, ModList.get().isLoaded(mod) ? functions : new LootItemFunction[]{});
        this.exclusiveItem = exclusiveItem;
        this.backupItem = backupItem;
        this.mod = mod;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext)
    {
        stackConsumer.accept(new ItemStack(ModList.get().isLoaded(mod) ? exclusiveItem : backupItem));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike item)
    {
        return simpleBuilder((weight, quality, conditions, functions) ->
                new ModLoadedItem(item.asItem(), Items.AIR, weight, quality, "", conditions, functions)
        );
    }

    @Override
    public LootPoolEntryType getType() {
        return PoolEntryTypeRegistry.MOD_LOADED_ITEM.get(); }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ModLoadedItem>
    {
        @Override
        public void serializeCustom(JsonObject object, ModLoadedItem loot, JsonSerializationContext ctx)
        {
            super.serializeCustom(object, loot, ctx);

            ResourceLocation exclusiveItem = ForgeRegistries.ITEMS.getKey(loot.exclusiveItem);
            ResourceLocation backupItem = ForgeRegistries.ITEMS.getKey(loot.backupItem);
            String mod = loot.mod;
            if (exclusiveItem == null)
            {
                throw new IllegalArgumentException("Can't serialize unknown item " + loot.exclusiveItem);
            }
            if (backupItem == null)
            {
                throw new IllegalArgumentException("Can't serialize unknown item " + loot.backupItem);
            }
            object.addProperty("exclusiveItem", exclusiveItem.toString());
            object.addProperty("backupItem", backupItem.toString());
            object.addProperty("mod", mod);
        }

        @Override
        protected ModLoadedItem deserialize(JsonObject object, JsonDeserializationContext ctx, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions)
        {
            if (!object.has("exclusiveItem") || !object.get("exclusiveItem").isJsonPrimitive())
            {
                throw new JsonSyntaxException("Expected exclusiveItem to be an item, was " + GsonHelper.getType(object));
            }
            if (!object.has("backupItem") || !object.get("backupItem").isJsonPrimitive())
            {
                throw new JsonSyntaxException("Expected backupItem to be an item, was " + GsonHelper.getType(object));
            }

            Item exclusiveItem = Items.AIR;
            if (ForgeRegistries.ITEMS.containsKey(new ResourceLocation(object.get("exclusiveItem").getAsString())))
            {
                exclusiveItem = GsonHelper.getAsItem(object, "exclusiveItem");
            }

            Item backupItem = Items.AIR;
            if (ForgeRegistries.ITEMS.containsKey(new ResourceLocation(object.get("backupItem").getAsString())))
            {
                backupItem = GsonHelper.getAsItem(object, "backupItem");
            }
            return new ModLoadedItem(exclusiveItem, backupItem, weight, quality, GsonHelper.getAsString(object, "mod"), conditions, functions);
        }
    }
}
