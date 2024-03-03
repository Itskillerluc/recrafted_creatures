package io.github.itskillerluc.recrafted_creatures.entity.ai;

import io.github.itskillerluc.recrafted_creatures.entity.RedPanda;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

public class RedPandaPlayGoal extends Goal {
    RedPanda entity;
    int playTime;
    int stand;
    int maxPlayTime;
    final int minMaxPlayTime;
    final int maxMaxPlayTime;

    public RedPandaPlayGoal(RedPanda entity, int minPlayTime, int maxPlayTime) {
        this.entity = entity;
        minMaxPlayTime = minPlayTime;
        maxMaxPlayTime = maxPlayTime;
    }

    @Override
    public boolean canUse() {
        if (!entity.refuseToMove() && entity.getNavigation().isDone()) {
            var pandas = entity.level().getEntitiesOfClass(RedPanda.class, AABB.ofSize(entity.position(), 5, 5, 5), panda -> !panda.refuseToMove() && panda.getNavigation().isDone());
            if (pandas.size() > 1) {
                return entity.getRandom().nextFloat() > 0.8;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!entity.refuseToMove()) {
            var pandas = entity.level().getEntitiesOfClass(RedPanda.class, AABB.ofSize(entity.position(), 8, 8, 8), panda -> !panda.refuseToMove());
            if (pandas.size() > 1) {
                return entity.getRandom().nextFloat() > 0.8;
            }
        }
        return false;
    }

    @Override
    public void start() {
        maxPlayTime = entity.getRandom().nextIntBetweenInclusive(minMaxPlayTime, maxMaxPlayTime);
        entity.isPlaying = true;
    }

    @Override
    public void stop() {
        entity.isPlaying = false;
        playTime = 0;
        entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (playTime > maxPlayTime) {
            stop();
        } else {
            playTime++;
        }
        var pandas = entity.level().getEntitiesOfClass(RedPanda.class, AABB.ofSize(entity.position(), 8, 8, 8));
        var randPanda = pandas.get(entity.getRandom().nextInt(pandas.size()));
        var pos = randPanda.position().offsetRandom(entity.getRandom(), 20);
        if (stand == 0) {
            if (entity.getNavigation().isDone()) {
                if (entity.getRandom().nextFloat() < 0.75) {
                    entity.getNavigation().moveTo(pos.x, pos.y, pos.z, 3);
                } else {
                    entity.level().broadcastEntityEvent(entity, (byte) 8);
                    stand = 16;
                }
            }
        } else {
            stand--;
        }
    }
}
