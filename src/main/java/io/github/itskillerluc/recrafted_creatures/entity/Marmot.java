package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.MarmotModel;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class Marmot extends Animal implements Animatable<MarmotModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "marmot");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    protected boolean canLook = true;
    protected boolean isDancing = false;

    private static final EntityDataAccessor<Integer> EATING = SynchedEntityData.defineId(Marmot.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPLODE = SynchedEntityData.defineId(Marmot.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> ANVIL = SynchedEntityData.defineId(Marmot.class, EntityDataSerializers.BYTE);

    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> MarmotModel.createStateMap(getAnimation()));


    static final Predicate<ItemEntity> EATABLE_ITEMS = itemEntity -> {
        ItemStack stack = itemEntity.getItem();
        return stack.is(Items.TALL_GRASS) || stack.is(ItemTags.FLOWERS) || stack.is(Items.EGG) || stack.is(Items.COOKIE) || stack.is(Items.TNT);
    };

    public Marmot(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        lookControl = new LookControl(this) {
            @Override
            public void setLookAt(Entity pEntity) {
                if (canLook) {
                    super.setLookAt(pEntity);
                }
            }
        };
        this.setCanPickUpLoot(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putByte("anvil", entityData.get(ANVIL));
        pCompound.putBoolean("canLook", canLook);
        pCompound.putBoolean("isDancing", isDancing);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(ANVIL, pCompound.getByte("anvil"));
        canLook = pCompound.getBoolean("canLook");
        isDancing = pCompound.getBoolean("isDancing");
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EATING, 0);
        this.entityData.define(EXPLODE, 0);
        this.entityData.define(ANVIL, (byte) 0);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 5));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new MarmotAlertGoal(400));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 50, true, false, mob -> (mob instanceof Enemy || mob instanceof Player) && !mob.hasEffect(MobEffects.GLOWING)));
    }

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(equipmentSlot).isEmpty()) {
            return false;
        } else {
            return equipmentSlot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
        }
    }

    public boolean isEating() {
        return this.entityData.get(EATING) > 0;
    }

    public void eat(boolean eating) {
        this.entityData.set(EATING, eating ? 1 : 0);
    }

    public int getEating() {
        return this.entityData.get(EATING);
    }

    private void setEating(int eating) {
        this.entityData.set(EATING, eating);
    }

    public boolean isPrimed() {
        return this.entityData.get(EXPLODE) > 0;
    }

    public void prime(boolean primed) {
        this.entityData.set(EXPLODE, primed ? 1 : 0);
    }

    private int getFuse() {
        return this.entityData.get(EXPLODE);
    }

    private void setFuse(int fuse) {
        this.entityData.set(EXPLODE, fuse);
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
        return Optional.ofNullable(getAnimations().get().get("animation.marmot." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.MARMOT.get().create(pLevel);
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(Items.TALL_GRASS) ||
                pStack.is(ItemTags.FLOWERS) ||
                pStack.is(Items.EGG) ||
                pStack.is(Items.COOKIE);
    }

    @Override
    public void aiStep() {
        if (getEntityData().get(ANVIL) > 0 || isDancing) {
            canLook = false;
            navigation.stop();
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        animateWhen("eat", isEating());
        if (!getAnimationState("anvil").get().isStarted() && getEntityData().get(ANVIL) == 1) {
            getEntityData().set(ANVIL, (byte) 2);
        }
        animateWhen("squashed", getEntityData().get(ANVIL) == 2);
        if (!level().isClientSide()) {
            handleEating();
        }
        animateWhen("boing", isDancing);
        explode();
    }

    public void handleEntityEvent(byte pId) {
        super.handleEntityEvent(pId);
        if (pId == 1) {
            replayAnimation("alert");
        }
    }

    private void explode() {
        if (isPrimed()) {
            playAnimation("shake");
            setFuse(getFuse() + 1);
            if (getFuse() > 80) {
                if (!this.level().isClientSide) {
                    this.dead = true;
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), 5, Level.ExplosionInteraction.MOB);
                    this.discard();
                }
            }
        }
    }

    private void handleEating() {
        if (!this.isEating() && !this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && this.random.nextInt(80) == 1) {
            this.eat(true);
        } else if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            this.eat(false);
        }

        if (this.isEating()) {
            this.addEatingParticles();
            if (!this.level().isClientSide && this.getEating() > 40) {
                if (this.getEating() > 40 && (this.isFood(this.getItemBySlot(EquipmentSlot.MAINHAND)) || this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.TNT))) {
                    if (!this.level().isClientSide) {
                        if (this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.TNT)) {
                            prime(true);
                        }
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.gameEvent(GameEvent.EAT);
                    }
                }

                this.eat(false);
                return;
            }

            this.setEating(this.getEating() + 1);
        }
    }

    private void addEatingParticles() {
        if (this.getEating() % 5 == 0) {
            this.playSound(SoundEvents.PANDA_EAT, 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

            for(int i = 0; i < 6; ++i) {
                Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, ((double)this.random.nextFloat() - 0.5D) * 0.1D);
                vec3 = vec3.xRot(-this.getXRot() * ((float)Math.PI / 180F));
                vec3 = vec3.yRot(-this.getYRot() * ((float)Math.PI / 180F));
                double d0 = (double)(-this.random.nextFloat()) * 0.6D - 0.3D;
                Vec3 vec31 = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.8D, d0, 1.0D + ((double)this.random.nextFloat() - 0.5D) * 0.4D);
                vec31 = vec31.yRot(-this.yBodyRot * ((float)Math.PI / 180F));
                vec31 = vec31.add(this.getX(), this.getEyeY() + 1.0D, this.getZ());
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItemBySlot(EquipmentSlot.MAINHAND)), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
            }
        }
    }

    protected void pickUpItem(ItemEntity pItemEntity) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && EATABLE_ITEMS.test(pItemEntity)) {
            this.onItemPickup(pItemEntity);
            ItemStack itemstack = pItemEntity.getItem();
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
        }

    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundRegistry.MARMOT_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.MARMOT_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.MARMOT_AMBIENCE.get();
    }

    public void setRecordPlayingNearby(BlockPos pPos, boolean pIsPartying) {
        isDancing = pIsPartying;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.is(DamageTypes.FALLING_ANVIL) && entityData.get(ANVIL) == 0) {
            playAnimation("anvil");
            entityData.set(ANVIL, (byte) 1);
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    protected class MarmotAlertGoal extends Goal {
        private final int glowTime;

        protected MarmotAlertGoal(int glowTime) {
            this.glowTime = glowTime;
        }

        @Override
        public boolean canUse() {
            return Marmot.this.getTarget() != null && entityData.get(ANVIL) == 0;
        }

        @Override
        public void start() {
            super.start();
            Marmot.this.getTarget().addEffect(new MobEffectInstance(MobEffects.GLOWING, glowTime));
            playSound(SoundRegistry.MARMOT_ALERT.get());
            level().broadcastEntityEvent(Marmot.this, (byte) 1);
            setTarget(null);
        }
    }
}
