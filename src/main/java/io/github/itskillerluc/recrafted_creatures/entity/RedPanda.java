package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.client.models.GiraffeModel;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.client.registries.EntityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RedPanda extends Animal implements Animatable<RedPandaModel> {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "red_panda");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);

    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> GiraffeModel.createStateMap(getAnimation()));
    public RedPanda(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
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

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.RED_PANDA.get().create(pLevel);
    }
}
