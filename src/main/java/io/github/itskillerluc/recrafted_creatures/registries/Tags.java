package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BannerPattern;

public class Tags {
    public static final TagKey<BannerPattern> ZEBRA_PATTERN = TagKey.create(Registries.BANNER_PATTERN, new ResourceLocation(RecraftedCreatures.MODID, "pattern_item/zebra_pattern"));

    public static final TagKey<Instrument> MEGAPHONE = TagKey.create(Registries.INSTRUMENT, new ResourceLocation(RecraftedCreatures.MODID, "megaphone"));

    public static final TagKey<Biome> OWL_FOREST = TagKey.create(Registries.BIOME, new ResourceLocation(RecraftedCreatures.MODID, "owl_forest"));
}
