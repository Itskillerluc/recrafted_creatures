package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.GameData;

import java.util.*;
import java.util.function.Supplier;

public class ChangePalettePacket {
    private final Map<BlockState, Integer> colors;
    private final ResourceLocation structure;
    private final int paletteId;
    public ChangePalettePacket(Map<BlockState, Integer> colors, ResourceLocation structure, int paletteId) {
        this.colors = colors;
        this.structure = structure;
        this.paletteId = paletteId;
    }

    public static ChangePalettePacket decoder(FriendlyByteBuf buffer) {
        return new ChangePalettePacket(buffer.readMap(friendlyByteBuf -> friendlyByteBuf.readById(GameData.getBlockStateIDMap()),FriendlyByteBuf::readInt), buffer.readResourceLocation(), buffer.readInt());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeMap(colors, (friendlyByteBuf, blockState) -> friendlyByteBuf.writeId(GameData.getBlockStateIDMap(), blockState),FriendlyByteBuf::writeInt);
        buffer.writeResourceLocation(structure);
        buffer.writeInt(paletteId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var sender = ctx.get().getSender();
        if (sender == null) return;
        var level = sender.level();
        Optional<StructureTemplate> optionalTemplate = ((ServerLevel) level).getStructureManager().get(structure);
        optionalTemplate.ifPresent(template -> {
            var palette = template.palettes.get(paletteId);
            var blocks = palette.blocks();
            Map<BlockState, BlockState> newPalette = new HashMap<>();
            for (Map.Entry<BlockState, Integer> blockStateIntegerEntry : colors.entrySet()) {
                var closestColor = ConstructorBlockEntity.PALETTE.entrySet().stream()
                        .min(Comparator.comparingInt(i -> (Math.abs((i.getValue() >> 16 & 0xFF) - (blockStateIntegerEntry.getValue() >> 16 & 0xFF)) + Math.abs((i.getValue() >> 8 & 0xFF) - (blockStateIntegerEntry.getValue() >> 8 & 0xFF)) + Math.abs((i.getValue() & 0xFF) - (blockStateIntegerEntry.getValue() & 0xFF))))).orElse(Map.entry(Blocks.AIR, 0x866042));
                newPalette.put(blockStateIntegerEntry.getKey(), closestColor.getKey().defaultBlockState());
            }
            template.palettes.remove(paletteId);
            List<StructureTemplate.StructureBlockInfo> infoList = new ArrayList<>();
            for (StructureTemplate.StructureBlockInfo block : blocks) {
                infoList.add(new StructureTemplate.StructureBlockInfo(block.pos(), newPalette.get(block.state()), block.nbt()));
            }
            var actualPalette = new StructureTemplate.Palette(infoList);
            template.palettes.add(paletteId, actualPalette);
            ((ServerLevel) level).getStructureManager().save(structure);
        });
    }
}
