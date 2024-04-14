package io.github.itskillerluc.recrafted_creatures.event;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.capability.ExtraTickProvider;
import io.github.itskillerluc.recrafted_creatures.capability.HerdProvider;
import io.github.itskillerluc.recrafted_creatures.capability.IExtraTick;
import io.github.itskillerluc.recrafted_creatures.capability.IHerd;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = RecraftedCreatures.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void interactEvent(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Frog frog) {
            event.setCancellationResult(bucketMobPickup(event.getEntity(), event.getHand(), frog).get());
            event.setCanceled(true);
        }
    }

    private static <T extends LivingEntity> Optional<InteractionResult> bucketMobPickup(Player pPlayer, InteractionHand pHand, T pEntity) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.getItem() == Items.WATER_BUCKET && pEntity.isAlive()) {
            pEntity.playSound(((Bucketable) pEntity).getPickupSound(), 1.0F, 1.0F);
            ItemStack itemstack1 = ((Bucketable) pEntity).getBucketItemStack();
            ((Bucketable) pEntity).saveToBucketTag(itemstack1);
            ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, pPlayer, itemstack1, false);
            pPlayer.setItemInHand(pHand, itemstack2);
            Level level = pEntity.level();
            if (!level.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)pPlayer, itemstack1);
            }

            pEntity.discard();
            return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
        } else {
            return Optional.of(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(HerdProvider.IDENTIFIER, new HerdProvider());
        event.addCapability(ExtraTickProvider.IDENTIFIER, new ExtraTickProvider());
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IHerd.class);
        event.register(IExtraTick.class);
    }

    @SubscribeEvent
    public static void livingHurtEvent(final LivingHurtEvent event) {
        if (event.getEntity().getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.BUILDER_HAT.get())) {
            event.setCanceled(true);
        }
    }
}
