package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PaintingRegistry {
    public static final DeferredRegister<PaintingVariant> PAINTINGS = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, RecraftedCreatures.MODID);

    public static final RegistryObject<PaintingVariant> SAFARI = PAINTINGS.register("safari",
            () -> new PaintingVariant(64, 48));

    public static final RegistryObject<PaintingVariant> GLOBAL_WARMING = PAINTINGS.register("global_warming",
            () -> new PaintingVariant(16, 16));

    public static final RegistryObject<PaintingVariant> POP_ART = PAINTINGS.register("pop_art",
            () -> new PaintingVariant(32, 32));

    public static final RegistryObject<PaintingVariant> NOT_LIKE_THE_OTHER_ORANGES = PAINTINGS.register("not_like_the_other_oranges",
            () -> new PaintingVariant(32, 32));
}
