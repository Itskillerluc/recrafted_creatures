package io.github.itskillerluc.recrafted_creatures.networking;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.networking.packets.DancePacket;
import io.github.itskillerluc.recrafted_creatures.networking.packets.DeliveryPacket;
import io.github.itskillerluc.recrafted_creatures.networking.packets.ScareOwlPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkChannel {
    public static SimpleChannel CHANNEL;
    public static final String PROTOCOL_VERSION = "1";
    public static void register() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(RecraftedCreatures.MODID, "main_channel"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        CHANNEL.messageBuilder(DeliveryPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeliveryPacket::encoder)
                .decoder(DeliveryPacket::decoder)
                .consumerMainThread(DeliveryPacket::handle)
                .add();

        CHANNEL.messageBuilder(DancePacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DancePacket::encoder)
                .decoder(DancePacket::decoder)
                .consumerMainThread(DancePacket::handle)
                .add();

        CHANNEL.messageBuilder(ScareOwlPacket.class, 2, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ScareOwlPacket::encoder)
                .decoder(ScareOwlPacket::decoder)
                .consumerMainThread(ScareOwlPacket::handle)
                .add();
    }
}
