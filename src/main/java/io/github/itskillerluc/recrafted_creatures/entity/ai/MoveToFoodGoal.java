package io.github.itskillerluc.recrafted_creatures.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class MoveToFoodGoal <T extends PathfinderMob & FoodSearching> extends Goal {
    private final T entity;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float within;
    private final Predicate<ItemEntity> predicate;

    public MoveToFoodGoal(T entity, double pSpeedModifier, float pWithin, Predicate<ItemEntity> predicate) {
        this.entity = entity;
        this.speedModifier = pSpeedModifier;
        this.within = pWithin;
        this.predicate = predicate;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (entity.getItemTarget() == null) {
            var list = entity.level().getEntitiesOfClass(ItemEntity.class, AABB.ofSize(entity.position(), 10, 3, 10));
            list.sort(Comparator.comparingDouble(entity::distanceToSqr));
            Optional<ItemEntity> optional = list.stream().filter(predicate).filter((p_26706_) -> entity.wantsToPickUp(p_26706_.getItem())).filter((p_26701_) -> p_26701_.closerThan(entity, 32.0D)).filter(entity::hasLineOfSight).findFirst();
            optional.ifPresent(entity::setItemTarget);
            return entity.getItemTarget() != null;
        } else if (entity.getItemTarget().distanceToSqr(this.entity) > (double) (this.within * this.within)) {
            return false;
        } else {
            Vec3 vec3 = DefaultRandomPos.getPosTowards(this.entity, 16, 7, entity.getItemTarget().position(), (double) ((float) Math.PI / 2F));
            if (vec3 == null) {
                return false;
            } else {
                this.wantedX = vec3.x;
                this.wantedY = vec3.y;
                this.wantedZ = vec3.z;
                return true;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return entity.getItemTarget() != null && entity.getItemTarget().isAlive() && entity.getItemTarget().distanceToSqr(this.entity) < (double) (this.within * this.within);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        entity.setItemTarget(null);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        wantedX = entity.getItemTarget().getX();
        wantedY = entity.getItemTarget().getY();
        wantedZ = entity.getItemTarget().getZ();

        this.entity.getNavigation().moveTo(this.entity.getNavigation().createPath(new BlockPos((int) this.wantedX, (int) this.wantedY, (int) this.wantedZ), 0), 1d) ;
    }
}
