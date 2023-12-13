package io.github.itskillerluc.recrafted_creatures.block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Optional;
import java.util.stream.Stream;

public class OwlEnvelope extends Item {
    public OwlEnvelope(Item.Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return getContents(pStack).isPresent();
    }

    public static Optional<ItemStack> getContents(ItemStack stack) {
        if (stack.getTag() != null && stack.getTag().contains("item")) {
             if (stack.getTag().getCompound("item").isEmpty()) {
                 return Optional.empty();
             }
             return Optional.of(ItemStack.of(stack.getTag().getCompound("item")));
        }
        return Optional.empty();
    }

    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pOther.isEmpty()) return false;
        if (pStack.getTag() != null && pStack.getTag().contains("item")) {
            if (!ItemStack.of(pStack.getTag().getCompound("item")).isEmpty()) return false;
            pOther.save(pStack.getTag().getCompound("item"));
            pOther.setCount(0);
        } else {
            var compound = new CompoundTag();
            pOther.save(compound);
            pStack.getOrCreateTag().put("item", compound);
            pOther.setCount(0);
        }
        playInsertSound(pPlayer);
        return true;
    }
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (getContents(itemstack).isPresent()) {
            if (pLevel.isClientSide()) {
                itemstack.removeTagKey("item");
                playDropContentsSound(pPlayer);
            } else {
                var stack = getContents(itemstack).get().copy();
                itemstack.removeTagKey("item");
                pPlayer.drop(stack, true);
            }
            return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
        }
        return InteractionResultHolder.fail(itemstack);
    }

    public void onDestroyed(ItemEntity pItemEntity) {
        ItemUtils.onContainerDestroyed(pItemEntity, Stream.of(getContents(pItemEntity.getItem()).orElse(ItemStack.EMPTY)));
    }

    private void playInsertSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }
}
