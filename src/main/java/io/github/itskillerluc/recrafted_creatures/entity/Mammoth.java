package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.MammothModel;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Mammoth extends TamableRCMob implements NeutralMob, Animatable<MammothModel>, PlayerRideable {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "mammoth");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> MammothModel.createStateMap(getAnimation()));
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(Mammoth.class, EntityDataSerializers.INT);
    public Mammoth(EntityType<? extends Mammoth> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
        return !(pTarget instanceof OwnableEntity entity) || entity.getOwner() == null || !entity.getOwner().is(pOwner);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 50)
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, 0xFF0000);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("color", entityData.get(COLOR));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(COLOR, pCompound.getInt("color"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D, Mammoth.class){
            @Override
            public boolean canUse() {
                if (!this.animal.isInLove()) {
                    return false;
                } else {
                    this.partner = this.getFreePartner();
                    return this.partner != null;
                }
            }

            private Animal getFreePartner() {
                List<? extends Animal> list = this.level.getNearbyEntities(Mammoth.class, TargetingConditions.forNonCombat().range(16.0D).ignoreLineOfSight(), this.animal, this.animal.getBoundingBox().inflate(8.0D));
                double d0 = Double.MAX_VALUE;
                Animal animal = null;

                for(Animal animal1 : list) {
                    if (this.animal.canMate(animal1) && this.animal.distanceToSqr(animal1) < d0) {
                        animal = animal1;
                        d0 = this.animal.distanceToSqr(animal1);
                    }
                }

                return animal;
            }

        });
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return super.canUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.9f, true) {
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
                        level().broadcastEntityEvent(Mammoth.this, (byte) -4);
                    } else {
                        this.mob.swing(InteractionHand.MAIN_HAND);
                        this.mob.doHurtTarget(pEnemy);
                        level().broadcastEntityEvent(Mammoth.this, (byte)-6);
                        pEnemy.knockback(2, this.mob.getX() - pEnemy.getX(), this.mob.getZ() - pEnemy.getZ());

                    }
                }


            }
        });
        targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                if (getOwnerUUID() != null && mob.getLastHurtByMob() != null && mob.getLastHurtByMob().getUUID().compareTo(getOwnerUUID()) == 0){
                    return;
                }
                super.start();
            }
        });
        targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    @Override
    public float getStepHeight() {
        return 1;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 2;
    }

    @Override
    public void positionRider(@NotNull Entity pPassenger, @NotNull MoveFunction function) {
        if (this.hasPassenger(pPassenger)) {
            float f3 = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
            float f = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
            pPassenger.setPos(this.getX() + (0.7 * f3), this.getY() + getPassengersRidingOffset(), this.getZ() - (0.7 * f));

        }
    }

    public int getColor(){
        return entityData.get(COLOR);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (this.getOwner() != null && PotionUtils.getPotion(pPlayer.getItemInHand(pHand)) == Potions.WATER && !level().isClientSide()) {
            entityData.set(COLOR, 0xFF0000);
            pPlayer.setItemInHand(pHand, new ItemStack(Items.GLASS_BOTTLE));
            return InteractionResult.SUCCESS;
        }
        if (this.getOwner() != null && pPlayer.getItemInHand(pHand).is(Tags.Items.DYES) && !level().isClientSide()) {
            DyeItem item = ((DyeItem) pPlayer.getItemInHand(pHand).getItem());
            int existingColor = getColor();
            int dyeColor = item.getDyeColor().getTextColor();

            int resultColor = blendColors(existingColor, dyeColor);
            entityData.set(COLOR, resultColor);
            return InteractionResult.SUCCESS;
        }
        if (this.getOwner() != null && this.getAge() == 0 && !this.isInLove() && pPlayer.getItemInHand(pHand).is(Items.HAY_BLOCK) && !level().isClientSide()) {
            this.setInLove(pPlayer);
            pPlayer.getItemInHand(pHand).shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (this.isBaby() && isFood(pPlayer.getItemInHand(pHand)) && !level().isClientSide()) {
            pPlayer.getItemInHand(pHand).shrink(1);
            this.ageUp(getSpeedUpSecondsWhenFeeding(-getAge()), true);
            return InteractionResult.SUCCESS;
        }
        if (getOwnerUUID() != null && this.getOwnerUUID().compareTo(pPlayer.getUUID()) == 0 && !level().isClientSide()) {
            if (pPlayer.isShiftKeyDown()){
                var itemStack = pPlayer.getItemInHand(pHand);
                if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                    if (!pPlayer.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    this.heal(3);
                }
                return InteractionResult.SUCCESS;
            } else if (pHand == InteractionHand.MAIN_HAND && !isBaby()){
                pPlayer.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        }
        if (this.getOwner() == null && isFood(pPlayer.getItemInHand(pHand)) && !level().isClientSide()){
            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
                this.tame(pPlayer);
                this.navigation.stop();
                this.setTarget(null);
                this.entityData.set(COLOR, 0xFF0000);
                this.level().broadcastEntityEvent(this, (byte)7);
                pPlayer.getItemInHand(pHand).shrink(1);
            } else {
                this.level().broadcastEntityEvent(this, (byte)6);
                pPlayer.getItemInHand(pHand).shrink(1);
            }

            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(pPlayer, pHand);
    }

    private int blendColors(int baseColor, int addedColor) {
        int originalRed = baseColor >> 16 & 255;
        int originalGreen = baseColor >> 8 & 255;
        int originalBlue = baseColor & 255;

        int dyeRed = addedColor >> 16 & 255;
        int dyeGreen = addedColor >> 8 & 255;
        int dyeBlue = addedColor & 255;

        int diffRed = -dyeRed + originalRed;
        int diffGreen = -dyeGreen + originalGreen;
        int diffBlue = -dyeBlue + originalBlue;


        int resultRed = 255 - Math.max(0, 255 - originalRed + diffRed/Math.max(1, Math.abs(diffRed/16)));
        int resultGreen = 255 - Math.max(0, 255- originalGreen + diffGreen/Math.max(1, Math.abs(diffGreen/16)));
        int resultBlue = 255 - Math.max(0, 255 - originalBlue + diffBlue/Math.max(1, Math.abs(diffBlue/16)));

        return resultRed << 16 | resultGreen << 8 | resultBlue;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            animateWhen("idle", hasPose(Pose.STANDING));
        }
    }

    @Override
    protected void addPassenger(@NotNull Entity pPassenger) {
        super.addPassenger(pPassenger);
        if (!level().isClientSide() && pPassenger instanceof Player player && player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null){
            player.getAttribute(ForgeMod.BLOCK_REACH.get()).setBaseValue(player.getBlockReach() + (player.isCreative() ? 2.5 :3));
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity pPassenger) {
        super.removePassenger(pPassenger);
        if (pPassenger instanceof Player player && !level().isClientSide() && player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null){
            player.getAttribute(ForgeMod.BLOCK_REACH.get()).setBaseValue(player.getBlockReach() - (player.isCreative() ? 3.5 : 3));
        }
    }

    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity p_230268_1_) {
        Direction direction = this.getMotionDirection();
        if (direction.getAxis() != Direction.Axis.Y) {
            int[][] aint = DismountHelper.offsetsForDirection(direction);
            BlockPos blockpos = this.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

            for (Pose pose : p_230268_1_.getDismountPoses()) {
                AABB axisalignedbb = p_230268_1_.getLocalBoundsForPose(pose);

                for (int[] aint1 : aint) {
                    blockpos$mutable.set(blockpos.getX() + aint1[0], blockpos.getY(), blockpos.getZ() + aint1[1]);
                    double d0 = this.level().getBlockFloorHeight(blockpos$mutable);
                    if (DismountHelper.isBlockFloorValid(d0)) {
                        Vec3 vec3 = Vec3.upFromBottomCenterOf(blockpos$mutable, d0);
                        if (DismountHelper.canDismountTo(this.level(), p_230268_1_, axisalignedbb.move(vec3))) {
                            p_230268_1_.setPose(pose);
                            return vec3;
                        }
                    }
                }
            }

        }
        return super.getDismountLocationForPassenger(p_230268_1_);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof LivingEntity entity ? entity : null;
    }

    public void travel(@NotNull Vec3 pTravelVector) {
        if (this.isAlive()) {
            if (this.isVehicle() && isTame()) {
                LivingEntity livingentity = this.getControllingPassenger();
                this.setYRot(livingentity.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(livingentity.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;
                float f = livingentity.xxa * 0.5F;
                float f1 = livingentity.zza;
                if (f1 <= 0.0F) {
                    f1 *= 0.25F;
                }

                if (this.isControlledByLocalInstance()) {
                    this.setSpeed(0.2F);
                    super.travel(new Vec3(f, pTravelVector.y, f1));
                }
                this.calculateEntityAnimation(false);
                this.tryCheckInsideBlocks();
            } else {
                this.setSpeed(0.4F);
                super.travel(pTravelVector);
            }
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack pStack) {
        return pStack.is(Items.PUMPKIN) || pStack.is(Items.MELON);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        var entity = EntityRegistry.MAMMOTH.get().create(pLevel);
        if (((Mammoth) pOtherParent).isTame() && isTame()){
            entity.setTame(true);
            entity.setOwnerUUID(this.getOwnerUUID());
        }
        return entity;
    }
    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        remainingPersistentAngerTime = time;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
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
        return Optional.ofNullable(getAnimations().get().get("animation.mammoth." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundRegistry.MAMMOTH_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.MAMMOTH_DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return random.nextFloat() < 0.3 ? SoundRegistry.MAMMOTH_TRUMPET.get() : SoundRegistry.MAMMOTH_AMBIENCE.get();
    }

    @Override
    public void handleEntityEvent(byte pId) {

        if (pId == -4) {
            replayAnimation("attack");
        } else if (pId == -6){
            replayAnimation("stomp");
        } else {
            super.handleEntityEvent(pId);
        }
    }
}
