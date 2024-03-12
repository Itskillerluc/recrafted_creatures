package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.SecretarybirdEggBlock;
import io.github.itskillerluc.recrafted_creatures.client.models.SecretarybirdModel;
import io.github.itskillerluc.recrafted_creatures.entity.ai.DefendTargetGoal;
import io.github.itskillerluc.recrafted_creatures.entity.ai.EggLaying;
import io.github.itskillerluc.recrafted_creatures.entity.ai.EggLayingBreedGoal;
import io.github.itskillerluc.recrafted_creatures.entity.ai.LayEggGoal;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

public class Secretarybird extends TamableRCMob implements Animatable<SecretarybirdModel>, VariantHolder<Secretarybird.SecretarybirdVariant>, FlyingAnimal, EggLaying {
    private static final EntityDataSerializer<SecretarybirdVariant> SECRETARYBIRD_VARIANT_SERIALIZER = EntityDataSerializer.simpleEnum(SecretarybirdVariant.class);
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Secretarybird.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Secretarybird.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> NEST = SynchedEntityData.defineId(Secretarybird.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "secretary_bird");
    private static final EntityDataAccessor<SecretarybirdVariant> VARIANT = SynchedEntityData.defineId(Secretarybird.class, SECRETARYBIRD_VARIANT_SERIALIZER);
    private static final EntityDataAccessor<OptionalInt> POTION_COLOR = SynchedEntityData.defineId(Secretarybird.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> SecretarybirdModel.createStateMap(getAnimation()));
    private final MoveControl flyingControl = new FlyingMoveControl(this, 10, false);
    private final MoveControl walkingControl = new MoveControl(this);
    boolean goToTree = false;
    int layEggCounter;
    List<Supplier<MobEffectInstance>> effect = null;
    boolean isDancing = false;


    static {
        EntityDataSerializers.registerSerializer(SECRETARYBIRD_VARIANT_SERIALIZER);
    }


