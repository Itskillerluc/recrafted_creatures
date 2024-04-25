package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.GameData;

import java.util.*;
import java.util.function.Supplier;

public class SaveStructurePacket {
    private final BlockPos constructorPos;
    private final String structureName;
    public SaveStructurePacket(String structureName, BlockPos constructorPos) {
        this.constructorPos = constructorPos;
        this.structureName = structureName;
    }

    public static SaveStructurePacket decoder(FriendlyByteBuf buffer) {
        return new SaveStructurePacket(buffer.readUtf(), buffer.readBlockPos());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeUtf(structureName);
        buffer.writeBlockPos(constructorPos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getSender().level().getBlockEntity(constructorPos) instanceof ConstructorBlockEntity blockEntity) {
            blockEntity.setStructureName(structureName);
            blockEntity.saveStructure(ctx.get().getSender());
        }
    }
}
