package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ScareOwlPacket {
    private final UUID entity;
    private final int scare;
    public ScareOwlPacket(UUID entity, int scare) {
        this.entity = entity;
        this.scare = scare;
    }

    public static ScareOwlPacket decoder(FriendlyByteBuf buffer) {
        return new ScareOwlPacket(buffer.readUUID(), buffer.readInt());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeUUID(entity);
        buffer.writeInt(scare);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        Entity entity1 = ((ServerLevel) player.level()).getEntity(this.entity);
        if (entity1 instanceof Owl owl) {
            owl.scared = scare;
        }

    }
}
