package io.github.itskillerluc.recrafted_creatures.networking.packets;

import io.github.itskillerluc.recrafted_creatures.screen.BeaverScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SetBlocksPacket {
    private final List<ItemStack> itemStackList;
    public SetBlocksPacket(List<ItemStack> itemStackList) {
        this.itemStackList = itemStackList;
    }

    public static SetBlocksPacket decoder(FriendlyByteBuf buffer) {
        return new SetBlocksPacket(buffer.readList(FriendlyByteBuf::readItem).stream().peek(item -> item.setCount(item.getTag().getInt("count"))).toList());
    }

    public void encoder(FriendlyByteBuf buffer) {
        for (ItemStack itemStack : itemStackList) {
            itemStack.getOrCreateTag().putInt("count", itemStack.getCount());
        }
        buffer.writeCollection(itemStackList, ((friendlyByteBuf, itemStack) -> friendlyByteBuf.writeItemStack(itemStack, false)));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var screen = Minecraft.getInstance().screen;
        if (screen != null) {
            if (screen instanceof BeaverScreen beaverScreen) {
                beaverScreen.blockList.updateBlockList(itemStackList, beaverScreen.blockList.getScrollAmount());
            }
        }
    }
}
