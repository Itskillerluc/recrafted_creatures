package io.github.itskillerluc.recrafted_creatures.worldgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.itskillerluc.recrafted_creatures.registries.PlacementModifierTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Arrays;

public class AdjacentChunkPlacementModifier extends PlacementFilter {
    public static final Codec<AdjacentChunkPlacementModifier> CODEC = RecordCodecBuilder.create(top ->
            top.group(StringRepresentable.fromEnum(Type::values).fieldOf("predicateType").forGetter(AdjacentChunkPlacementModifier::getType),
            TagKey.hashedCodec(Registries.BIOME).fieldOf("biome").forGetter(AdjacentChunkPlacementModifier::getBiome))
            .apply(top, AdjacentChunkPlacementModifier::new));
    private final Type type;
    private final TagKey<Biome> biome;

    public Type getType() {
        return type;
    }

    public TagKey<Biome> getBiome() {
        return biome;
    }
    public AdjacentChunkPlacementModifier(Type type, TagKey<Biome> biome) {
        this.type = type;
        this.biome = biome;
    }

    @Override
    protected boolean shouldPlace(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
        return switch (type) {
            case ANY_MATCH -> Arrays.stream(getAdjacentChunks(pPos)).anyMatch(pos -> pContext.getLevel().getBiome(pos).is(biome));
            case ALL_MATCH -> Arrays.stream(getAdjacentChunks(pPos)).allMatch(pos -> pContext.getLevel().getBiome(pos).is(biome));
            case NONE_MATCH ->Arrays.stream(getAdjacentChunks(pPos)).noneMatch(pos -> pContext.getLevel().getBiome(pos).is(biome));
        };
    }

    private BlockPos[] getAdjacentChunks(BlockPos pos) {
        return new BlockPos[] {
                pos.north(16),
                pos.east(16),
                pos.south(16),
                pos.west(16),
                pos.north(16).east(16),
                pos.east(16).south(16),
                pos.south(16).west(16),
                pos.west(16).north(16)};
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierTypeRegistry.ADJACENT_CHUNK_MODIFIER.get();
    }

    public enum Type implements StringRepresentable{
        ANY_MATCH,
        ALL_MATCH,
        NONE_MATCH;

        @Override
        public String getSerializedName() {
            return switch(this) {
                case ANY_MATCH -> "any_match";
                case ALL_MATCH -> "all_match";
                case NONE_MATCH -> "none_match";
            };
        }
    }
}
