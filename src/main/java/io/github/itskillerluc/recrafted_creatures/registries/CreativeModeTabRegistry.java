package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeModeTabRegistry {
    public static DeferredRegister<CreativeModeTab> CREATIVEMODE_TAB_REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RecraftedCreatures.MODID);

    public static final RegistryObject<CreativeModeTab> RECRAFTED_TAB = CREATIVEMODE_TAB_REGISTRY.register("recrafted_creatures",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + RecraftedCreatures.MODID + ".tab"))
                    .icon(() -> new ItemStack(ItemRegistry.GIRAFFE_SPAWN_EGG.get()))
                    .displayItems((param, out) -> ItemRegistry.ITEMS.getEntries()
                            .stream().map(RegistryObject::get).forEach(out::accept)).build());
}
