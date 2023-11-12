package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Giraffe extends TamableAnimal implements NeutralMob, Animatable<GiraffeModel>, PlayerRideable, Saddleable {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "giraffe");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private static final Ingredient FOOD_ITEMS = Ingredient.merge(List.of(Ingredient.of(Items.WHEAT, Items.HAY_BLOCK.asItem(), Items.CARROT, Items.GOLDEN_CARROT), Ingredient.of(ItemTags.LEAVES)));
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(Giraffe.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_TARGET = SynchedEntityData.defineId(Giraffe.class, EntityDataSerializers.BOOLEAN);
    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> GiraffeModel.createStateMap(getAnimation()));
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;

    public Giraffe(EntityType<? extends Giraffe> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.00D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SADDLED, false);
        entityData.define(HAS_TARGET, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D, Giraffe.class));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                if (getOwnerUUID() != null && mob.getLastHurtByMob() != null && mob.getLastHurtByMob().getUUID().compareTo(getOwnerUUID()) == 0){
                    return;
                }
                super.start();
            }
        });
    }

    public boolean getHasTarget() {
        return entityData.get(HAS_TARGET);
    }

    @Override
    public float getStepHeight() {
        return 1;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 2.2;
    }

    @Override
    public void positionRider(@NotNull Entity pPassenger, @NotNull MoveFunction function) {
        if (this.hasPassenger(pPassenger)) {
            float f3 = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
            float f = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
            pPassenger.setPos(this.getX() + (0.5 * f3), this.getY() + getPassengersRidingOffset(), this.getZ() - (0.5 * f));

        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (level().isClientSide()){
            return InteractionResult.FAIL;
        }
        if (getOwnerUUID() != null && this.getOwnerUUID().compareTo(pPlayer.getUUID()) == 0) {
            if (isSaddled() && pPlayer.getItemInHand(pHand).isEmpty() && pPlayer.isShiftKeyDown()) {
                setSaddled(false);
                pPlayer.setItemInHand(pHand, new ItemStack(Items.SADDLE));
                return InteractionResult.SUCCESS;
            } else if (!pPlayer.getItemInHand(pHand).is(Items.SADDLE) && pPlayer.isShiftKeyDown()){
                var itemStack = pPlayer.getItemInHand(pHand);
                if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                    if (!pPlayer.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    this.heal(3);
                }
                return InteractionResult.SUCCESS;
            } else if (!pPlayer.getItemInHand(pHand).is(Items.SADDLE)) {
                pPlayer.startRiding(this);
            }
        }
        if (this.getOwner() != null && this.getAge() == 0 && !this.isInLove() && isFood(pPlayer.getItemInHand(pHand))) {
            this.setInLove(pPlayer);
            pPlayer.getItemInHand(pHand).shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (this.getOwner() == null && isFood(pPlayer.getItemInHand(pHand))){
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
        return InteractionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (getTarget() != null) {
                entityData.set(HAS_TARGET, true);
            } else {
                entityData.set(HAS_TARGET, false);
            }
        }
        if (level().isClientSide()) {
            if (random.nextFloat() < 0.005) {
                replayAnimation("tongue");
            }
        } else {
            if (isInWater() && isVehicle()){
                ejectPassengers();
            }
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
            if (this.isVehicle() && isSaddled()) {
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
                    this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    super.travel(new Vec3(f, pTravelVector.y, f1));
                }
                this.calculateEntityAnimation(false);
                this.tryCheckInsideBlocks();
            } else {
                super.travel(pTravelVector);
            }
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack pStack) {
        return FOOD_ITEMS.test(pStack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.GIRAFFE.get().create(pLevel);
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
    public boolean isSaddleable() {
        return !isSaddled() && getOwnerUUID() != null;
    }

    public void setSaddled(boolean saddled){
        entityData.set(SADDLED, saddled);
    }

    @Override
    public void equipSaddle(@Nullable SoundSource pSource) {
        setSaddled(true);
        if (pSource != null) {
            this.level().playSound(null, this, this.getSaddleSoundEvent(), pSource, 0.5F, 1.0F);
        }
    }

    @Override
    public @NotNull SoundEvent getSaddleSoundEvent() {
        return SoundEvents.HORSE_SADDLE;
    }

    @Override
    public boolean isSaddled() {
        return entityData.get(SADDLED);
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
        return Optional.ofNullable(getAnimations().get().get("animation.giraffe." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("saddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setSaddled(pCompound.getBoolean("saddled"));
    }
}
