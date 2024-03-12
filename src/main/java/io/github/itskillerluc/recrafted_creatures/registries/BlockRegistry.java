package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.ChameleonEggBlock;
import io.github.itskillerluc.recrafted_creatures.block.MeatBlock;
import io.github.itskillerluc.recrafted_creatures.block.OwlEggBlock;
import io.github.itskillerluc.recrafted_creatures.block.SecretarybirdEggBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RecraftedCreatures.MODID);

    public static final RegistryObject<MeatBlock> RAW_MAMMOTH_MEAT = BLOCKS.register("raw_mammoth_meat", () -> new MeatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), 3, 1));
    public static final RegistryObject<MeatBlock> COOKED_MAMMOTH_MEAT = BLOCKS.register("cooked_mammoth_meat", () -> new MeatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), 5, 4));
    public static final RegistryObject<ChameleonEggBlock> CHAMELEON_EGG_BLOCK = BLOCKS.register("chameleon_egg_block", () -> new ChameleonEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<OwlEggBlock> OWL_EGG_BLOCK = BLOCKS.register("owl_egg_block", () -> new OwlEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<SecretarybirdEggBlock> SECRETARYBIRD_EGG_BLOCk = BLOCKS.register("secretarybird_egg_block", () -> new SecretarybirdEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<Block> RAINBOW_GEL = BLOCKS.register("rainbow_gel", () -> new HalfTransparentBlock(BlockBehaviour.Properties.of().sound(SoundType.SLIME_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN).friction(0.8F).noOcclusion().strength(0.8f).lightLevel(block -> 7).jumpFactor(0.6f)));
    public static final RegistryObject<CarpetBlock> ZEBRA_CARPET = BLOCKS.register("zebra_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY)));
}
