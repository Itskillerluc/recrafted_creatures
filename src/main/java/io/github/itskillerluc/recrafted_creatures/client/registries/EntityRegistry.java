package io.github.itskillerluc.recrafted_creatures.client.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.entity.Giraffe;
import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
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
            () -> EntityType.Builder.of(RedPanda::new, MobCategory.CREATURE)
                    .build(new ResourceLocation(RecraftedCreatures.MODID, "red_panda").toString()));
}
