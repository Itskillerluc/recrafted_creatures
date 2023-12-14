package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.blockentity.EggBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RecraftedCreatures.MODID);

    public static final RegistryObject<BlockEntityType<?>> CHAMELEON_EGG = BLOCK_ENTITY_TYPES.register("chameleon_egg",
            () -> BlockEntityType.Builder.of((pPos, pState) ->
                    new EggBlockEntity<>(BlockEntityRegistry.CHAMELEON_EGG.get(), pPos, pState, 4500, level ->
                            EntityRegistry.CHAMELEON.get().create(level), 2, BlockRegistry.CHAMELEON_EGG_BLOCK.get().getEggs()), BlockRegistry.CHAMELEON_EGG_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> OWL_EGG = BLOCK_ENTITY_TYPES.register("owl_egg",
            () -> BlockEntityType.Builder.of((pPos, pState) ->
                    new EggBlockEntity<>(BlockEntityRegistry.OWL_EGG.get(), pPos, pState, 9000, level ->
                            EntityRegistry.OWL.get().create(level), 2, BlockRegistry.OWL_EGG_BLOCK.get().getEggs()), BlockRegistry.OWL_EGG_BLOCK.get()).build(null));
}
