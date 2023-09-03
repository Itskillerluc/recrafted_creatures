package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class Tags {
    public static final TagKey<BannerPattern> ZEBRA_PATTERN = TagKey.create(Registries.BANNER_PATTERN, new ResourceLocation(RecraftedCreatures.MODID, "pattern_item/zebra_pattern"));
}
