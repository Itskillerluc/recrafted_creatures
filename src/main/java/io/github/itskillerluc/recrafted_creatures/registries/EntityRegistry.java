package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RecraftedCreatures.MODID);

    public static final RegistryObject<EntityType<Giraffe>> GIRAFFE = ENTITY_TYPES.register("giraffe",
            () -> EntityType.Builder.of(Giraffe::new, MobCategory.CREATURE).sized(1, 3)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "giraffe").toString()));

    public static final RegistryObject<EntityType<RedPanda>> RED_PANDA = ENTITY_TYPES.register("red_panda",
            () -> EntityType.Builder.of(RedPanda::new, MobCategory.CREATURE).sized(1, 1)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "red_panda").toString()));

    public static final RegistryObject<EntityType<Zebra>> ZEBRA = ENTITY_TYPES.register("zebra",
            () -> EntityType.Builder.of(Zebra::new, MobCategory.CREATURE).sized(1.3964844F, 1.8F)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "zebra").toString()));

    public static final RegistryObject<EntityType<Mammoth>> MAMMOTH = ENTITY_TYPES.register("mammoth",
            () -> EntityType.Builder.of(Mammoth::new, MobCategory.CREATURE).sized(3, 3f)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "mammoth").toString()));

    public static final RegistryObject<EntityType<Marmot>> MARMOT = ENTITY_TYPES.register("marmot",
            () -> EntityType.Builder.of(Marmot::new, MobCategory.CREATURE).sized(0.5f, 1f)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "marmot").toString()));

    public static final RegistryObject<EntityType<Chameleon>> CHAMELEON = ENTITY_TYPES.register("chameleon",
            () -> EntityType.Builder.of(Chameleon::new, MobCategory.CREATURE).sized(.9f, 0.5f)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "chameleon").toString()));

    public static final RegistryObject<EntityType<Owl>> OWL = ENTITY_TYPES.register("owl",
            () -> EntityType.Builder.of(Owl::new, MobCategory.CREATURE).sized(0.5f, 1f)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "owl").toString()));

    public static final RegistryObject<EntityType<Secretarybird>> SECRETARYBIRD = ENTITY_TYPES.register("secretarybird",
            () -> EntityType.Builder.of(Secretarybird::new, MobCategory.CREATURE).sized(1f, 1.2f)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "secretarybird").toString()));
}