package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.entity.Orangutan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class OrangutanBabyRidePacket {
    private final int parent;
    private final int baby;
    public OrangutanBabyRidePacket(int parent, int baby) {
        this.parent = parent;
        this.baby = baby;
    }

    public static OrangutanBabyRidePacket decoder(FriendlyByteBuf buffer) {
        return new OrangutanBabyRidePacket(buffer.readInt(), buffer.readInt());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(parent);
        buffer.writeInt(baby);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            if (level.getEntity(baby) instanceof Orangutan orangutan) {
                orangutan.getEntityData().set(Orangutan.ON_BACK, true);
                orangutan.startRiding(level.getEntity(parent));
                orangutan.missingMommy = level.getRandom().nextInt(2000, 8000);
                orangutan.curiosity = 0;
            }
        }
    }
}
