package io.github.itskillerluc.recrafted_creatures.networking;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.networking.packets.*;
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

        CHANNEL.messageBuilder(ScareOrangutanPacket.class, 3, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ScareOrangutanPacket::encoder)
                .decoder(ScareOrangutanPacket::decoder)
                .consumerMainThread(ScareOrangutanPacket::handle)
                .add();

        CHANNEL.messageBuilder(OrangutanBabyRidePacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OrangutanBabyRidePacket::encoder)
                .decoder(OrangutanBabyRidePacket::decoder)
                .consumerMainThread(OrangutanBabyRidePacket::handle)
                .add();

        CHANNEL.messageBuilder(AskColorPacket.class, 5, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AskColorPacket::encoder)
                .decoder(AskColorPacket::decoder)
                .consumerMainThread(AskColorPacket::handle)
                .add();

        CHANNEL.messageBuilder(ChangePalettePacket.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChangePalettePacket::encoder)
                .decoder(ChangePalettePacket::decoder)
                .consumerMainThread(ChangePalettePacket::handle)
                .add();

        CHANNEL.messageBuilder(SaveStructurePacket.class, 7, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SaveStructurePacket::encoder)
                .decoder(SaveStructurePacket::decoder)
                .consumerMainThread(SaveStructurePacket::handle)
                .add();

        CHANNEL.messageBuilder(FetchBlocksPacket.class, 8, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FetchBlocksPacket::encoder)
                .decoder(FetchBlocksPacket::decoder)
                .consumerMainThread(FetchBlocksPacket::handle)
                .add();

        CHANNEL.messageBuilder(SetBlocksPacket.class, 9, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SetBlocksPacket::encoder)
                .decoder(SetBlocksPacket::decoder)
                .consumerMainThread(SetBlocksPacket::handle)
                .add();

        CHANNEL.messageBuilder(SetBeaverSettingsPacket.class, 10, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetBeaverSettingsPacket::encoder)
                .decoder(SetBeaverSettingsPacket::decoder)
                .consumerMainThread(SetBeaverSettingsPacket::handle)
                .add();
    }
}
