package io.github.itskillerluc.recrafted_creatures.client;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.config.Configs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = RecraftedCreatures.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeEvents {
    @SubscribeEvent
    public static void onJoinWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!ModList.get().isLoaded("patchouli") &&     Configs.Client.showPatchouliWarning.get()) {
            event.getPlayer().displayClientMessage(Component.translatableWithFallback("recrafted_creatures.misc.patchouli_not_installed",
                    "You do not have Patchouli installed. This means that you won't be able to access the guidebook.\nConsider installing %s. \nYou can disable this message in the %s.", Component.literal("Patchouli").withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatableWithFallback("recrafted_creatures.misc.install_patchouli", "Click here to install Patchouli."))).withColor(ChatFormatting.LIGHT_PURPLE).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/patchouli"))), Component.literal("Configs").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatableWithFallback("recrafted_creatures.misc.open_config", "Open the config."))).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Path.of(FMLPaths.CONFIGDIR.get().toString(), "/recrafted_creatures-client.toml").toString())))).withStyle(ChatFormatting.GOLD), false);
        }
    }
}
