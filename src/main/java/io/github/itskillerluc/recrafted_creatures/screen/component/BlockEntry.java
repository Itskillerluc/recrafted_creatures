package io.github.itskillerluc.recrafted_creatures.screen.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockEntry extends ObjectSelectionList.Entry<BlockEntry> {
    private final Minecraft minecraft;
    public ItemStack itemStack;
    private boolean isRemoved;
    public static final int BG_FILL = FastColor.ARGB32.color(255, 255, 0, 0);
    private int x;
    private int y;
    private int width;
    private int height;

    public BlockEntry(Minecraft minecraft, ItemStack stack, int x, int y, int width, int height) {
        this.minecraft = minecraft;
        itemStack = stack;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(@NotNull GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
        int l;
        l = pTop + (pHeight - 9) / 2;
        pGuiGraphics.renderFakeItem(itemStack, pLeft, l);
        pGuiGraphics.drawString(minecraft.font, String.format("x%s", itemStack.getCount()), pLeft + 17, l + 4, 0xFFFFFF);
    }


    public void setRemoved(boolean pIsRemoved) {
        this.isRemoved = pIsRemoved;
    }

    public boolean isRemoved() {
        return this.isRemoved;
    }

    @Override
    public Component getNarration() {
        return itemStack.getDisplayName();
    }
}
