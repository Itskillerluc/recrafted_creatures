package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.registries.EntityRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.extensions.IForgePlayer;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Giraffe extends AbstractHorse implements NeutralMob, Animatable<GiraffeModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "giraffe");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private static final Ingredient FOOD_ITEMS = Ingredient.merge(List.of(Ingredient.of(Items.WHEAT, Items.HAY_BLOCK.asItem(), Items.CARROT, Items.GOLDEN_CARROT), Ingredient.of(ItemTags.LEAVES)));
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(Giraffe.class, EntityDataSerializers.BOOLEAN);

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
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (!level.isClientSide() && isSaddleable() && pPlayer.getItemInHand(pHand).isEmpty()){
            this.setSaddled(false);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();
        animateWhen("tail_run", getPose() == Pose.STANDING && Animatable.isMoving(this), tickCount);
        animateWhen("tounge", random.nextInt(3600) == 1, tickCount);
    }

    @Override
    protected void addPassenger(@NotNull Entity pPassenger) {
        super.addPassenger(pPassenger);
        if (pPassenger instanceof Player player && player.getAttribute(ForgeMod.REACH_DISTANCE.get()) != null){
            player.getAttribute(ForgeMod.REACH_DISTANCE.get()).setBaseValue(player.getReachDistance() + 2);
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity pPassenger) {
        super.removePassenger(pPassenger);
        if (pPassenger instanceof Player player && player.getAttribute(ForgeMod.REACH_DISTANCE.get()) != null){
            player.getAttribute(ForgeMod.REACH_DISTANCE.get()).setBaseValue(player.getReachDistance() -2);
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
        return !isSaddled();
    }

    public void setSaddled(boolean saddled){
        entityData.set(SADDLED, saddled);
    }

    @Override
    public void equipSaddle(@Nullable SoundSource pSource) {
        setSaddled(true);
        if (pSource != null) {
            this.level.playSound(null, this, this.getSaddleSoundEvent(), pSource, 0.5F, 1.0F);
        }
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
    public AnimationState getAnimationState(String animation) {
        return getAnimations().get().get("animation.giraffe." + animation);
    }
}
