package io.github.itskillerluc.recrafted_creatures.registries;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.UUID;

public class MemoryModuleRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, RecraftedCreatures.MODID);

    public static final RegistryObject<MemoryModuleType<UUID>> HERD = MEMORIES.register("herd", () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC)));
}
