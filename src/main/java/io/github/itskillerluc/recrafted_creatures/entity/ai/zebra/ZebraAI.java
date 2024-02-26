package io.github.itskillerluc.recrafted_creatures.entity.ai.zebra;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import io.github.itskillerluc.recrafted_creatures.entity.Zebra;
import io.github.itskillerluc.recrafted_creatures.entity.ai.CreateHerd;
import io.github.itskillerluc.recrafted_creatures.entity.ai.HerdAI;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.MemoryModuleRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SensorRegistry;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Objects;
import java.util.Optional;

public class ZebraAI {
    private static final ImmutableList<SensorType<? extends Sensor<? super Zebra>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_ADULT, SensorRegistry.ZEBRA_TEMPTATIONS.get(), SensorType.NEAREST_PLAYERS);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.ATTACK_TARGET, MemoryModuleRegistry.HERD.get(), MemoryModuleType.ANGRY_AT, MemoryModuleType.AVOID_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);

    public static Brain.Provider<Zebra> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static Brain<?> makeBrain(Brain<Zebra> pBrain) {
        initCoreActivity(pBrain);
        initIdleActivity(pBrain);
        initFightActivity(pBrain);
        pBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        pBrain.setDefaultActivity(Activity.IDLE);
        pBrain.useDefaultActivity();
        return pBrain;
    }

    private static void initCoreActivity(Brain<Zebra> pBrain) {
        pBrain.addActivity(Activity.CORE, 0,ImmutableList.of(
                new Swim(0.8F),
                new CreateHerd(),
                HerdAI.flee(MemoryModuleType.AVOID_TARGET, 2.0F, 12, true, Entity::position),
                new LookAtTargetSink(45, 90),
                new RunOne<>(ImmutableList.of(
                        Pair.of(new ZebraKickPlayer(), 1),
                        Pair.of(new DoNothing(1, 5), 1)
                )),
                new TameZebra(),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                StopBeingAngryIfTargetDead.create(),
                new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Zebra> pBrain) {
        pBrain.addActivity(Activity.IDLE, ImmutableList.of(
                Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                Pair.of(1, new AnimalMakeLove(EntityRegistry.ZEBRA.get(), 1.0F)),
                Pair.of(1, StartAttacking.create(ZebraAI::findNearestValidAttackTarget)),
                Pair.of(2, new FollowTemptation((p_250812_) -> 1F)),
                Pair.of(2, StartAttacking.create(zebra -> BehaviorUtils.getLivingEntityFromUUIDMemory(zebra, MemoryModuleType.ANGRY_AT))),
                Pair.of(3, BabyFollowAdult.create(UniformInt.of(5, 16), 2.5F)),
                Pair.of(4, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
                Pair.of(5, new RunOne<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableList.of(
                        Pair.of(HerdAI.stroll(1, 15, 5, true), 1),
                        Pair.of(SetWalkTargetFromLookTarget.create(2.0F, 3), 1),
                        Pair.of(new DoNothing(30, 60), 1))))));

    }

    private static void initFightActivity(Brain<Zebra> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F),
                MeleeAttack.create(40),
                StopAttackingIfTargetInvalid.create()), MemoryModuleType.ATTACK_TARGET);
    }

    public static void updateActivity(Zebra zebra) {
        Brain<Zebra> brain = zebra.getBrain();
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        zebra.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    public static void wasHurtBy(Zebra zebra, LivingEntity pTarget) {
        if (!(pTarget instanceof Zebra) && !Objects.equals(zebra.getOwnerUUID(), pTarget.getUUID())) {
            alertOthers(zebra, pTarget);
            zebra.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, pTarget.getUUID(), 600L);
        }
    }

    public static void alertOthers(Zebra zebra, LivingEntity avoid) {
        if (zebra.getBrain().checkMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT)) {
            zebra.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent(memory -> {
                for (LivingEntity livingEntity : memory) {
                    if (livingEntity instanceof Zebra zBra && !livingEntity.getUUID().equals(zebra.getUUID())) {
                        zBra.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_PANICKING, true, 600L);
                        zBra.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, avoid, 600L);
                    }
                }
            });
        }
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Zebra zebra) {
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(zebra, MemoryModuleType.ANGRY_AT);
        return optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(zebra, optional.get()) ? optional : Optional.empty();
    }
}
