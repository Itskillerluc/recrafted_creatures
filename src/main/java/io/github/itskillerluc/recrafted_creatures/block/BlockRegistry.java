package io.github.itskillerluc.recrafted_creatures.block;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RecraftedCreatures.MODID);

    public static final RegistryObject<MeatBlock> RAW_ELEPHANT_MEAT = BLOCKS.register("raw_mammoth_meat", () -> new MeatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), 3, 1));
    public static final RegistryObject<MeatBlock> COOKED_ELEPHANT_MEAT = BLOCKS.register("cooked_mammoth_meat", () -> new MeatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), 5, 4));
}
