package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.ChameleonModel;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

;

public class Chameleon extends Animal implements Animatable<ChameleonModel> {
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
        moveControl = new MoveControl(this) {

            @Override
            public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
                var blockOn = Stream.of(Direction.NORTH,
                                Direction.EAST,
                                Direction.SOUTH,
                                Direction.WEST,
                                Direction.DOWN
                        ).filter(dir -> level().getBlockState(blockPosition().relative(dir)).isSolidRender(level(), blockPosition().relative(dir)))
                        .findFirst();
                if (blockOn.isEmpty()) return;
                switch (blockOn.get()) {
                    case NORTH, SOUTH -> super.setWantedPosition(pX, pY, ((int) pZ) + 0.5, pSpeed);
                    case WEST, EAST -> super.setWantedPosition(((int) pX) + 0.5, pY, pZ, pSpeed);
                }
            }
        };
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

    void setHasEgg(boolean pHasEgg) {
        this.entityData.set(HAS_EGG, pHasEgg);
    }

    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    void setLayingEgg(boolean pIsLayingEgg) {
        this.layEggCounter = pIsLayingEgg ? 1 : 0;
        this.entityData.set(LAYING_EGG, pIsLayingEgg);
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
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @javax.annotation.Nullable
            protected Vec3 getPosition() {
                if (this.mob.isInWaterOrBubble()) {
                    Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
                    return vec3 == null ? super.getPosition() : vec3;
                } else {
                    return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 5, 4) : super.getPosition();
                }
            }
        });
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 5));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    @Override
    public void tick() {
        super.tick();
        animateWhen("idle", hasPose(Pose.STANDING));
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

    class ChameleonNavigation extends WallClimberNavigation {

        public ChameleonNavigation(Mob pMob, Level pLevel) {
            super(pMob, pLevel);
        }

        public boolean isStableDestination(BlockPos pPos) {
            Stream<BlockPos> positions = Stream.of(pPos.below(), pPos.north(), pPos.east(), pPos.south(), pPos.west());
            return positions.anyMatch(blockPos -> this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos));
        }
    }

    static class ChameleonLayEggGoal extends MoveToBlockGoal {
        private final Chameleon chameleon;

        ChameleonLayEggGoal(Chameleon chameleon, double pSpeedModifier) {
            super(chameleon, pSpeedModifier, 16);
            this.chameleon = chameleon;
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return this.chameleon.hasEgg() && super.canUse();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.chameleon.hasEgg();
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            super.tick();
            BlockPos blockpos = this.chameleon.blockPosition();
            if (this.isReachedTarget()) {
                if (this.chameleon.layEggCounter < 1) {
                    this.chameleon.setLayingEgg(true);
                } else if (this.chameleon.layEggCounter > this.adjustedTickDelay(200)) {
                    Level level = this.chameleon.level();
                    level.playSound((Player)null, blockpos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
                    BlockPos blockpos1 = this.blockPos.above();
                    BlockState blockstate = Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, Integer.valueOf(this.chameleon.random.nextInt(4) + 1));
                    level.setBlock(blockpos1, blockstate, 3);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockpos1, GameEvent.Context.of(this.chameleon, blockstate));
                    this.chameleon.setHasEgg(false);
                    this.chameleon.setLayingEgg(false);
                    this.chameleon.setInLoveTime(600);
                }

                if (this.chameleon.isLayingEgg()) {
                    ++this.chameleon.layEggCounter;
                }
            }

        }

        /**
         * Return {@code true} to set given position as destination
         */
        protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
            return !pLevel.isEmptyBlock(pPos.above()) ? false : TurtleEggBlock.isSand(pLevel, pPos);
        }
    }
}
