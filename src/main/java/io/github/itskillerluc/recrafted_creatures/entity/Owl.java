package io.github.itskillerluc.recrafted_creatures.entity;

import com.google.common.graph.Network;
import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.advancement.OwlDeliveryTrigger;
import io.github.itskillerluc.recrafted_creatures.block.OwlEggBlock;
import io.github.itskillerluc.recrafted_creatures.client.models.ChameleonModel;
import io.github.itskillerluc.recrafted_creatures.client.models.OwlModel;
import io.github.itskillerluc.recrafted_creatures.entity.ai.EggLaying;
import io.github.itskillerluc.recrafted_creatures.entity.ai.EggLayingBreedGoal;
import io.github.itskillerluc.recrafted_creatures.entity.ai.LayEggGoal;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.ScareOwlPacket;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import io.github.itskillerluc.recrafted_creatures.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Owl extends TamableRCMob implements Animatable<ChameleonModel>, VariantHolder<Owl.OwlVariant>, FlyingAnimal, EggLaying {
    private static final EntityDataSerializer<OwlVariant> OWL_VARIANT_SERIALIZER = EntityDataSerializer.simpleEnum(OwlVariant.class);
    private static final EntityDataAccessor<Optional<UUID>> DELIVERY_TARGET = SynchedEntityData.defineId(Owl.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> SENDER = SynchedEntityData.defineId(Owl.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Owl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_DELIVER = SynchedEntityData.defineId(Owl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Owl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Owl.class, EntityDataSerializers.BOOLEAN);

    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "owl");
    private static final EntityDataAccessor<OwlVariant> VARIANT = SynchedEntityData.defineId(Owl.class, OWL_VARIANT_SERIALIZER);

    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> OwlModel.createStateMap(getAnimation()));
    int layEggCounter;
    public int scared = 0;


    static {
        EntityDataSerializers.registerSerializer(OWL_VARIANT_SERIALIZER);
    }


    public Owl(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new OwlMoveControl();
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.FLYING_SPEED, 0.4F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString("variant", getVariant().name());
        if (entityData.get(DELIVERY_TARGET).isPresent()) {
            pCompound.putUUID("deliveryTarget", entityData.get(DELIVERY_TARGET).get());
        }
        if (entityData.get(SENDER).isPresent()) {
            pCompound.putUUID("sender", entityData.get(SENDER).get());
        }
        pCompound.putBoolean("isSleeping", isSleeping());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setVariant(OwlVariant.valueOf(pCompound.getString("variant")));
        entityData.set(DELIVERY_TARGET, pCompound.hasUUID("deliveryTarget") ? Optional.of(pCompound.getUUID("deliveryTarget")) : Optional.empty());
        entityData.set(SENDER, pCompound.hasUUID("sender") ? Optional.of(pCompound.getUUID("sender")) : Optional.empty());
        setSleeping(pCompound.getBoolean("isSleeping"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        var biome = pLevel.getBiome(this.blockPosition()).get();
        setVariant(biome.coldEnoughToSnow(this.blockPosition()) ? OwlVariant.SNOWY : OwlVariant.HORNED);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, OwlVariant.HORNED);
        entityData.define(DELIVERY_TARGET, Optional.empty());
        entityData.define(SLEEPING, false);
        entityData.define(HAS_EGG, false);
        entityData.define(LAYING_EGG, false);
        entityData.define(CAN_DELIVER, false);
        entityData.define(SENDER, Optional.empty());
    }

    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, createOwlGoal(new PanicGoal(this, 1.25D)));
        this.goalSelector.addGoal(0, createOwlGoal(new FloatGoal(this)));
        this.goalSelector.addGoal(2, createOwlGoal(new SitWhenOrderedToGoal(this)));
        this.goalSelector.addGoal(2, createOwlGoal(new FollowOwnerGoal(this, 1.0D, 5.0F, 1.0F, true) {
            @Override
            public boolean canUse() {
                return super.canUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }
        }));
        this.goalSelector.addGoal(3, createOwlGoal(new SleepGoal(this, 1, 20, 20)));
        this.goalSelector.addGoal(3, createOwlGoal(new EggLayingBreedGoal<>(this, 1.0D)));
        this.goalSelector.addGoal(4, createOwlGoal(new LayEggGoal<>(this, 1, BlockRegistry.OWL_EGG_BLOCK.get().defaultBlockState().setValue(OwlEggBlock.EGGS, random.nextInt(OwlEggBlock.MAX_EGGS) + OwlEggBlock.MIN_EGGS), (level, pos) -> level.getBlockState(pos).is(BlockTags.LEAVES))));
        this.goalSelector.addGoal(4, createOwlGoal(new OwlWanderGoal(this, 1.0D)));
        this.goalSelector.addGoal(4, createOwlGoal(new MeleeAttackGoal(this, 1, false)));
        this.goalSelector.addGoal(7, createOwlGoal(new BreedGoal(this, 1.0D)));
        this.goalSelector.addGoal(10, createOwlGoal(new LookAtPlayerGoal(this, Player.class, 8)));
        this.goalSelector.addGoal(10, createOwlGoal(new RandomLookAroundGoal(this)));

        this.targetSelector.addGoal(1, createOwlGoal(new NearestAttackableTargetGoal<>(this, Rabbit.class, true)));
    }

    protected <T extends Goal> OwlGoal<T> createOwlGoal(T goal) {
        return new OwlGoal<>(goal, this);
    }

    @Override
    public void setInLove(int pInLove) {
        super.setInLoveTime(pInLove);
    }
    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return 0;
    }

    @Override
    public ResourceLocation getModelLocation() {
        return LOCATION;
    }

    @Override
    public void tick() {
        super.tick();
        if (scared > 0) scared--;
        if (navigation.isDone() && getDeliveryTarget().isPresent() && !level().isClientSide()) {
            if (getDeliveryTarget().get().distanceToSqr(this) < 4) {
                if (isReturning()) {
                    finishDelivery();
                } else {
                    releaseAndReturn();
                }
            } else if (getDeliveryTarget().get().distanceToSqr(this) < 400) {
                var pos = getDeliveryTarget().get().position();
                navigation.moveTo(pos.x(), pos.y() + 1, pos.z(), 1);
            } else {
                if (entityData.get(SENDER).isPresent() && level().getServer().getPlayerList().getPlayer(entityData.get(SENDER).get()).distanceToSqr(this) < 225) {
                    var playerPos = level().getServer().getPlayerList().getPlayer(entityData.get(SENDER).get()).position();
                    navigation.moveTo(playerPos.x() + random.nextInt(-5, 5), playerPos.y() + 25 + random.nextInt(0, 10), playerPos.z() + random.nextInt(-5, 5), 1);
                } else {
                    teleportNearTarget();
                }
            }
        }
        if (random.nextFloat() > 0.999f && this.onGround() && level().isClientSide()) {
            replayAnimation("neck_rotate");
        }
        if (level().isClientSide()) {
            animateWhen("holding", hasItemInSlot(EquipmentSlot.MAINHAND));

            animateWhen("idle", !isMoving(this) && onGround() && !isSleeping());
            animateWhen("sleep", isSleeping());

            animateWhen("fly", isFlying() && isMoving(this));
        }
    }

    public Optional<Player> getDeliveryTarget() {
        return entityData.get(DELIVERY_TARGET).map(value -> level().getPlayerByUUID(value));
    }

    public boolean isSleeping() {
        return entityData.get(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        entityData.set(SLEEPING, sleeping);
    }

    public boolean isReturning() {
        return getDeliveryTarget().isPresent() && getDeliveryTarget().get().getUUID().equals(getOwnerUUID());
    }

    protected void finishDelivery() {
        navigation.stop();
        entityData.set(DELIVERY_TARGET, Optional.empty());
    }

    protected void releaseAndReturn() {
        var item = getItemBySlot(EquipmentSlot.MAINHAND);
        this.spawnAtLocation(item);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        entityData.set(SENDER, entityData.get(DELIVERY_TARGET));
        entityData.set(DELIVERY_TARGET, Optional.ofNullable(getOwnerUUID()));
    }

    protected void teleportNearTarget() {
        level().addParticle(ParticleTypes.LARGE_SMOKE, position().x, position().y, position().z, 0, 0, 0);
        playSound(SoundEvents.POWDER_SNOW_FALL);
        var pos = getDeliveryTarget().get().position();
        teleportTo(pos.x() + random.nextInt(-5, 5), pos.y() + 10 + random.nextInt(0, 5), pos.z() + random.nextInt(-5, 5));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        calculateFlapping();
    }

    private void calculateFlapping() {
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0.0D) {
            this.setDeltaMovement(vec3.multiply(1.0D, target != null || entityData.get(DELIVERY_TARGET).isPresent() ? 0.9D : 0.6D, 1.0D));
            if (level().isClientSide()) {
                animateWhen("grab_swoop", target != null || entityData.get(DELIVERY_TARGET).isPresent());
            }
        }
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
        return Optional.ofNullable(getAnimations().get().get("animation.owl." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.OWL.get().create(pLevel);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundRegistry.OWL_DEATH.get();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() != null) {
            scared = 200;
            NetworkChannel.CHANNEL.sendToServer(new ScareOwlPacket(getUUID(), 200));
            Vec3 posAway = LandRandomPos.getPosAway(this, 10, 5, pSource.getEntity().position());
            if (posAway != null) {
                navigation.moveTo(posAway.x(), posAway.y(), posAway.z(), 1);
            }
        }
        return super.hurt(pSource, pAmount);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return random.nextBoolean() ? SoundRegistry.OWL_AMBIENCE.get() : SoundRegistry.OWL_HOOT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2F;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.OWL_DEATH.get();
    }

    @Override
    public void setVariant(OwlVariant pVariant) {
        entityData.set(VARIANT, pVariant);
    }

    @Override
    public OwlVariant getVariant() {
        return entityData.get(VARIANT);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        if (isReturning()) return false;
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(equipmentSlot).isEmpty()) {
            return false;
        } else {
            return equipmentSlot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
        }
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            this.onItemPickup(pItemEntity);
            ItemStack itemstack = pItemEntity.getItem();
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
        }
    }

    public void sendItem(UUID player) {
        OwlDeliveryTrigger.INSTANCE.trigger(getServer().getPlayerList().getPlayer(player), getItemBySlot(EquipmentSlot.MAINHAND));
        entityData.set(DELIVERY_TARGET, Optional.of(player));
        entityData.set(SENDER, Optional.ofNullable(getOwnerUUID()));
        navigation.stop();
        entityData.set(CAN_DELIVER, false);
    }

    boolean canMove() {
        return !this.isSleeping();
    }

    public boolean canDeliver() {
        return entityData.get(CAN_DELIVER);
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (pPlayer.isCrouching() && level().isClientSide() && Objects.equals(getOwnerUUID(), pPlayer.getUUID())) {
            ClientHelper.openDeliveryScreen(this);
            return InteractionResult.SUCCESS;
        }
        if (itemstack.is(ItemRegistry.OWL_ENVELOPE.get()) && Objects.equals(getOwnerUUID(), pPlayer.getUUID())) {
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            pPlayer.getItemInHand(pHand).shrink(1);
            entityData.set(CAN_DELIVER, true);
        }
        if (itemstack.isEmpty() && !pPlayer.isCrouching() && this.getItemBySlot(EquipmentSlot.MAINHAND).is(ItemRegistry.OWL_ENVELOPE.get())  && Objects.equals(getOwnerUUID(), pPlayer.getUUID())) {
            var stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            pPlayer.setItemInHand(pHand, stack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            entityData.set(CAN_DELIVER, false);
        }
        if (itemstack.is(Items.COOKED_RABBIT)) {
            if (level().isClientSide()) {
                replayAnimation("eating");
            }
            if (this.getHealth() < this.getMaxHealth()) {
                if (!pPlayer.getAbilities().instabuild) {
                    pPlayer.getItemInHand(pHand).shrink(1);
                }
                this.heal(3);
                return InteractionResult.SUCCESS;
            } else {
                if (this.getAge() == 0 && !this.isInLove()) {
                    this.setInLove(pPlayer);
                    pPlayer.getItemInHand(pHand).shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!this.isTame() && itemstack.is(Items.RABBIT)) {
            if (level().isClientSide()) {
                replayAnimation("eating");
            }
            if (!pPlayer.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            if (!this.isSilent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            }

            if (!this.level().isClientSide) {
                if (this.random.nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
                    this.tame(pPlayer);
                    this.level().broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6);
                }
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    @Override
    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    @Override
    public void setHasEgg(boolean hasEgg) {
        this.entityData.set(HAS_EGG, hasEgg);
    }

    @Override
    public int getEggLayCounter() {
        return layEggCounter;
    }

    @Override
    public void setEggLayCounter(int count) {
        layEggCounter = count;
    }

    @Override
    public void setEggLaying(boolean isLayingEgg) {
        this.entityData.set(LAYING_EGG, isLayingEgg);
    }

    @Override
    public boolean getEggLaying() {
        return entityData.get(LAYING_EGG);
    }

    static class OwlWanderGoal extends WaterAvoidingRandomFlyingGoal {
        public OwlWanderGoal(PathfinderMob p_186224_, double p_186225_) {
            super(p_186224_, p_186225_);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && mob.getEntityData().get(DELIVERY_TARGET).isEmpty();
        }

        @javax.annotation.Nullable
        protected Vec3 getPosition() {
            Vec3 vec3 = null;
            if (this.mob.isInWater()) {
                vec3 = LandRandomPos.getPos(this.mob, 15, 15);
            }

            if (this.mob.getRandom().nextFloat() >= this.probability) {
                vec3 = this.getTreePos();
            }

            return vec3 == null ? super.getPosition() : vec3;
        }

        @javax.annotation.Nullable
        private Vec3 getTreePos() {
            BlockPos blockpos = this.mob.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

            for(BlockPos blockpos1 : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 3.0D), Mth.floor(this.mob.getY() - 6.0D), Mth.floor(this.mob.getZ() - 3.0D), Mth.floor(this.mob.getX() + 3.0D), Mth.floor(this.mob.getY() + 6.0D), Mth.floor(this.mob.getZ() + 3.0D))) {
                if (!blockpos.equals(blockpos1)) {
                    BlockState blockstate = this.mob.level().getBlockState(blockpos$mutableblockpos1.setWithOffset(blockpos1, Direction.DOWN));
                    boolean flag = blockstate.getBlock() instanceof LeavesBlock || blockstate.is(BlockTags.LOGS);
                    if (flag && this.mob.level().isEmptyBlock(blockpos1) && this.mob.level().isEmptyBlock(blockpos$mutableblockpos.setWithOffset(blockpos1, Direction.UP))) {
                        return Vec3.atBottomCenterOf(blockpos1);
                    }
                }
            }

            return null;
        }
    }

    private static class OwlGoal<T extends Goal> extends Goal {
        private final T goal;
        private final Owl owl;

        public OwlGoal(T goal, Owl owl) {
            this.goal = goal;
            this.owl = owl;
        }

        @Override
        public boolean canUse() {
            return owl.entityData.get(DELIVERY_TARGET).isEmpty() && goal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return goal.canContinueToUse();
        }

        public boolean isInterruptable() {
            return goal.isInterruptable();
        }

        public void start() {
            goal.start();
        }

        public void stop() {
            goal.stop();
        }

        public boolean requiresUpdateEveryTick() {
            return goal.requiresUpdateEveryTick();
        }

        public void tick() {
            goal.tick();
        }

        public void setFlags(EnumSet<Flag> pFlagSet) {
            goal.setFlags(pFlagSet);
        }

        public String toString() {
            return goal.toString();
        }

        public EnumSet<Goal.Flag> getFlags() {
            return goal.getFlags();
        }
    }

    class SleepGoal extends MoveToBlockGoal {
        public SleepGoal(PathfinderMob pMob, double pSpeedModifier, int pSearchRange, int verticalSearch) {
            super(pMob, pSpeedModifier, pSearchRange, verticalSearch);
        }

        @Override
        public boolean canUse() {
            if (level().isNight() || scared > 0) {
                Owl.this.setSleeping(false);
                return false;
            } else {
                return super.canUse();
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (level().isNight() || scared > 0) {
                stop();
                setSleeping(false);
                return;
            }

            if ((isReachedTarget() || Owl.this.onGround() && Owl.this.getBlockStateOn().is(BlockTags.LEAVES)) && scared == 0) {
                Owl.this.setJumping(false);
                Owl.this.setSleeping(true);
                Owl.this.getNavigation().stop();
                Owl.this.getMoveControl().setWantedPosition(Owl.this.getX(), Owl.this.getY(), Owl.this.getZ(), 0.0D);
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

    class OwlMoveControl extends FlyingMoveControl {
        public OwlMoveControl() {
            super(Owl.this, 5, false);
        }

        public void tick() {
            if (Owl.this.canMove()) {
                super.tick();
            }

        }
    }

    public enum OwlVariant {
        HORNED(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/owl_horned_awake.png"), new ResourceLocation(RecraftedCreatures.MODID,"textures/entity/owl_horned_asleep.png")),
        SNOWY(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/owl_snowy_awake.png"), new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/owl_snowy_asleep.png"));
        public final ResourceLocation awake;
        public final ResourceLocation sleeping;
        private static final ResourceLocation ROWLET_ASLEEP = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/rowlet_asleep.png");
        private static final ResourceLocation ROWLET_AWAKE = new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/rowlet_awake.png");


        OwlVariant(ResourceLocation awake, ResourceLocation sleeping) {
            this.awake = awake;
            this.sleeping = sleeping;
        }

        public ResourceLocation getTexture(Owl owl) {
            if (owl.hasCustomName()) {
                String s = ChatFormatting.stripFormatting(owl.getName().getString());
                if ("Rowlet".equals(s)) {
                    return owl.isSleeping() ? ROWLET_ASLEEP : ROWLET_AWAKE;
                }
            }
            return owl.isSleeping() ? sleeping : awake;
        }
    }
}
