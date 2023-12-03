package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class DeliveryPacket {
    private final UUID player;
    private final UUID owl;

    public DeliveryPacket(UUID player, UUID owl) {
        this.player = player;
        this.owl = owl;
    }

    public static DeliveryPacket decoder(FriendlyByteBuf buffer) {
        return new DeliveryPacket(buffer.readUUID(), buffer.readUUID());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeUUID(player);
        buffer.writeUUID(owl);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        var owl = ((ServerLevel) player.level()).getEntity(this.owl);
        if (owl instanceof Owl owlEntity) {
            owlEntity.sendItem(this.player);
        }
    }
}