    public Secretarybird(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = walkingControl;
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
        pCompound.putBoolean("isSleeping", isSleeping());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setVariant(SecretarybirdVariant.valueOf(pCompound.getString("variant")));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        setVariant(SecretarybirdVariant.values()[pLevel.getRandom().nextIntBetweenInclusive(0, 2)]);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, SecretarybirdVariant.White);
        entityData.define(HAS_EGG, false);
        entityData.define(LAYING_EGG, false);
        entityData.define(NEST, Optional.empty());
        entityData.define(POTION_COLOR, OptionalInt.empty());
    }

    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);

        return flyingpathnavigation;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            animateWhen("idle", hasPose(Pose.STANDING));
            animateWhen("sit", hasPose(Pose.SITTING));
            animateWhen("fly", isFlying());
        }
    }

    @Override
    public void swing(InteractionHand pHand) {
        replayAnimation(random.nextBoolean() ? "kill" : "kill2");
        super.swing(pHand);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 5.0F, 1.0F, true) {
            @Override
            public boolean canUse() {
                return super.canUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }
        });
        this.goalSelector.addGoal(3, new EggLayingBreedGoal<>(this, 1.0D));
        this.goalSelector.addGoal(4, new MoveToBlockGoal(this, 1, 16, 8) {

            @Override
            public boolean canUse() {
                verticalSearchStart = 6;
                return super.canUse() && goToTree;
            }

            @Override
            protected int nextStartTick(PathfinderMob pCreature) {
                return 0;
            }

            @Override
            public void start() {
                moveControl = flyingControl;
                super.start();
            }

            @Override
            protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
                return pLevel.isEmptyBlock(pPos.above()) && pLevel.getBlockState(pPos).is(BlockTags.LEAVES);
            }

            @Override
            protected void moveMobToBlock() {
                super.moveMobToBlock();
                goToTree = false;
                moveControl = flyingControl;
            }

//            @Override
//            protected boolean findNearestBlock() {
//                int i = 16;
//                int j = 16;
//                BlockPos blockpos = this.mob.blockPosition();
//                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
//
//                for(int k = j - this.verticalSearchStart; k >= 0; k = k > 0 ? -k : 1 - k) {
//                    for(int l = 0; l < i; ++l) {
//                        for(int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
//                            for(int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
//                                blockpos$mutableblockpos.setWithOffset(blockpos, i1, k - 1, j1);
//                                if (this.mob.isWithinRestriction(blockpos$mutableblockpos) && this.isValidTarget(this.mob.level(), blockpos$mutableblockpos)) {
//                                    this.blockPos = blockpos$mutableblockpos;
//                                    return true;
//                                }
//                            }
//                        }
//                    }
//                }
//
//                return false;
//            }
        });
        this.goalSelector.addGoal(4, new LayEggGoal<>(this, 1, BlockRegistry.SECRETARYBIRD_EGG_BLOCk.get().defaultBlockState(), (level, pos) -> level.getBlockState(pos).is(BlockTags.LEAVES)));
        this.goalSelector.addGoal(4, new SecretarybirdWanderGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1, false));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new DefendTargetGoal<>(this, LivingEntity.class, false, () -> entityData.get(NEST).get().getCenter(), 50).shouldDefend(() -> entityData.get(NEST).isPresent()));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, entity -> entity.getBoundingBox().getXsize() * entity.getBoundingBox().getYsize() * getBoundingBox().getZsize() < 1));
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() != null) {
            if (entityData.get(NEST).isPresent()) {
                var pos = entityData.get(NEST).get();
                getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);
            } else {
                goToTree = true;
            }
        }
        return super.hurt(pSource, pAmount);
    }

    public void setEffect(ItemStack stack) {
        if (stack.getItem() instanceof LingeringPotionItem) {
            var effects = PotionUtils.getMobEffects(stack);
            effect = effects.stream().map((Function<MobEffectInstance, Supplier<MobEffectInstance>>) mobEffectInstance -> () -> new MobEffectInstance(mobEffectInstance)).toList();
            entityData.set(POTION_COLOR, OptionalInt.of(PotionUtils.getColor(effects)));
        }
    }

    public void removeEffects() {
        effect = null;
        entityData.set(POTION_COLOR, OptionalInt.empty());
    }

    public Vec3 getEffectRGB(boolean asFloat) {
        if (entityData.get(POTION_COLOR).isEmpty()) return null;
        var color = entityData.get(POTION_COLOR).getAsInt();
        var factor = asFloat ? 255d : 1d;
        return new Vec3(((color & 0xFF0000) >> 16) / factor, ((color & 0x00FF00) >> 8) / factor, (color & 0x0000FF) / factor);
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if (pEntity instanceof LivingEntity livingEntity && effect != null) {
            for (Supplier<MobEffectInstance> mobEffectInstanceSupplier : effect) {
                livingEntity.addEffect(mobEffectInstanceSupplier.get());
            }
        }
        return super.doHurtTarget(pEntity);
    }

    @Override
    public void setRecordPlayingNearby(BlockPos pJukebox, boolean pPartyParrot) {
        isDancing = pPartyParrot;
        if (pPartyParrot) {
            level().broadcastEntityEvent(this, (byte)2);
        } else {
            level().broadcastEntityEvent(this, (byte)5);
        }
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
    public void aiStep() {
        super.aiStep();
        calculateFlapping();
    }

    private void calculateFlapping() {
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0.0D) {
            this.setDeltaMovement(vec3.multiply(1.0D, target != null ? 0.9D : 0.6D, 1.0D));
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
        return Optional.ofNullable(getAnimations().get().get("animation.secretary_bird." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.SECRETARYBIRD.get().create(pLevel);
    }

    @Override
    public void setVariant(SecretarybirdVariant pVariant) {
        entityData.set(VARIANT, pVariant);
    }

    @Override
    public SecretarybirdVariant getVariant() {
        return entityData.get(VARIANT);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(Items.CHICKEN) || pStack.is(Items.RABBIT);
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.getOwner() != null && pPlayer.getItemInHand(pHand).is(Items.MILK_BUCKET) && !level().isClientSide()) {
            removeEffects();
            pPlayer.setItemInHand(pHand, new ItemStack(Items.BUCKET));
            return InteractionResult.SUCCESS;
        }

        if (this.getOwner() != null && pPlayer.getItemInHand(pHand).is(Items.LINGERING_POTION) && !level().isClientSide()) {
            setEffect(pPlayer.getItemInHand(pHand));
            pPlayer.setItemInHand(pHand, new ItemStack(Items.GLASS_BOTTLE));
            return InteractionResult.SUCCESS;
        }
        if (this.getOwner() != null && this.getAge() == 0 && !this.isInLove() && isFood(pPlayer.getItemInHand(pHand)) && !level().isClientSide()) {
            this.setInLove(pPlayer);
            pPlayer.getItemInHand(pHand).shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (this.isBaby() && isFood(pPlayer.getItemInHand(pHand)) && !level().isClientSide()) {
            pPlayer.getItemInHand(pHand).shrink(1);
            this.ageUp(getSpeedUpSecondsWhenFeeding(-getAge()), true);
            return InteractionResult.SUCCESS;
        }
        if (this.getOwner() == null && isFood(pPlayer.getItemInHand(pHand)) && !level().isClientSide()){
            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
                this.tame(pPlayer);
                this.navigation.stop();
                this.setTarget(null);
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

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 2) {
            isDancing = true;
            setNoAi(true);
        } else if (pId == 5) {
            isDancing = false;
            setNoAi(false);
        } else {
            super.handleEntityEvent(pId);
        }
    }

    class SecretarybirdWanderGoal extends WaterAvoidingRandomStrollGoal {
        public SecretarybirdWanderGoal(PathfinderMob p_186224_, double p_186225_) {
            super(p_186224_, p_186225_);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !goToTree;
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !goToTree;
        }

        protected Vec3 getPosition() {
            Vec3 vec3;
            Secretarybird.this.moveControl = walkingControl;
            setNoGravity(false);
            if (this.mob.level().random.nextFloat() < 0.05F) {
                Secretarybird.this.moveControl = flyingControl;
                if (mob.getEntityData().get(NEST).isPresent()) {
                    vec3 = mob.getEntityData().get(NEST).get().getCenter();
                } else {
                    goToTree = true;
                    stop();
                    return null;
                }
            } else if (this.mob.level().random.nextFloat() < 0.3F || mob.getEntityData().get(NEST).isEmpty()) {
                vec3 = LandRandomPos.getPos(this.mob, 15, 15);
            } else {
                vec3 = LandRandomPos.getPosTowards(mob, 10, 5, mob.getEntityData().get(NEST).get().getCenter());
                if (vec3 == null) {
                    vec3 = LandRandomPos.getPos(this.mob, 15, 15);
                }
                return vec3 == null ? super.getPosition() : vec3;
            }
            return vec3 == null ? super.getPosition() : vec3;
        }
    }

    public enum SecretarybirdVariant {
        White(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/white_secretary_bird.png")),
        GRAY(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/gray_secretary_bird.png")),
        OPILA(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/opila_secretary_bird.png"));

        public final ResourceLocation texture;

        SecretarybirdVariant(ResourceLocation texture) {
            this.texture = texture;
        }
    }
}
