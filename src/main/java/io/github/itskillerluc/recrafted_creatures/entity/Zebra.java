package io.github.itskillerluc.recrafted_creatures.entity;

import com.mojang.serialization.Dynamic;
import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.ZebraModel;
import io.github.itskillerluc.recrafted_creatures.entity.ai.CreateHerd;
import io.github.itskillerluc.recrafted_creatures.entity.ai.zebra.ZebraAI;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Zebra extends AbstractChestedHorse implements NeutralMob, Animatable<ZebraModel> {

    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "zebra");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);

    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> ZebraModel.createStateMap(getAnimation()));
    private static final EntityDataSerializer<ZebraVariant> VARIANT_SERIALIZER = EntityDataSerializer.simpleEnum(ZebraVariant.class);
    public static final EntityDataAccessor<Optional<UUID>> RUN_AWAY_FROM = SynchedEntityData.defineId(Zebra.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<ZebraVariant> VARIANT = SynchedEntityData.defineId(Zebra.class, VARIANT_SERIALIZER);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;


    public Zebra(EntityType<? extends AbstractChestedHorse> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        navigation.setCanFloat(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RUN_AWAY_FROM, Optional.empty());
        entityData.define(VARIANT, ZebraVariant.NORMAL);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        var herd = new CreateHerd();
        herd.actuallyCreateHerd(pLevel.getLevel(), this, pLevel.getLevel().getGameTime(), new ArrayList<>(pLevel.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(this.position(), 16, 16, 16))));
        attributes().add(Attributes.JUMP_STRENGTH, generateJumpStrength(() -> pLevel.getRandom().nextDouble()));
        if (random.nextFloat() < 0.02f) {
            entityData.set(VARIANT, ZebraVariant.BROWN);
        }
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public Brain<Zebra> getBrain() {
        return (Brain<Zebra>)super.getBrain();
    }
    @Override
    protected Brain.Provider<Zebra> brainProvider() {
        return ZebraAI.brainProvider();
    }

    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        return ZebraAI.makeBrain(this.brainProvider().makeBrain(pDynamic));
    }

    @Override
    public boolean canMate(@NotNull Animal pOtherAnimal) {
        return pOtherAnimal instanceof Zebra zebra && zebra.canParent() && this.canParent();
    }


    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.ZEBRA.get().create(pLevel);
    }

    @Override
    protected void registerGoals() {}

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            animateWhen("idle", !isMoving(this) && !isJumping);
            animateWhen("jump", isJumping);
        }
        if (entityData.get(RUN_AWAY_FROM).isPresent()) {
            Player player = level().getPlayerByUUID(entityData.get(RUN_AWAY_FROM).get());
            if (player != null) {
                if (distanceToSqr(player) > 900) {
                    entityData.set(RUN_AWAY_FROM, Optional.empty());
                }
            }
        }
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime) {
        remainingPersistentAngerTime = pRemainingPersistentAngerTime;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget) {
        persistentAngerTarget = pPersistentAngerTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    /**
     * event id:
     * 8 = play kick animation
     * @param pId the event id
     */
    @Override
    public void handleEntityEvent(byte pId) {
        super.handleEntityEvent(pId);
        if (pId == 8) {
            replayAnimation("kick");
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return Horse.createBaseHorseAttributes()
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
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
        return Optional.ofNullable(getAnimations().get().get("animation.zebra." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    public ZebraVariant getVariant() {
        return entityData.get(VARIANT);
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.ZEBRA_DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.ZEBRA_AMBIENCE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundRegistry.ZEBRA_HURT.get();
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean flag = super.hurt(pSource, pAmount);
        if (this.level().isClientSide) {
            return false;
        } else {
            if (flag && pSource.getEntity() instanceof LivingEntity) {
                ZebraAI.wasHurtBy(this, (LivingEntity)pSource.getEntity());
            }

            return flag;
        }
    }

    @Override
    protected void customServerAiStep() {
        this.getBrain().tick((ServerLevel)this.level(), this);
        ZebraAI.updateActivity(this);
        super.customServerAiStep();
    }

    @Override
    public void swing(InteractionHand pHand) {
        if (level().isClientSide()) {
            replayAnimation(random.nextBoolean() ? "head_swat" : "head_swat_second");
        }
        super.swing(pHand);
    }

    public enum ZebraVariant {
        NORMAL(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_normal.png"),
                new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_normal_chest.png"),
                new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_normal_saddled.png"),
                new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_normal_saddled_chest.png")),
        BROWN(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_brown.png"),
                new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_brown_chest.png"),
                new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_brown_saddled.png"),
                new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/zebra_brown_saddled_chest.png"));

        private final ResourceLocation normal;
        private final ResourceLocation chested;
        private final ResourceLocation saddled;
        private final ResourceLocation saddledAndChested;

        ZebraVariant(ResourceLocation normal, ResourceLocation chested, ResourceLocation saddled, ResourceLocation saddledAndChested) {
            this.normal = normal;
            this.chested = chested;
            this.saddled = saddled;
            this.saddledAndChested = saddledAndChested;
        }

        public ResourceLocation getTexture(boolean saddled, boolean chested) {
            if (saddled) {
                return chested ? saddledAndChested : this.saddled;
            } else {
                return chested ? this.chested : normal;
            }
        }
    }

    static {
        EntityDataSerializers.registerSerializer(VARIANT_SERIALIZER);
    }
}
