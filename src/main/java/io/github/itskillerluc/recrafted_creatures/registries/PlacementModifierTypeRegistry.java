package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.*;
import io.github.itskillerluc.recrafted_creatures.worldgen.placement.AdjacentChunkPlacementModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PlacementModifierTypeRegistry {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, RecraftedCreatures.MODID);

    public static final RegistryObject<PlacementModifierType<AdjacentChunkPlacementModifier>> ADJACENT_CHUNK_MODIFIER = PLACEMENT_MODIFIERS.register("adjacent_chunk",
            () -> () -> AdjacentChunkPlacementModifier.CODEC);

}
