package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.advancement.BeaverBuildTrigger;
import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.screen.BeaverScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SetBeaverSettingsPacket {
    private final int entity;
    private final Mirror mirror;
    private final Rotation rotation;
    private final boolean show;
    private final String name;

    private final boolean shouldBuild;
    public SetBeaverSettingsPacket(int entity, Mirror mirror, Rotation rotation, boolean show, String name, boolean shouldBuild) {
        this.entity = entity;
        this.mirror = mirror;
        this.rotation = rotation;
        this.show = show;
        this.shouldBuild = shouldBuild;
        this.name = name;
    }

    public static SetBeaverSettingsPacket decoder(FriendlyByteBuf buffer) {
        return new SetBeaverSettingsPacket(buffer.readInt(), buffer.readEnum(Mirror.class), buffer.readEnum(Rotation.class), buffer.readBoolean(), buffer.readNullable(FriendlyByteBuf::readUtf), buffer.readBoolean());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(entity);
        buffer.writeEnum(mirror);
        buffer.writeEnum(rotation);
        buffer.writeBoolean(show);
        buffer.writeNullable(name, FriendlyByteBuf::writeUtf);
        buffer.writeBoolean(shouldBuild);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getSender() == null) return;
        var level = ctx.get().getSender().level();
        if (level.getEntity(entity) instanceof Beaver beaver) {
            beaver.getEntityData().set(Beaver.MIRROR, mirror);
            beaver.getEntityData().set(Beaver.ROTATION, rotation);
            if (show) {
                beaver.buildPos = null;
            }
            beaver.getEntityData().set(Beaver.BUILD_NAME, name);
            beaver.show = show;
            if (beaver.buildPos == null && beaver.possibleBuildPos != null && !beaver.getEntityData().get(Beaver.BUILD_NAME).isEmpty()) {
                beaver.buildPos = beaver.possibleBuildPos;
            }
            if (beaver.buildPos != null && level.getBlockState(beaver.buildPos).is(BlockRegistry.CONSTRUCTOR.get())) {
                beaver.shouldBuild = shouldBuild;
                BeaverBuildTrigger.INSTANCE.trigger(ctx.get().getSender());
            } else {
                ctx.get().getSender().sendSystemMessage(Component.translatableWithFallback("gui.beaver.nolocation", "No Valid Build Position Found."), true);
            }
        }
    }
}
