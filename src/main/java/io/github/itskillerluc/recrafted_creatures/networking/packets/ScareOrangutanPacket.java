package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.entity.Orangutan;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ScareOrangutanPacket {
    private final UUID entity;
    private final int scare;
    public ScareOrangutanPacket(UUID entity, int scare) {
        this.entity = entity;
        this.scare = scare;
    }

    public static ScareOrangutanPacket decoder(FriendlyByteBuf buffer) {
        return new ScareOrangutanPacket(buffer.readUUID(), buffer.readInt());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeUUID(entity);
        buffer.writeInt(scare);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        Entity entity1 = ((ServerLevel) player.level()).getEntity(this.entity);
        if (entity1 instanceof Orangutan monke) {
            monke.scared = scare;
        }

    }
}
