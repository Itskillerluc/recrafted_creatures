package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.GameData;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class AskColorPacket {
    private final List<BlockState> states;
    private final ResourceLocation structure;
    private final int paletteId;
    public AskColorPacket(List<BlockState> states, ResourceLocation structure, int paletteId) {
        this.states = states;
        this.structure = structure;
        this.paletteId = paletteId;
    }

    public static AskColorPacket decoder(FriendlyByteBuf buffer) {
        return new AskColorPacket(buffer.readList(friendlyByteBuf -> friendlyByteBuf.readById(GameData.getBlockStateIDMap())), buffer.readResourceLocation(), buffer.readInt());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeCollection(states, (friendlyByteBuf, blockState) -> friendlyByteBuf.writeId(GameData.getBlockStateIDMap(), blockState));
        buffer.writeResourceLocation(structure);
        buffer.writeInt(paletteId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        Map<BlockState, Integer> colors = new HashMap<>();
        for (BlockState state : states) {
            var model = dispatcher.getBlockModel(state);
            List<BakedQuad> quads = new ArrayList<>();
            for (Direction value : Direction.values()) {
                quads.addAll(model.getQuads(state, value, Minecraft.getInstance().player.getRandom(), ModelData.EMPTY, null));
            }

            float[] stateAverage = new float[3];

            if (quads.size() == 0) {
                var sprite = model.getParticleIcon(ModelData.EMPTY);
                var spriteContents = sprite.contents();
                int transparentPixels = 0;
                for (int x = 0; x < spriteContents.width(); x++) {
                    for (int y = 0; y < spriteContents.height(); y++) {
                        var color = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(spriteContents.name()).getPixelRGBA(0, x, y);
                        if ((color >> 24 & 0xFF) == 0) {
                            transparentPixels++;
                            continue;
                        }
                        stateAverage[2] += color >> 16 & 0xFF;
                        stateAverage[1] += color >> 8 & 0xFF;
                        stateAverage[0] += color & 0xFF;
                    }
                }

                stateAverage[0] /= spriteContents.width() * spriteContents.height() - transparentPixels;
                stateAverage[1] /= spriteContents.width() * spriteContents.height() - transparentPixels;
                stateAverage[2] /= spriteContents.width() * spriteContents.height() - transparentPixels;
            } else {
                for (BakedQuad quad : quads) {
                    var sprite = quad.getSprite();
                    float[] quadAverage = new float[3];
                    try {
                        var spriteContents = sprite.contents();
                        int transparentPixels = 0;
                        for (int x = 0; x < spriteContents.width(); x++) {
                            for (int y = 0; y < spriteContents.height(); y++) {
                                var color = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(spriteContents.name()).getPixelRGBA(0, x, y);

                                if ((color >> 24 & 0xFF) == 0) {
                                    transparentPixels++;
                                    continue;
                                }

                                quadAverage[2] += color >> 16 & 0xFF;
                                quadAverage[1] += color >> 8 & 0xFF;
                                quadAverage[0] += color & 0xFF;
                            }
                        }

                        quadAverage[0] /= spriteContents.width() * spriteContents.height() - transparentPixels;
                        quadAverage[1] /= spriteContents.width() * spriteContents.height() - transparentPixels;
                        quadAverage[2] /= spriteContents.width() * spriteContents.height() - transparentPixels;
                    } catch (Exception e) {
                        continue;
                    }
                    stateAverage[0] += quadAverage[0];
                    stateAverage[1] += quadAverage[1];
                    stateAverage[2] += quadAverage[2];
                }
                stateAverage[0] /= quads.size();
                stateAverage[1] /= quads.size();
                stateAverage[2] /= quads.size();
            }

            colors.put(state, (((int)stateAverage[0] << 16) & 0xFF0000) | (((int)stateAverage[1] << 8) & 0x00FF00) | ((int)stateAverage[2] & 0x0000FF));
        }
        NetworkChannel.CHANNEL.sendToServer(new ChangePalettePacket(colors, structure, paletteId));
    }
}
