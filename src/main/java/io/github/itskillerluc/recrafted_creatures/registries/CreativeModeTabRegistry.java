package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Comparator;

import static net.minecraft.world.item.CreativeModeTabs.generateInstrumentTypes;

public class CreativeModeTabRegistry {
    public static DeferredRegister<CreativeModeTab> CREATIVEMODE_TAB_REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RecraftedCreatures.MODID);
    private static final Comparator<Holder<PaintingVariant>> PAINTING_COMPARATOR = Comparator.comparing(Holder::value, Comparator.<PaintingVariant>comparingInt((p_270004_) -> {
        return p_270004_.getHeight() * p_270004_.getWidth();
    }).thenComparing(PaintingVariant::getWidth));
    public static final RegistryObject<CreativeModeTab> RECRAFTED_TAB = CREATIVEMODE_TAB_REGISTRY.register("recrafted_creatures",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + RecraftedCreatures.MODID + ".tab"))
                    .icon(() -> new ItemStack(ItemRegistry.GIRAFFE_SPAWN_EGG.get()))
                    .displayItems((param, out) -> {
                        param.holders().lookup(Registries.INSTRUMENT).ifPresent((action) -> {
                            generateInstrumentTypes(out, action, ItemRegistry.MEGAPHONE.get(), Tags.MEGAPHONE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        });
                        ItemRegistry.ITEMS.getEntries()
                                .stream()
                                .map(RegistryObject::get)
                                .filter(item -> item != ItemRegistry.MEGAPHONE.get() &&
                                        item != ItemRegistry.APPLE_SLICE.get() &&
                                        item != ItemRegistry.LEAF_PICKER.get() &&
                                        item != ItemRegistry.SMALL_LEAF.get())
                                .forEach(out::accept);

                        PaintingRegistry.PAINTINGS.getEntries().stream().map(entry -> entry.getHolder().get()).sorted(PAINTING_COMPARATOR).forEach((p_269979_) -> {
                            ItemStack itemstack = new ItemStack(Items.PAINTING);
                            CompoundTag compoundtag = itemstack.getOrCreateTagElement("EntityTag");
                            Painting.storeVariant(compoundtag, p_269979_);
                            out.accept(itemstack);
                        });
                    }).build());
}
