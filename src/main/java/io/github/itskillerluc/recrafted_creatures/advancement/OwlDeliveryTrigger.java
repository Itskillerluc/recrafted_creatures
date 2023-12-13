package io.github.itskillerluc.recrafted_creatures.advancement;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class OwlDeliveryTrigger extends SimpleCriterionTrigger<OwlDeliveryTrigger.TriggerInstance> {
    public static OwlDeliveryTrigger INSTANCE;
    static final ResourceLocation ID = new ResourceLocation(RecraftedCreatures.MODID, "owl_delivery");

    public ResourceLocation getId() {
        return ID;
    }

    public OwlDeliveryTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
        return new OwlDeliveryTrigger.TriggerInstance(pPredicate, ItemPredicate.fromJson(pJson.get("item")));
    }

    public void trigger(ServerPlayer pPlayer, ItemStack pItem) {
        this.trigger(pPlayer, (p_23687_) -> {
            return p_23687_.matches(pItem);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ContextAwarePredicate pPlayer, ItemPredicate item) {
            super(OwlDeliveryTrigger.ID, pPlayer);
            this.item = item;
        }

        public static OwlDeliveryTrigger.TriggerInstance usedItem() {
            return new OwlDeliveryTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY);
        }

        public static OwlDeliveryTrigger.TriggerInstance usedItem(ItemPredicate pItem) {
            return new OwlDeliveryTrigger.TriggerInstance(ContextAwarePredicate.ANY, pItem);
        }

        public static OwlDeliveryTrigger.TriggerInstance usedItem(ItemLike pItem) {
            return new OwlDeliveryTrigger.TriggerInstance(ContextAwarePredicate.ANY, new ItemPredicate((TagKey<Item>)null, ImmutableSet.of(pItem.asItem()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, (Potion)null, NbtPredicate.ANY));
        }

        public boolean matches(ItemStack pItem) {
            return this.item.matches(pItem);
        }

        public JsonObject serializeToJson(SerializationContext pConditions) {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("item", this.item.serializeToJson());
            return jsonobject;
        }
    }
}
