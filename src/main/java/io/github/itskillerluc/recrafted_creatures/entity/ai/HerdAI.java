package io.github.itskillerluc.recrafted_creatures.entity.ai;

import io.github.itskillerluc.recrafted_creatures.capability.HerdProvider;
import io.github.itskillerluc.recrafted_creatures.registries.MemoryModuleRegistry;
import io.github.itskillerluc.recrafted_creatures.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class HerdAI {
    public static OneShot<PathfinderMob> stroll(float speed, int horizontalMax, int verticalMax, boolean pMayStrollFromWater) {
        return BehaviorBuilder.create((instance) ->
                instance.group(instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleRegistry.HERD.get())).apply(instance, (target, herd) -> {
                    if (instance.tryGet(herd).isPresent()) {
                        return (level, entity, gameTime) -> {
                            if (!pMayStrollFromWater && entity.isInWaterOrBubble()) return false;
                            var randomDir = RandomPos.generateRandomDirection(entity.getRandom(), horizontalMax, verticalMax);
                            UUID herdUUID = instance.tryGet(herd).get();
                            Herd herdInstance = level.getCapability(HerdProvider.HERD_CAP).resolve().get().getHerd(herdUUID);
                            double x = 0;
                            double y = 0;
                            double z = 0;
                            for (UUID animal : herdInstance.animals) {
                                var herdAnimal = level.getEntity(animal);
                                if (herdAnimal == null) continue;
                                x += herdAnimal.position().x();
                                y += herdAnimal.position().y();
                                z += herdAnimal.position().z();
                            }
                            Vec3 middlePos = new Vec3(x / herdInstance.animals.size(), y / herdInstance.animals.size(), z /herdInstance.animals.size());
                            var actualPos = randomDir.offset(new Vec3i(((int) middlePos.x()), ((int) middlePos.y()), ((int) middlePos.z())));
                            Optional<BlockPos> optional = Optional.ofNullable(!GoalUtils.isOutsideLimits(actualPos, entity) && !entity.isWithinRestriction(actualPos) && !GoalUtils.isNotStable(entity.getNavigation(), actualPos) ? actualPos : null);
                            target.setOrErase(optional.map((pos) -> new WalkTarget(pos, speed, 0)));
                            return true;
                        };
                    } else {
                        return ((OneShot<PathfinderMob>) RandomStroll.stroll(speed, horizontalMax, verticalMax));
                    }
                }));
    }

    public static <T> OneShot<PathfinderMob> flee(MemoryModuleType<T> pWalkTargetAwayFromMemory, float pSpeedModifier, int pDesiredDistance, boolean pHasTarget, Function<T, Vec3> pToPosition) {
        return BehaviorBuilder.create((instance) ->
            instance.group(instance.registered(MemoryModuleRegistry.HERD.get()), instance.registered(MemoryModuleType.WALK_TARGET), instance.present(pWalkTargetAwayFromMemory)).apply(instance, (herd, target, away) -> {
                if (instance.tryGet(herd).isPresent()) {
                    return (level, entity, gameTime) -> {
                        if (level.getCapability(HerdProvider.HERD_CAP).isPresent()) {
                            Optional<WalkTarget> optional = instance.tryGet(target);
                            if (optional.isPresent() && !pHasTarget) {
                                return false;
                            }
                            Vec3 vec3 = entity.position();
                            Vec3 vec31 = pToPosition.apply(instance.get(away));
                            if (!vec3.closerThan(vec31, pDesiredDistance)) {
                                return false;
                            } else {
                                if (optional.isPresent() && optional.get().getSpeedModifier() == pSpeedModifier) {
                                    Vec3 vec32 = optional.get().getTarget().currentPosition().subtract(vec3);
                                    Vec3 vec33 = vec31.subtract(vec3);
                                    if (vec32.dot(vec33) < 0.0D) {
                                        return false;
                                    }
                                }

                                UUID herdUUID = instance.tryGet(herd).get();
                                Herd herdInstance = level.getCapability(HerdProvider.HERD_CAP).resolve().get().getHerd(herdUUID);

                                double x = 0;
                                double y = 0;
                                double z = 0;
                                for (UUID animal : herdInstance.animals) {
                                    var herdAnimal = level.getEntity(animal);
                                    if (herdAnimal == null) continue;
                                    x += herdAnimal.position().x();
                                    y += herdAnimal.position().y();
                                    z += herdAnimal.position().z();
                                }

                                Vec3 middlePos = new Vec3(x / herdInstance.animals.size(), y / herdInstance.animals.size(), z / herdInstance.animals.size());

                                PathfinderMob closestEntity = (PathfinderMob) herdInstance.animals.stream().map(level::getEntity).filter(e -> e instanceof PathfinderMob).min(Comparator.comparingDouble(e -> e.distanceToSqr(middlePos))).orElse(entity);

                                for (int i = 0; i < 10; ++i) {
                                    Vec3 vec30 = closestEntity.position().subtract(vec31);
                                    boolean flag = GoalUtils.mobRestricted(closestEntity, 16);
                                    Vec3 vec34 = Util.getPosInDirection(closestEntity, 16, 7, vec30, flag, Math.PI / 10f);
                                    if (vec34 != null) {
                                        target.set(new WalkTarget(vec34, pSpeedModifier, 0));
                                        break;
                                    }
                                }
                                return true;
                            }
                        }
                        return false;
                    };
                } else {
                    return SetWalkTargetAwayFrom.create(pWalkTargetAwayFromMemory, pSpeedModifier, pDesiredDistance, pHasTarget, pToPosition);
                }
            }
        ));
    }
}