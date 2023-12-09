package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.entity.Marmot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class DancePacket {
    private final UUID entity;
    private final boolean dancing;

    public DancePacket(UUID entity, boolean dancing) {
        this.entity = entity;
        this.dancing = dancing;
    }

    public static DancePacket decoder(FriendlyByteBuf buffer) {
        return new DancePacket(buffer.readUUID(), buffer.readBoolean());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeUUID(entity);
        buffer.writeBoolean(dancing);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        Entity entity1 = ((ServerLevel) player.level()).getEntity(this.entity);
        if (entity1 instanceof Marmot marmot) {
            marmot.isDancing = dancing;
            marmot.setNoAi(dancing);
        }

    }
}
