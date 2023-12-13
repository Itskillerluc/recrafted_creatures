package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.OwlEnvelope;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RecraftedCreatures.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> GIRAFFE_SPAWN_EGG = ITEMS.register("giraffe_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GIRAFFE, 0xeab676, 0x21130d, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> RED_PANDA_SPAWN_EGG = ITEMS.register("red_panda_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.RED_PANDA, 0xBE4826, 0x3C251E, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> ZEBRA_SPAWN_EGG = ITEMS.register("zebra_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.ZEBRA, 0xFFFFFF, 0x000000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> MAMMOTH_SPAWN_EGG = ITEMS.register("mammoth_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.MAMMOTH, 0x6b4832, 0x473123, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> MARMOT_SPAWN_EGG = ITEMS.register("marmot_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.MARMOT, 0xC08F5F, 0x7C5240, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> CHAMELEON_SPAWN_EGG = ITEMS.register("chameleon_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.CHAMELEON, 0x90EE90, 0x00A432, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> OWL_SPAWN_EGG = ITEMS.register("owl_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.OWL, 0x7B3F00, 0xB87333	, new Item.Properties()));

    public static final RegistryObject<BlockItem> MAMMOTH_MEAT = ITEMS.register("raw_mammoth_meat",
            () -> new BlockItem(BlockRegistry.RAW_MAMMOTH_MEAT.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> COOKED_MAMMOTH_MEAT = ITEMS.register("cooked_mammoth_meat",
            () -> new BlockItem(BlockRegistry.COOKED_MAMMOTH_MEAT.get(), new Item.Properties()));

    public static final RegistryObject<MobBucketItem> FROG_BUCKET = ITEMS.register("frog_bucket",
            () -> new MobBucketItem(() -> EntityType.FROG, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_TADPOLE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ZEBRA_HIDE = ITEMS.register("zebra_hide",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<BannerPatternItem> ZEBRA_PATTERN = ITEMS.register("zebra_pattern",
            () -> new BannerPatternItem(Tags.ZEBRA_PATTERN, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> MEGAPHONE_FRAGMENT = ITEMS.register("megaphone_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> APPLE_SLICE = ITEMS.register("apple_slice",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().fast().nutrition(2).build())));

    public static final RegistryObject<Item> LEAF_PICKER = ITEMS.register("leaf_picker",
            () -> new Item(new Item.Properties().durability(100)) {
                @Override
                public InteractionResult useOn(UseOnContext pContext) {
                    if (!pContext.getLevel().isClientSide()) {
                        if (pContext.getLevel().getBlockState(pContext.getClickedPos()).is(BlockTags.LEAVES)) {
                            if (!pContext.getPlayer().addItem(new ItemStack(ItemRegistry.SMALL_LEAF.get()))) {
                                pContext.getPlayer().drop(new ItemStack(ItemRegistry.SMALL_LEAF.get()), false);
                            }
                            return InteractionResult.SUCCESS;
                        }
                    }
                    return InteractionResult.FAIL;
                }
            });

    public static final RegistryObject<Item> SMALL_LEAF = ITEMS.register("small_leaf",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> OWL_ENVELOPE = ITEMS.register("owl_envelope",
            () -> new OwlEnvelope(new Item.Properties()));

    public static final RegistryObject<BlockItem> CHAMELEON_EGG = ITEMS.register("chameleon_egg",
            () -> new BlockItem(BlockRegistry.CHAMELEON_EGG_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> OWL_EGG = ITEMS.register("owl_egg",
            () -> new BlockItem(BlockRegistry.OWL_EGG_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> RAINBOW_GEL = ITEMS.register("rainbow_gel",
            () -> new BlockItem(BlockRegistry.RAINBOW_GEL.get(), new Item.Properties()));

    public static final RegistryObject<InstrumentItem> MEGAPHONE = ITEMS.register("megaphone",
            () -> new InstrumentItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), Tags.MEGAPHONE) {
                @Override
                public UseAnim getUseAnimation(ItemStack pStack) {
                    if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                        return UseAnim.BOW;
                    } else {
                        return UseAnim.TOOT_HORN;
                    }
                }
            });
}
