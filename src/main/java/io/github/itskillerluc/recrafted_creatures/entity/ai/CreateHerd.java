package io.github.itskillerluc.recrafted_creatures.entity.ai;

import com.google.common.collect.Lists;
import io.github.itskillerluc.recrafted_creatures.capability.HerdProvider;
import io.github.itskillerluc.recrafted_creatures.capability.IHerd;
import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import io.github.itskillerluc.recrafted_creatures.registries.MemoryModuleRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.*;

public class CreateHerd extends Behavior<LivingEntity> {
    public CreateHerd() {
        super(Map.of(MemoryModuleRegistry.HERD.get(),MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        if (level.getCapability(HerdProvider.HERD_CAP).isPresent()) {
            IHerd cap = level.getCapability(HerdProvider.HERD_CAP).resolve().get();
            for (LivingEntity livingEntity : entity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get()) {
                if (!(livingEntity instanceof Zebra)) continue;
                Optional<UUID> herdMemory = livingEntity.getBrain().getMemory(MemoryModuleRegistry.HERD.get());
                if (livingEntity.getUUID() != entity.getUUID() && (herdMemory.isEmpty() || (cap.getHerd(herdMemory.get())) != null && cap.getHerd(herdMemory.get()).animals.size() < cap.getHerd(herdMemory.get()).maxSize)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canStillUse(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
        return pEntity.getBrain().checkMemory(MemoryModuleRegistry.HERD.get(), MemoryStatus.VALUE_ABSENT) &&
                pEntity.getBrain().checkMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT);
    }

    public void start(ServerLevel level, LivingEntity entity, long gameTime) {
        level.getProfiler().push("createHerdAI");
        var entities = entity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
        actuallyCreateHerd(level, entity, gameTime, entities);
        level.getProfiler().pop();
    }

    public void actuallyCreateHerd(ServerLevel level, LivingEntity entity, long gameTime, List<LivingEntity> candidates) {
        level.getCapability(HerdProvider.HERD_CAP).ifPresent(cap -> {
            var random = new Random();

            candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(entity)));

            for (LivingEntity livingEntity : candidates) {
                if (!(livingEntity instanceof Zebra)) continue;
                Optional<UUID> herdMemory = livingEntity.getBrain().getMemory(MemoryModuleRegistry.HERD.get());
                if (livingEntity.getUUID() == entity.getUUID() || (herdMemory.isPresent() && (cap.getHerd(herdMemory.get()) == null || cap.getHerd(herdMemory.get()).animals.size() >= cap.getHerd(herdMemory.get()).maxSize))) {
                    continue;
                }

                if (herdMemory.isPresent()) {
                    entity.getBrain().setMemory(MemoryModuleRegistry.HERD.get(), herdMemory.get());
                    cap.getHerd(herdMemory.get()).animals.add(entity.getUUID());
                    return;
                }
                Herd herd = new Herd(Lists.newArrayList(entity.getUUID()), UUID.randomUUID(), ((int) random.nextGaussian(15, 2.5)));
                entity.getBrain().setMemory(MemoryModuleRegistry.HERD.get(), herd.id);
                cap.addHerd(herd.id, herd);
            }
        });
    }
}
