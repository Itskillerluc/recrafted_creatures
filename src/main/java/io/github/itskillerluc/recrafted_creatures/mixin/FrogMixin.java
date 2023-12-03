package io.github.itskillerluc.recrafted_creatures.mixin;

import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Frog.class)
public abstract class FrogMixin extends Animal implements Bucketable {
    public FrogMixin(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean fromBucket() {
        return true;
    }

    @Override
    public void setFromBucket(boolean pFromBucket) {

    }

    @Override
    public void saveToBucketTag(ItemStack pStack) {
        Bucketable.saveDefaultDataToBucketTag((Frog)(Object)this, pStack);
        CompoundTag compoundtag = pStack.getOrCreateTag();
        compoundtag.putInt("Age", this.getAge());
        int variant = ((Frog)((Object) this)).getVariant() == FrogVariant.TEMPERATE ? 0 : ((Frog)((Object) this)).getVariant() == FrogVariant.WARM ? 1 : 2;
        compoundtag.putInt("Variant", variant);
    }

    @Override
    public void loadFromBucketTag(CompoundTag pTag) {
        Bucketable.loadDefaultDataFromBucketTag((Frog)(Object)this, pTag);
        if (pTag.contains("Age")) {
            this.setAge(pTag.getInt("Age"));
        }
        int variant = pTag.getInt("Variant");
        if (variant == 0) {
            ((Frog)((Object) this)).setVariant(FrogVariant.TEMPERATE);
        } else if (variant == 1) {
            ((Frog)((Object) this)).setVariant(FrogVariant.WARM);
        } else {
            ((Frog)((Object) this)).setVariant(FrogVariant.COLD);
        }
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(ItemRegistry.FROG_BUCKET.get());
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_TADPOLE;
    }
}
