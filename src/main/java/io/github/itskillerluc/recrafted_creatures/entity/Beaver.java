package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.BeaverModel;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.Tags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Beaver extends TamableRCMob implements Animatable<BeaverModel> {
    private static final EntityDataSerializer<BeaverVariant> BEAVER_VARIANT_SERIALIZER = EntityDataSerializer.simpleEnum(BeaverVariant.class);
    public static final EntityDataAccessor<BeaverVariant> VARIANT = SynchedEntityData.defineId(Beaver.class, BEAVER_VARIANT_SERIALIZER);

    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "beaver");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private static final int MAX_TRADE_TIME = 40;
    static final ResourceLocation LOOT = new ResourceLocation(RecraftedCreatures.MODID, "gameplay/beaver_barter");

    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> RedPandaModel.createStateMap(getAnimation()));
    private int pickupCooldown;
    private int tradeTimer;

    public Beaver(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        moveControl = new BeaverMoveControl();
        setCanPickUpLoot(true);
    }

    class BeaverMoveControl extends MoveControl {

        public BeaverMoveControl() {
            super(Beaver.this);
        }

        @Override
        public void tick() {
            if (getMainHandItem().isEmpty()) {
                super.tick();
            }
        }
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, BeaverVariant.LIGHT);
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
            level().broadcastEntityEvent(this, (byte) 1);
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
    public void handleEntityEvent(byte pId) {
        super.handleEntityEvent(pId);
        if (pId == 1) {
            playAnimation("observing_trade");
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BeaverGoal(new FloatGoal(this)));
        this.goalSelector.addGoal(2, new BeaverGoal(new SitWhenOrderedToGoal(this)));
        this.goalSelector.addGoal(3, new BeaverGoal(new FollowOwnerGoal(this, 1.4D, 10.0F, 2.0F, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }
        }));
        this.goalSelector.addGoal(7, new BeaverGoal(new BreedGoal(this, 1.0D)));
        this.goalSelector.addGoal(8, new BeaverGoal(new RandomStrollGoal(this, 1.0D)));
        this.goalSelector.addGoal(10, new BeaverGoal(new LookAtPlayerGoal(this, Player.class, 5)));
        this.goalSelector.addGoal(10, new BeaverGoal(new RandomLookAroundGoal(this)));

    }

    private class BeaverGoal extends Goal {
        Goal goal;

        public BeaverGoal(Goal goal) {
            this.goal = goal;
        }

        @Override
        public boolean canUse() {
            return Beaver.this.getMainHandItem().isEmpty() && goal.canUse();
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
        return Optional.ofNullable(getAnimations().get().get("animation.beaver." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.BEAVER.get().create(pLevel);
    }


    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (getOwnerUUID() != null && this.getOwnerUUID().compareTo(pPlayer.getUUID()) == 0 && !level().isClientSide()) {
            if (pPlayer.getItemInHand(pHand).is(ItemRegistry.FRUIT_KEBAB.get()) && this.getHealth() < this.getMaxHealth()) {
                if (!pPlayer.getAbilities().instabuild) {
                    pPlayer.getItemInHand(pHand).shrink(1);
                }
                this.heal(3);
            } else {
                if (pPlayer.getItemInHand(pHand).is(ItemRegistry.FRUIT_KEBAB.get()) && this.getAge() == 0 && !this.isInLove()) {
                    this.setInLove(pPlayer);
                    pPlayer.getItemInHand(pHand).shrink(1);
                    return InteractionResult.SUCCESS;
                } else {
                    return super.mobInteract(pPlayer, pHand);
                }
            }
            return InteractionResult.SUCCESS;
        } else if (this.getOwner() == null && !level().isClientSide() && (pPlayer.getItemInHand(pHand).is(ItemRegistry.FRUIT_KEBAB.get()) || pPlayer.getItemInHand(pHand).is(ItemRegistry.APPLE_SLICE.get()))) {
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
        return super.mobInteract(pPlayer, pHand);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getMainHandItem().isEmpty()) {
            navigation.stop();
            if (tradeTimer++ > (level().isClientSide() ? MAX_TRADE_TIME - 1 : MAX_TRADE_TIME)) {
                pickupCooldown = 200;
                tradeTimer = 0;
                if (getMainHandItem().is(Tags.BEAVER_BARTERING)) {
                    if (level().isClientSide()) {
                        playAnimation("trade_likes");
                    } else {
                        getMainHandItem().shrink(1);
                        LootTable loottable = level().getServer().getLootData().getLootTable(LOOT);
                        List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel) level())).withParameter(LootContextParams.THIS_ENTITY, Beaver.this).create(LootContextParamSets.PIGLIN_BARTER));
                        for (ItemStack itemStack : list) {
                            spawnAtLocation(itemStack);
                        }
                    }
                } else {
                    if (level().isClientSide()) {
                        playAnimation("trade_dislikes");
                    } else {
                        spawnAtLocation(getMainHandItem());
                        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
            }
        }
        if (this.level().isClientSide()) {
            animateWhen("idle", !isMoving(this));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        entityData.set(VARIANT, random.nextBoolean() ? BeaverVariant.LIGHT : BeaverVariant.DARK);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
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
        return SoundRegistry.RED_PANDA_AMBIENCE.get();
    }

    public enum BeaverVariant {
        LIGHT(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_normal.png"), new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_bob_normal.png")),
        DARK(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_dark.png"),new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_bob_dark.png"));

        private final ResourceLocation normal;
        private final ResourceLocation hat;

        BeaverVariant(ResourceLocation normal, ResourceLocation hat) {
            this.normal = normal;
            this.hat = hat;
        }

        public ResourceLocation getTexture(boolean bob) {
            return bob ? hat : normal;
        }
    }

    static {
        EntityDataSerializers.registerSerializer(BEAVER_VARIANT_SERIALIZER);
    }
}
