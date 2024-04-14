package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.OwlEnvelope;
import io.github.itskillerluc.recrafted_creatures.client.models.BuilderHatModel;
import io.github.itskillerluc.recrafted_creatures.client.models.JungleStaffModel;
import io.github.itskillerluc.recrafted_creatures.client.renderers.ItemRenderer;
import io.github.itskillerluc.recrafted_creatures.item.JungleStaff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.rmi.registry.Registry;
import java.util.function.Consumer;

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

    public static RegistryObject<ForgeSpawnEggItem> BEAVER_SPAWN_EGG = ITEMS.register("beaver_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.BEAVER, 0x7C5240, 0xC08F5F, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> CHAMELEON_SPAWN_EGG = ITEMS.register("chameleon_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.CHAMELEON, 0x90EE90, 0x00A432, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> OWL_SPAWN_EGG = ITEMS.register("owl_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.OWL, 0x7B3F00, 0xB87333, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> SECRETARYBIRD_SPAWN_EGG = ITEMS.register("secretarybird_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.SECRETARYBIRD, 0xEDEADE, 0xEBF5FB, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> ORANGUTAN_SPAWN_EGG = ITEMS.register("orangutan_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.ORANGUTAN, 0xd65e09, 0x808080, new Item.Properties()));

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

    public static final RegistryObject<BlockItem> STICK_BUNDLE = ITEMS.register("stick_bundle",
            () -> new BlockItem(BlockRegistry.STICK_BUNDLE.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> MUDDY_STICK_BUNDLE = ITEMS.register("muddy_stick_bundle",
            () -> new BlockItem(BlockRegistry.MUDDY_STICK_BUNDLE.get(), new Item.Properties()));

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


    public static final RegistryObject<Item> RAW_ZEBRA_MEAT = ITEMS.register("raw_zebra_meat",
            () -> new Item(new Item.Properties().food(Foods.BEEF)));

    public static final RegistryObject<Item> COOKED_ZEBRA_MEAT = ITEMS.register("cooked_zebra_meat",
            () -> new Item(new Item.Properties().food(Foods.COOKED_BEEF)));

    public static final RegistryObject<Item> FRUIT_KEBAB = ITEMS.register("fruit_kebab",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(10).saturationMod(0.7f).build())));

    public static final RegistryObject<BlockItem> ZEBRA_CARPET = ITEMS.register("zebra_carpet",
            () -> new BlockItem(BlockRegistry.ZEBRA_CARPET.get(), new Item.Properties()));

    public static final RegistryObject<JungleStaff> JUNGLE_STAFF = ITEMS.register("jungle_staff",
            () -> new JungleStaff(new Item.Properties().durability(20)));

    public static BuilderHatModel builderHatModel;
    public static final RegistryObject<Item> BUILDER_HAT = ITEMS.register("builder_hat",
            () -> new ArmorItem(ArmorMaterials.BUILDER, ArmorItem.Type.HELMET, new Item.Properties()) {
                @Override
                public void initializeClient(Consumer<IClientItemExtensions> consumer) {
                    consumer.accept(new IClientItemExtensions() {
                        @Override
                        public @NotNull Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                            builderHatModel.headRotation = new Vec2(original.head.xRot, original.head.yRot);
                            return builderHatModel;
                        }
                    });
                }
            });
}
