package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.ChameleonEggBlock;
import io.github.itskillerluc.recrafted_creatures.client.models.ChameleonModel;
import io.github.itskillerluc.recrafted_creatures.entity.ai.EggLaying;
import io.github.itskillerluc.recrafted_creatures.entity.ai.EggLayingBreedGoal;
import io.github.itskillerluc.recrafted_creatures.entity.ai.LayEggGoal;
import io.github.itskillerluc.recrafted_creatures.entity.ai.WallRandomPos;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Chameleon extends Animal implements Animatable<ChameleonModel>, EggLaying {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Chameleon.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Chameleon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Chameleon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(Chameleon.class, EntityDataSerializers.INT);
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "chameleon");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private static final int TIME_BEFORE_COLOR_CHANGE = 60;
    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> ChameleonModel.createStateMap(getAnimation()));
    int layEggCounter;

    public Chameleon(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(COLOR, 0x99C351);
        this.entityData.define(HAS_EGG, false);
        this.entityData.define(LAYING_EGG, false);
    }

    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    public void setHasEgg(boolean pHasEgg) {
        this.entityData.set(HAS_EGG, pHasEgg);
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
    public void setInLove(int pInLove) {
        super.setInLoveTime(pInLove);
    }

    @Override
    public void setEggLaying(boolean isLayingEgg) {
        this.layEggCounter = isLayingEgg ? 1 : 0;
        this.entityData.set(LAYING_EGG, isLayingEgg);
    }

    @Override
    public boolean getEggLaying() {
        return this.entityData.get(LAYING_EGG);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.entityData.set(COLOR, pCompound.getInt("color"));
        this.setHasEgg(pCompound.getBoolean("HasEgg"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("color", this.entityData.get(COLOR));
        pCompound.putBoolean("HasEgg", this.hasEgg());
    }

    protected @NotNull PathNavigation createNavigation(@NotNull Level pLevel) {
        return new ChameleonNavigation(this, pLevel);
    }

    @Override
    public float getXRot() {
        return -super.getXRot();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new EggLayingBreedGoal<>(this, 1.0D));
        this.goalSelector.addGoal(5, new EatEntityGoal(entity -> entity instanceof Silverfish || entity instanceof CaveSpider || (entity instanceof Slime slime && slime.isTiny())));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @javax.annotation.Nullable
            protected Vec3 getPosition() {
                if (this.mob.isInWaterOrBubble()) {
                    Vec3 vec3 = WallRandomPos.getPos(this.mob, 15, 7);
                    return vec3 == null ? super.getPosition() : vec3;
                } else {
                    return this.mob.getRandom().nextFloat() >= this.probability ? WallRandomPos.getPos(this.mob, 5, 4) : super.getPosition();
                }
            }
        });
        this.goalSelector.addGoal(4, new LayEggGoal<>(this, 1,BlockRegistry.CHAMELEON_EGG_BLOCK.get().defaultBlockState().setValue(ChameleonEggBlock.EGGS, random.nextInt(ChameleonEggBlock.MAX_EGGS) + ChameleonEggBlock.MIN_EGGS), (level, pos) -> level.getBlockState(pos).is(BlockTags.DIRT)));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 5));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            animateWhen("idle", hasPose(Pose.STANDING));
        }
        if (random.nextInt(TIME_BEFORE_COLOR_CHANGE) == 1) {
            Stream.of(blockPosition().north(),
                            blockPosition().east(),
                            blockPosition().south(),
                            blockPosition().west(),
                            blockPosition().below()
                    ).filter(pos -> level().getBlockState(pos).isSolidRender(level(), pos))
                    .findFirst().ifPresentOrElse(blockPos -> {
                        var state = level().getBlockState(blockPos);
                        Chameleon.this.entityData.set(COLOR, state.getMapColor(level(), blockPos).col);
                    }, () -> Chameleon.this.entityData.set(COLOR, 0x99C351));
        }
    }

    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public void aiStep() {
        if (navigation.isDone() && isClimbing()) {
            setDeltaMovement(Vec3.ZERO);
        }
        setOnGround(Stream.of(
                blockPosition().north(),
                blockPosition().east(),
                blockPosition().south(),
                blockPosition().west(),
                blockPosition().below()
        ).anyMatch(pos -> level().getBlockState(pos).isSolidRender(level(), pos)));

        super.aiStep();
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return 0;
    }

    public boolean isClimbing() {
        BlockPos under = blockPosition().below();
        Stream<BlockPos> sides = Stream.of(blockPosition().north(), blockPosition().east(), blockPosition().south(), blockPosition().west());
        return level().isEmptyBlock(under) && sides.anyMatch(pos -> level().getBlockState(pos).isSolidRender(level(), pos));
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (this.isFood(itemstack)) {
            int i = this.getAge();
            if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.setInLove(pPlayer);
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.level().isClientSide) {
                replayAnimation("tongue");
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
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
        return Optional.ofNullable(getAnimations().get().get("animation.chameleon." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.CHAMELEON.get().create(pLevel);
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(Items.SPIDER_EYE) ||
                pStack.is(Items.FERMENTED_SPIDER_EYE);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundRegistry.CHAMELEON_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.CHAMELEON_HURT.get();
    }

    @Override
    public void handleEntityEvent(byte pId) {
        super.handleEntityEvent(pId);
        if (pId == 5 && level().isClientSide()) {
            replayAnimation("tongue");
        }
    }

    static class ChameleonNavigation extends WallClimberNavigation {
        public ChameleonNavigation(Mob pMob, Level pLevel) {
            super(pMob, pLevel);
        }
        public boolean isStableDestination(BlockPos pPos) {
            Stream<BlockPos> positions = Stream.of(pPos.below(), pPos.north(), pPos.east(), pPos.south(), pPos.west());
            return positions.anyMatch(blockPos -> this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos));
        }
    }

    class EatEntityGoal extends Goal {
        private final Predicate<LivingEntity> predicate;

        EatEntityGoal(Predicate<LivingEntity> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void tick() {
            super.tick();
            if (distanceToSqr(Chameleon.this.getTarget()) < 4) {
                eat();
            } else if (navigation.isDone()) {
                navigation.moveTo(Chameleon.this.getTarget(), 1);
            }
        }

        private void eat() {
            level().broadcastEntityEvent(Chameleon.this, (byte)5);
            Chameleon.this.spawnAtLocation(ItemRegistry.RAINBOW_GEL.get());
            Chameleon.this.getTarget().remove(RemovalReason.KILLED);
            stop();
        }

        @Override
        public void stop() {
            super.stop();
            Chameleon.this.setTarget(null);
        }

        @Override
        public boolean canContinueToUse() {
            return Chameleon.this.getTarget() != null;
        }

        @Override
        public boolean canUse() {
            if (Chameleon.this.getTarget() == null) {
                Chameleon.this.setTarget(level().getNearestEntity(level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(Chameleon.this.position(), 5, 5, 5), predicate), TargetingConditions.DEFAULT, Chameleon.this, Chameleon.this.getX(), Chameleon.this.getY(), Chameleon.this.getZ()));
            }
            return Chameleon.this.getTarget() != null && random.nextFloat() > 0.9;
        }
    }

}
