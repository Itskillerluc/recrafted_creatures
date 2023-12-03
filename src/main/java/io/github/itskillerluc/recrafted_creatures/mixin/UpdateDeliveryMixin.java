package io.github.itskillerluc.recrafted_creatures.mixin;

import io.github.itskillerluc.recrafted_creatures.screen.DeliveryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerSocialManager.class)
public class UpdateDeliveryMixin {
    @Inject(method = "Lnet/minecraft/client/gui/screens/social/PlayerSocialManager;removePlayer(Ljava/util/UUID;)V", at = @At("TAIL"))
    public void removePlayer(UUID id, CallbackInfo info) {
        if (Minecraft.getInstance().screen instanceof DeliveryScreen deliveryScreen) {
            deliveryScreen.onRemovePlayer(id);
        }
    }

    @Inject(method = "Lnet/minecraft/client/gui/screens/social/PlayerSocialManager;addPlayer(Lnet/minecraft/client/multiplayer/PlayerInfo;)V", at = @At("TAIL"))
    public void addPlayer(PlayerInfo playerInfo, CallbackInfo info) {
        if (Minecraft.getInstance().screen instanceof DeliveryScreen deliveryScreen) {
            deliveryScreen.onAddPlayer(playerInfo);
        }
    }
}
