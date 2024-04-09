package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.OrangutanModel;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.OrangutanBabyRidePacket;
import io.github.itskillerluc.recrafted_creatures.networking.packets.ScareOrangutanPacket;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.Tags;
import io.github.itskillerluc.recrafted_creatures.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Orangutan extends Animal implements NeutralMob, Animatable<OrangutanModel> {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Orangutan.class, EntityDataSerializers.BYTE);

    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "orangutan");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> OrangutanModel.createStateMap(getAnimation()));
    public static final EntityDataAccessor<Boolean> ON_BACK = SynchedEntityData.defineId(Orangutan.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Orangutan.class, EntityDataSerializers.BOOLEAN);
    public int curiosity;
    public int missingMommy;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;
    public int scared = 0;
    private int pickupCooldown;
    public Orangutan(EntityType<? extends Orangutan> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new OrangutanMoveControl();
        setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ON_BACK, false);
        entityData.define(SLEEPING, false);
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new WallClimberNavigation(this, pLevel) {
            @Override
            public boolean isStableDestination(BlockPos pPos) {
                return true;
            }
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (!getMainHandItem().isEmpty()) {
            navigation.stop();
            setSpeed(0);
        }
        if (pickupCooldown > 0) pickupCooldown--;
        if (scared > 0) scared--;
        if (this.isBaby()) {
            if (entityData.get(ON_BACK)) {
                curiosity++;
                if (curiosity > missingMommy) {
                    if (getVehicle() != null && getVehicle().onGround()) {
                        entityData.set(ON_BACK, false);
                        stopRiding();
                        curiosity = random.nextInt(2000, 8000);
                        missingMommy = 0;
                    }
                }
            } else {
                missingMommy++;
            }
        }

        if (level().isClientSide()) {
            animateWhen("idle", !isMoving(this) && onGround() && !isSleeping());
            animateWhen("sleep", isSleeping());
            animateWhen("climb", isClimbing() && isMoving(this));
            animateWhen("tree_jump", !onGround());
            animateWhen("trade", !getMainHandItem().isEmpty());
        } else {
            this.setClimbing(this.horizontalCollision);
        }
    }

    @Override
    public void swing(InteractionHand pHand, boolean pUpdateSelf) {
        replayAnimation(random.nextBoolean() ? "swat" : "swat_flipped");
        super.swing(pHand, pUpdateSelf);
    }

    public boolean onClimbable() {
        return this.isClimbing();
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean pClimbing) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pClimbing) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setDiscardFriction(!onGround());
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);

        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return EntityRegistry.ORANGUTAN.get().create(pLevel);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.addPersistentAngerSaveData(pCompound);
        pCompound.putBoolean("isSleeping", isSleeping());
    }
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.readPersistentAngerSaveData(this.level(), pCompound);
        setSleeping(pCompound.getBoolean("isSleeping"));
    }

    class OrangutanMoveControl extends MoveControl {
        public OrangutanMoveControl() {
            super(Orangutan.this);
        }

        @Override
        public void tick() {
            if (Orangutan.this.canMove()) {
                super.tick();
            }
        }
    }

    boolean canMove() {
        return !this.isSleeping() && getMainHandItem().isEmpty();
    }

    @Override
    public boolean isSleeping() {
        return entityData.get(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        entityData.set(SLEEPING, sleeping);
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public void setRemainingPersistentAngerTime(int pTime) {
        this.remainingPersistentAngerTime = pTime;
    }

    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    public void setPersistentAngerTarget(@javax.annotation.Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }

    @javax.annotation.Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.FOLLOW_RANGE, 40);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new OrangutanGoal(new FloatGoal(this)));
        this.goalSelector.addGoal(2, new OrangutanGoal(new BreedGoal(this, 1.0D, Orangutan.class)));
        this.goalSelector.addGoal(3, new OrangutanGoal(new ClimbOnBackGoal(7, 2)));
        this.goalSelector.addGoal(3, new OrangutanGoal(new SleepGoal(this, 1, 20, 20)));
        this.goalSelector.addGoal(6, new OrangutanGoal(new WaterAvoidingRandomStrollGoal(this, 0.7D)));
        this.goalSelector.addGoal(7, new OrangutanGoal(new LookAtPlayerGoal(this, Player.class, 6.0F)));
        this.goalSelector.addGoal(9, new BarterGoal());
        this.goalSelector.addGoal(8, new OrangutanGoal(new RandomLookAroundGoal(this)));
        this.goalSelector.addGoal(2, new OrangutanGoal(new MeleeAttackGoal(this, 0.9f, true) {
            @Override
            protected double getAttackReachSqr(@NotNull LivingEntity pAttackTarget) {
                return super.getAttackReachSqr(pAttackTarget) / 2;
            }

            @Override
            protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                double d0 = this.getAttackReachSqr(pEnemy);
                if (pDistToEnemySqr <= d0 && getTicksUntilNextAttack() <= 0) {
                    this.resetAttackCooldown();
                    if (random.nextFloat() > 0.5f) {
                        this.mob.swing(InteractionHand.MAIN_HAND);
                        this.mob.doHurtTarget(pEnemy);
                    } else {
                        this.mob.swing(InteractionHand.MAIN_HAND);
                        this.mob.doHurtTarget(pEnemy);
                        pEnemy.knockback(2, this.mob.getX() - pEnemy.getX(), this.mob.getZ() - pEnemy.getZ());

                    }
                }


            }
        }));
        this.goalSelector.addGoal(3, new OrangutanGoal(new NavigationGoal()));
        this.targetSelector.addGoal(4, new OrangutanGoal(new ResetUniversalAngerTargetGoal<>(this, false)));
        this.targetSelector.addGoal(2, new OrangutanGoal(new HurtByTargetGoal(this)));
    }

    private class OrangutanGoal extends Goal {
        Goal goal;

        public OrangutanGoal(Goal goal) {
            this.goal = goal;
        }

        @Override
        public boolean canUse() {
            return Orangutan.this.getMainHandItem().isEmpty() && goal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return goal.canContinueToUse();
        }

        @Override
        public boolean isInterruptable() {
            return goal.isInterruptable();
        }

        @Override
        public void start() {
            goal.start();
        }

        @Override
        public void stop() {
            goal.stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return goal.requiresUpdateEveryTick();
        }

        @Override
        public void tick() {
            goal.tick();
        }

        @Override
        public void setFlags(EnumSet<Flag> pFlagSet) {
            goal.setFlags(pFlagSet);
        }

        @Override
        public String toString() {
            return goal.toString();
        }

        @Override
        public EnumSet<Flag> getFlags() {
            return goal.getFlags();
        }
    }

    class BarterGoal extends Goal {
        static final ResourceLocation LOOT = new ResourceLocation(RecraftedCreatures.MODID, "gameplay/orangutan_barter");

        @Override
        public boolean canUse() {
            return !getMainHandItem().isEmpty() && random.nextFloat() > 0.9f;
        }

        @Override
        public void start() {
            pickupCooldown = 200;
            if (getMainHandItem().is(Tags.ORANGUTAN_BARTERING)) {
                getMainHandItem().shrink(1);
                LootTable loottable = level().getServer().getLootData().getLootTable(LOOT);
                List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel)level())).withParameter(LootContextParams.THIS_ENTITY, Orangutan.this).create(LootContextParamSets.PIGLIN_BARTER));
                for (ItemStack itemStack : list) {
                    spawnAtLocation(itemStack);
                }
                level().broadcastEntityEvent(Orangutan.this, (byte)1);
            } else {
                spawnAtLocation(getMainHandItem());
                setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                level().broadcastEntityEvent(Orangutan.this, (byte)2);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte pId) {
        super.handleEntityEvent(pId);
        if (pId == 1) {
            replayAnimation("trade_accept");
        } else if (pId == 2) {
            replayAnimation("trade_decline");
        }
    }

    class SleepGoal extends MoveToBlockGoal {
        public SleepGoal(PathfinderMob pMob, double pSpeedModifier, int pSearchRange, int verticalSearch) {
            super(pMob, pSpeedModifier, pSearchRange, verticalSearch);
        }

        @Override
        public boolean canUse() {
            if (level().isDay() || scared > 0) {
                Orangutan.this.setSleeping(false);
                return false;
            } else {
                return super.canUse();
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (level().isDay()  || scared > 0) {
                stop();
                setSleeping(false);
                return;
            }

            if ((isReachedTarget() || Orangutan.this.onGround() && Orangutan.this.getBlockStateOn().is(BlockTags.LEAVES)) && scared == 0) {
                Orangutan.this.setJumping(false);
                Orangutan.this.setSleeping(true);
                Orangutan.this.getNavigation().stop();
                Orangutan.this.getMoveControl().setWantedPosition(Orangutan.this.getX(), Orangutan.this.getY(), Orangutan.this.getZ(), 0.0D);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && level().isDay() && scared == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
            return !pLevel.isEmptyBlock(pPos.above())
                    && pLevel.getBlockState(pPos.north()).is(BlockTags.LEAVES)
                    && pLevel.getBlockState(pPos).is(BlockTags.LEAVES)
                    && pLevel.getBlockState(pPos.east()).is(BlockTags.LEAVES)
                    && pLevel.getBlockState(pPos.south()).is(BlockTags.LEAVES)
                    && pLevel.getBlockState(pPos.west()).is(BlockTags.LEAVES);
        }
    }

    @Override
    public ResourceLocation getModelLocation() {
        return LOCATION;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() != null) {
            scared = 200;
            NetworkChannel.CHANNEL.sendToServer(new ScareOrangutanPacket(getUUID(), 200));
            Vec3 posAway = LandRandomPos.getPosAway(this, 10, 5, pSource.getEntity().position());
            if (posAway != null) {
                navigation.moveTo(posAway.x(), posAway.y(), posAway.z(), 1);
            }
        }
        return super.hurt(pSource, pAmount);
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
        return Optional.ofNullable(animations.get().get("animation.orangutan." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    private class NavigationGoal extends Goal {

        @Override
        public boolean canUse() {
            return onGround() && navigation.isDone() && getRandom().nextFloat() > 0.5f;
        }

        @Override
        public void start() {
            var random = getRandom().nextFloat();
            if (random > 0.8f) {
                stroll();
            } else if (random < 0.2f) {
                jumpDown();
            } else {
                jumpUp();
            }
        }

        private void jumpUp() {
            var pos = DefaultRandomPos.getPos(Orangutan.this, 10, 40);
            if (pos == null) return;
            pos = pos.add(0, 10, 0);
            actuallyJump(pos);
        }

        private void jumpDown() {
            var pos = DefaultRandomPos.getPos(Orangutan.this, 10, 1);
            if (pos == null) return;
            actuallyJump(pos);
        }

        private void actuallyJump(Vec3 pos) {
            for (double i = pos.y(); i > -64; i--) {
                var pos2 = new BlockPos((int) pos.x(), (int) i, (int) pos.z());
                if (level().getBlockState(pos2).entityCanStandOn(level(), pos2, Orangutan.this)) {
                    var path = navigation.createPath(pos2, 1);
                    if (path != null && path.canReach() || pos2.getY() < Orangutan.this.position().y) return;
                    Vec3 velocity = optimalVelocity(pos2.getCenter());
                    if (velocity != null) {
                        Orangutan.this.setYRot(Orangutan.this.yBodyRot);
                        double d0 = velocity.length();
                        double d1 = d0 + Orangutan.this.getJumpBoostPower();
                        Orangutan.this.setDeltaMovement(velocity.scale(d1 / d0));
                    }
                }
            }
        }

        private void stroll() {
            var pos = LandRandomPos.getPos(Orangutan.this, 10, 7);
            if (pos != null) {
                navigation.moveTo(pos.x(), pos.y(), pos.z(), 1);
            }
        }
        private boolean isValidAngle(int angle, Vec3 start, Vec3 end) {
            Vec3 hypotenuse = new Vec3(end.x(), end.y(), end.z()).subtract(new Vec3(start.x(), start.y(), start.z()));
            Vec3 adjacent = new Vec3(hypotenuse.x(), 0, hypotenuse.z());
            double startEndAngle = Math.toDegrees(Math.acos(adjacent.length() / hypotenuse.length()));
            return start.y() > end.y() ? -startEndAngle < angle : startEndAngle < angle;
        }

        private Vec3 optimalVelocity(Vec3 target) {
            int[] angles = new int[] {-85, -75, -60, -45, -30, -15, 0, 15, 30, 45, 60, 75, 85};
            return Arrays.stream(angles)
                    .filter(angle -> isValidAngle(angle, position(), target))
                    .mapToObj(angle -> Util.getJumpVector(Orangutan.this, target, angle, 5))
                    .filter(Objects::nonNull)
                    .min(Comparator.comparingDouble(Vec3::length))
                    .orElse(null);
        }
    }

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        if (pickupCooldown > 0) return false;
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(equipmentSlot).isEmpty()) {
            return false;
        } else {
            return equipmentSlot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
        }
    }

    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return pickupCooldown <= 0 && super.wantsToPickUp(pStack);
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        return true;
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            this.onItemPickup(pItemEntity);
            ItemStack itemstack = removeOneItemFromItemEntity(pItemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(pItemEntity, itemstack.getCount());
        }
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        ItemStack itemstack1 = itemstack.split(1);
        if (itemstack.isEmpty()) {
            pItemEntity.discard();
        } else {
            pItemEntity.setItem(itemstack);
        }

        return itemstack1;
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return 0;
    }

    private class ClimbOnBackGoal extends Goal {
        private final int range;
        private final int grabDistance;
        private Orangutan availableParent = null;

        public ClimbOnBackGoal(int range, int grabDistance) {
            this.range = range;
            this.grabDistance = grabDistance;
        }

        @Override
        public boolean canUse() {
            return Orangutan.this.isBaby() && !entityData.get(ON_BACK) && missingMommy > curiosity && isAvailableParentNear();
        }

        @Override
        public void start() {
            entityData.set(ON_BACK, true);
            startRiding(availableParent);
            missingMommy = random.nextInt(2000, 8000);
            curiosity = 0;
            NetworkChannel.CHANNEL.send(PacketDistributor.ALL.noArg(),  new OrangutanBabyRidePacket(availableParent.getId(), getId()));
        }

        private boolean isAvailableParentNear() {
            var entities = level().getEntitiesOfClass(Orangutan.class, AABB.ofSize(Orangutan.this.position(), range, range, range));
            for (Orangutan entity : entities) {
                if (!entity.isBaby() && distanceToSqr(entity) <= (grabDistance * grabDistance) && entity.canAddPassenger(Orangutan.this)) {
                    availableParent = entity;
                    return true;
                }
            }
            if (entities.size() > 0) {
                navigation.moveTo(entities.stream().min(Comparator.comparingDouble(Orangutan.this::distanceToSqr)).get(), 1);
            }
            return false;
        }
    }
}
