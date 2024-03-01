package io.github.itskillerluc.recrafted_creatures.event;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.capability.HerdCap;
import io.github.itskillerluc.recrafted_creatures.capability.HerdProvider;
import io.github.itskillerluc.recrafted_creatures.capability.IHerd;
import io.github.itskillerluc.recrafted_creatures.config.Configs;
import io.github.itskillerluc.recrafted_creatures.mixin.FrogMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
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
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IHerd.class);
    }

    @SubscribeEvent
    public static void onJoinWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!ModList.get().isLoaded("patchouli") &&     Configs.Client.showPatchouliWarning.get()) {
            event.getPlayer().displayClientMessage(Component.translatableWithFallback("recrafted_creatures.misc.patchouli_not_installed",
                    "You do not have Patchouli installed. This means that you won't be able to access the guidebook.\nConsider installing %s. \nYou can disable this message in the %s.", Component.literal("Patchouli").withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatableWithFallback("recrafted_creatures.misc.install_patchouli", "Click here to install Patchouli."))).withColor(ChatFormatting.LIGHT_PURPLE).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/patchouli"))), Component.literal("Configs").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatableWithFallback("recrafted_creatures.misc.open_config", "Open the config."))).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Path.of(FMLPaths.CONFIGDIR.get().toString(), "/recrafted_creatures-client.toml").toString())))).withStyle(ChatFormatting.GOLD), false);
        }
    }
}
