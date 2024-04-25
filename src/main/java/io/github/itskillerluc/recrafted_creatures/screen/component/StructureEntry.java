package io.github.itskillerluc.recrafted_creatures.screen.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

public class StructureEntry extends ObjectSelectionList.Entry<StructureEntry> {
    private final Minecraft minecraft;
    private final ResourceLocation location;
    private boolean isRemoved;
    public static final int BG_FILL = FastColor.ARGB32.color(255, 255, 0, 0);
    private int x;
    private int y;
    private int width;
    private int height;

    public StructureEntry(Minecraft minecraft, ResourceLocation structure, int x, int y, int width, int height) {
        this.minecraft = minecraft;
        location = structure;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(@NotNull GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
        int l;
        l = pTop + (pHeight - 9) / 2;
        pGuiGraphics.drawString(this.minecraft.font, this.getStructureName().split(":")[1], pLeft, l, 0xFFFFFF, false);
    }

    public String getStructureName() {
        return this.location.toString();
    }

    public void setRemoved(boolean pIsRemoved) {
        this.isRemoved = pIsRemoved;
    }

    public boolean isRemoved() {
        return this.isRemoved;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public Component getNarration() {
        return Component.literal(getLocation().toString());
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return true;
    }
}
