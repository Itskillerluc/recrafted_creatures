package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BannerPatternRegistry {
    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS = DeferredRegister.create(BuiltInRegistries.BANNER_PATTERN.key(), RecraftedCreatures.MODID);

    public static final RegistryObject<BannerPattern> ZEBRA_PATTERN = BANNER_PATTERNS.register("zebra_pattern",
            () -> new BannerPattern("zebra_pattern"));
}
