package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.client.registries.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RedPanda extends TamableAnimal implements Animatable<RedPandaModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "red_panda");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);

    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> GiraffeModel.createStateMap(getAnimation()));
    public RedPanda(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 5));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new SleepGoal());
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
        return getAnimations().get().get("animation.red_panda." + animation);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.RED_PANDA.get().create(pLevel);
    }

    class SleepGoal extends Goal {
        protected boolean hasShelter() {
            BlockPos blockpos = new BlockPos(getX(), getBoundingBox().maxY, getZ());
            return !level.canSeeSky(blockpos) && getWalkTargetValue(blockpos) >= 0.0F;
        }

        private static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(140);
        private int countdown = random.nextInt(WAIT_TIME_BEFORE_SLEEP);

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if (xxa == 0.0F && yya == 0.0F && zza == 0.0F) {
                return this.canSleep() || isSleeping();
            } else {
                return false;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return this.canSleep();
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            } else {
                return level.isDay() && this.hasShelter() && !isInPowderSnow && !isInSittingPose();
            }
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.countdown = random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            RedPanda.this.setPose(Pose.STANDING);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            RedPanda.this.setPose(Pose.SLEEPING);
            RedPanda.this.getNavigation().stop();
            RedPanda.this.getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0D);
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (level.isClientSide()){
            return InteractionResult.CONSUME;
        }
        if (getOwnerUUID() != null && this.getOwnerUUID().compareTo(pPlayer.getUUID()) == 0) {
            if (pPlayer.getItemInHand(pHand).is(Items.BAMBOO) && this.getHealth() < this.getMaxHealth()) {
                if (!pPlayer.getAbilities().instabuild) {
                    pPlayer.getItemInHand(pHand).shrink(1);
                }
                this.heal(3);
                return InteractionResult.SUCCESS;
            } else {
                this.setOrderedToSit(!isOrderedToSit());
                return InteractionResult.SUCCESS;
            }
        } else if (this.getOwner() == null && this.getAge() == 0 && !this.isInLove() && pPlayer.getItemInHand(pHand).is(Items.BAMBOO)) {
            this.setInLove(pPlayer);
            return InteractionResult.SUCCESS;
        }
        else if (this.getOwner() == null && pPlayer.getItemInHand(pHand).is(Items.BAMBOO)){
            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
                this.tame(pPlayer);
                this.navigation.stop();
                this.setTarget(null);
                this.level.broadcastEntityEvent(this, (byte)7);
            } else {
                this.level.broadcastEntityEvent(this, (byte)6);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            animateWhen("sleep", this.getPose() == Pose.SLEEPING, tickCount);
            animateWhen("sit", this.isInSittingPose(), tickCount);
        }
    }
}
