package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;
import java.util.Optional;

import static io.github.itskillerluc.recrafted_creatures.entity.RedPanda.SleepGoal.WAIT_TIME_BEFORE_SLEEP;

public class RedPanda extends TamableAnimal implements Animatable<RedPandaModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "red_panda");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    public static final EntityDataAccessor<Long> LAST_POSE_CHANGE_TICK = SynchedEntityData.defineId(RedPanda.class, EntityDataSerializers.LONG);
    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> GiraffeModel.createStateMap(getAnimation()));
    private boolean isTargeted = false;

    public RedPanda(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new RedPandaMoveControl();
        GroundPathNavigation groundPathNavigation = (GroundPathNavigation) this.getNavigation();
        groundPathNavigation.setCanFloat(true);
        groundPathNavigation.setCanWalkOverFences(true);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("isTargeted", isTargeted);
        pCompound.putLong("LastPoseTick", entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        long i = pCompound.getLong("LastPoseTick");
        if (i < 0L) {
            this.setPose(Pose.SLEEPING);
        }
        isTargeted = pCompound.getBoolean("isTargeted");
        this.resetLastPoseChangeTick(i);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LAST_POSE_CHANGE_TICK, 0L);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.4D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 5));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new SleepGoal());
        this.goalSelector.addGoal(5, new DistractEntityGoal<>(Monster.class));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 4f, 2, 2));

    }

    @Override
    public void setJumping(boolean pJumping) {
        super.setJumping(pJumping && !isRedPandaSleeping());
    }
    @Override
    public ResourceLocation getModelLocation() {
        return LOCATION;
    }

    @Override
    public DucAnimation getAnimation() {
        return ANIMATION;
    }
    @Override
    public Lazy<Map<String, AnimationState>> getAnimations() {
        return animations;
    }

    @Override
    public Optional<AnimationState> getAnimationState(String animation) {
        return Optional.ofNullable(getAnimations().get().get("animation.red_panda." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.RED_PANDA.get().create(pLevel);
    }

    class DistractEntityGoal<T extends Mob> extends Goal {
        final Class<T> target;
        protected Mob toAvoid;
        T entity;

        public DistractEntityGoal(Class<T> tClass) {
            target = tClass;
        }

        @Override
        public boolean canUse() {
            T entity = RedPanda.this.level().getNearestEntity(target, TargetingConditions.DEFAULT, RedPanda.this, 5, 5, 5, AABB.ofSize(RedPanda.this.position(), 5, 5, 5));
            this.entity = entity;
            isTargeted = entity != null;
            return getOwnerUUID() != null && entity != null;
        }

        @Override
        public void start() {
            super.start();
            isTargeted = true;
            var target = TargetingConditions.forCombat().range(4).selector(EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.toAvoid = RedPanda.this.level().getNearestEntity(RedPanda.this.level().getEntitiesOfClass(Mob.class, RedPanda.this.getBoundingBox().inflate(4, 3.0D, 4), (p_148078_) -> true), target, RedPanda.this, getX(), getY(), getZ());
            Path path = null;
            if (toAvoid == null) {
                return;
            }
            Vec3 vec3 = DefaultRandomPos.getPosAway(RedPanda.this, 16, 7, this.toAvoid.position());
            if (vec3 != null) {
                if (!(this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(RedPanda.this))) {
                    path = RedPanda.this.navigation.createPath(vec3.x, vec3.y, vec3.z, 0);
                }
            }
            if (path != null) {
                RedPanda.this.navigation.moveTo(path, 2);
            }

        }

        @Override
        public void tick() {
            super.tick();
            addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 2));
            if (!(entity.target instanceof RedPanda)){
                entity.target = RedPanda.this;
            }
        }
    }

    class SleepGoal extends Goal {
        protected boolean hasShelter() {
            BlockPos blockpos = new BlockPos((int) getX(), (int) getBoundingBox().maxY, (int) getZ());
            return !level().canSeeSky(blockpos) && getWalkTargetValue(blockpos) >= 0.0F;
        }

        public static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(140);
        public int countdown = random.nextInt(WAIT_TIME_BEFORE_SLEEP);

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if (xxa == 0.0F && yya == 0.0F && zza == 0.0F) {
                return !RedPanda.this.isInFluidType() && (this.canSleep() || isRedPandaSleeping()) && !isTargeted;
            } else {
                return false;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return this.canSleep();
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            } else {
                return !RedPanda.this.isInFluidType() && level().isDay() && this.hasShelter() && !isInPowderSnow && !isInSittingPose() && !isTargeted;
            }
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.countdown = random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            RedPanda.this.wakeUp();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            if (!isRedPandaSleeping()) {
                fallAsleep();
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (level().isClientSide()){
            return InteractionResult.CONSUME;
        }
        if (getOwnerUUID() != null && this.getOwnerUUID().compareTo(pPlayer.getUUID()) == 0) {
            if (pPlayer.getItemInHand(pHand).is(Items.BAMBOO) && this.getHealth() < this.getMaxHealth()) {
                if (!pPlayer.getAbilities().instabuild) {
                    pPlayer.getItemInHand(pHand).shrink(1);
                }
                this.heal(3);
            } else {
                if (pPlayer.getItemInHand(pHand).is(Items.BAMBOO) && this.getAge() == 0 && !this.isInLove()) {
                    this.setInLove(pPlayer);
                    pPlayer.getItemInHand(pHand).shrink(1);
                    return InteractionResult.SUCCESS;
                }
                this.setOrderedToSit(!isOrderedToSit());
            }
            return InteractionResult.SUCCESS;
        } else if (this.getOwner() == null && pPlayer.getItemInHand(pHand).is(Items.BAMBOO)){
            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
                this.tame(pPlayer);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte)7);
                pPlayer.getItemInHand(pHand).shrink(1);
            } else {
                this.level().broadcastEntityEvent(this, (byte)6);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private void clampHeadRotationToBody(Entity p_265624_, float p_265541_) {
        float f = p_265624_.getYHeadRot();
        float f1 = Mth.wrapDegrees(this.yBodyRot - f);
        float f2 = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - f), -p_265541_, p_265541_);
        float f3 = f + f1 - f2;
        p_265624_.setYHeadRot(f3);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }

        if (this.refuseToMove()){
            this.clampHeadRotationToBody(this, 30.0F);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.resetLastPoseChangeTickToFullStand(pLevel.getLevel().getGameTime());
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    private void resetLastPoseChangeTickToFullStand(long p_265447_) {
        this.resetLastPoseChangeTick(Math.max(0L, p_265447_ - 30L - 1L));
    }
    private void setupAnimationStates() {
        if (this.isRedPandaVisuallySleeping()) {
            stopAnimation("waking_up");
            if (this.isVisuallySleeping()) {
                stopAnimation("sleep");
                playAnimation("falling_asleep");
            } else {
                playAnimation("sleep");
                stopAnimation("falling_asleep");
            }
        } else {
            stopAnimation("sleep");
            stopAnimation("falling_asleep");
            animateWhen("waking_up", this.isInPoseTransition() && this.getPoseTime() >= 0L);
            animateWhen("sit", hasPose(Pose.SITTING));
        }
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if (this.refuseToMove() && this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            pTravelVector = pTravelVector.multiply(0.0D, 1.0D, 0.0D);
        }

        super.travel(pTravelVector);
    }

    public boolean refuseToMove() {
        return this.isRedPandaSleeping() || this.isInPoseTransition();
    }

    @Override
    public void setInSittingPose(boolean pSitting) {
        super.setInSittingPose(pSitting);
        if (pSitting) {
            this.setPose(Pose.SITTING);
        } else {
            this.setPose(Pose.STANDING);
        }
    }

    public boolean isRedPandaSleeping() {
        return this.entityData.get(LAST_POSE_CHANGE_TICK) < 0L;
    }

    public boolean isRedPandaVisuallySleeping() {
        return this.getPoseTime() < 0L != this.isRedPandaSleeping();
    }

    public boolean isInPoseTransition() {
        long i = this.getPoseTime();
        return i < 30L;
    }

    private boolean isVisuallySleeping() {
        return this.isRedPandaSleeping() && this.getPoseTime() < 30L && this.getPoseTime() >= 0L;
    }

    public void fallAsleep() {
        if (!this.isRedPandaSleeping()) {
            this.setPose(Pose.SLEEPING);
            this.resetLastPoseChangeTick(-this.level().getGameTime());
        }
    }

    public void wakeUp() {
        if (this.isRedPandaSleeping()) {
            this.setPose(Pose.STANDING);
            this.resetLastPoseChangeTick(this.level().getGameTime());
        }
    }

    @VisibleForTesting
    public void resetLastPoseChangeTick(long lastPoseChangeTick) {
        this.entityData.set(LAST_POSE_CHANGE_TICK, lastPoseChangeTick);
    }

    public long getPoseTime() {
        return this.level().getGameTime() - Math.abs(this.entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new RedPandaBodyRotationControl(this);
    }

    class RedPandaBodyRotationControl extends BodyRotationControl {
        public RedPandaBodyRotationControl(RedPanda panda) {
            super(panda);
        }

        public void clientTick() {
            if (!RedPanda.this.refuseToMove()) {
                super.clientTick();
            }
        }
    }

    class RedPandaMoveControl extends MoveControl {
        public RedPandaMoveControl() {
            super(RedPanda.this);
        }

        public void tick() {
            if (this.operation == Operation.MOVE_TO && !RedPanda.this.isLeashed() && RedPanda.this.isRedPandaSleeping() && !RedPanda.this.isRedPandaSleeping() && !RedPanda.this.isInPoseTransition()) {
                RedPanda.this.wakeUp();
            }
            super.tick();
        }
    }
    @Override
    public void push(double pX, double pY, double pZ) {
        super.push(pX, pY, pZ);
        if (isRedPandaSleeping()) {
            var goal = goalSelector.getRunningGoals().filter(g -> g.getGoal() instanceof SleepGoal).findFirst();
            if (goal.isPresent()) {
                random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            }
            wakeUp();
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (isRedPandaSleeping()) {
            var goal = goalSelector.getRunningGoals().filter(g -> g.getGoal() instanceof SleepGoal).findFirst();
            if (goal.isPresent()) {
                random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            }
            wakeUp();
        }
        return super.hurt(pSource, pAmount);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundRegistry.RED_PANDA_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.RED_PANDA_DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.RED_PANDA_SOUND.get();
    }
}
