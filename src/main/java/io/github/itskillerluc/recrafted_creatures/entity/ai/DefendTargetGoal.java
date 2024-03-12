package io.github.itskillerluc.recrafted_creatures.entity.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DefendTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final Supplier<Vec3> position;
    private BooleanSupplier shouldDefend = null;
    private final int range;

    public DefendTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, Supplier<Vec3> positionSupplier, int range) {
        super(pMob, pTargetType, pMustSee);
        this.position = positionSupplier;
        this.range = range;
    }

    public DefendTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, Predicate<LivingEntity> pTargetPredicate, Supplier<Vec3> position, int range) {
        super(pMob, pTargetType, pMustSee, pTargetPredicate);
        this.position = position;
        this.range = range;
    }

    public DefendTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, boolean pMustReach, Supplier<Vec3> position, int range) {
        super(pMob, pTargetType, pMustSee, pMustReach);
        this.position = position;
        this.range = range;
    }

    public DefendTargetGoal(Mob pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate, Supplier<Vec3> position, int range) {
        super(pMob, pTargetType, pRandomInterval, pMustSee, pMustReach, pTargetPredicate);
        this.position = position;
        this.range = range;
    }

    public DefendTargetGoal<T> shouldDefend(BooleanSupplier shouldDefend) {
        this.shouldDefend = shouldDefend;
        return this;
    }

    @Override
    public boolean canUse() {
        return (shouldDefend == null || shouldDefend.getAsBoolean()) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    protected void findTarget() {
        var pos = position.get();
        if (pos == null) return;
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(range), (p_148152_) -> true),
                    this.targetConditions, this.mob, pos.x(), pos.y(), pos.z());
        } else {
            this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, pos.x(), pos.y(), pos.z());
        }
    }

    @Override
    protected AABB getTargetSearchArea(double pTargetDistance) {
        return AABB.ofSize(position.get(), pTargetDistance, pTargetDistance, pTargetDistance);
    }
}
