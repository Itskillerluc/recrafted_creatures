package io.github.itskillerluc.recrafted_creatures.screen.component;

import com.google.common.collect.Lists;
import io.github.itskillerluc.recrafted_creatures.screen.BeaverScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class BlockList extends ObjectSelectionList<BlockEntry> {
    private final List<BlockEntry> blocks = Lists.newArrayList();
    @Nullable
    private String filter;
    private final BeaverScreen screen;

    public BlockList(BeaverScreen screen, Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
        this.screen = screen;
        setRenderTopAndBottom(false);
        setRenderBackground(false);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    protected void renderDecorations(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        if (getHovered() != null) {
            pGuiGraphics.renderTooltip(minecraft.font, Component.literal(String.format("%sx %s",getHovered().itemStack.getCount(), getHovered().itemStack.getItem().getDescriptionId().replaceFirst("block.", "").replace(".", ":"))), pMouseX, pMouseY);
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return getLeft() + getRowWidth() - 6;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.fillGradient(getLeft(), getTop(), getLeft() + getWidth(), getTop() + getHeight(), 0, 0xC0101010, 0xD0101010);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public void updateBlockList(Collection<ItemStack> itemStacks, double pScrollAmount) {
        this.updateFiltersAndScroll(itemStacks.stream().map(name -> new BlockEntry(minecraft, name, getLeft(), getTop(), getLeft() + width, getTop() + height)).collect(Collectors.toCollection(ArrayList::new)), pScrollAmount);
        if (itemStacks.isEmpty() && screen.structureList.getSelected() != null) {
            screen.build.active = true;
        }
    }

    private void sortStructureEntries() {
        this.blocks.sort(Comparator.comparing(blockEntry -> blockEntry.itemStack.getDisplayName().getString()));
    }

    private void updateFiltersAndScroll(Collection<BlockEntry> blocks, double pScrollAmount) {
        this.blocks.clear();
        this.blocks.addAll(blocks);
        this.sortStructureEntries();
        this.updateFilteredStructures();
        this.replaceEntries(this.blocks);
        this.setScrollAmount(pScrollAmount);
    }

    private void updateFilteredStructures() {
        if (this.filter != null) {
            this.blocks.removeIf(entry -> !entry.itemStack.getDisplayName().toString().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.blocks);
        }

    }

    public void setFilter(String pFilter) {
        this.filter = pFilter;
    }

    public boolean isEmpty() {
        return this.blocks.isEmpty();
    }

    public void addStructure(ItemStack stack) {
        for(BlockEntry entry : this.blocks) {
            if (entry.itemStack.equals(stack)) {
                entry.setRemoved(false);
                return;
            }
        }
        BlockEntry entry = new BlockEntry(minecraft, stack, getLeft(), getTop(), getLeft() + width, getTop() + height);

        this.addEntry(entry);
        this.blocks.add(entry);
    }

    public void removeStructure(ItemStack stack) {
        for(BlockEntry entry : this.blocks) {
            if (entry.itemStack.equals(stack)) {
                entry.setRemoved(true);
                return;
            }
        }
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics) {
        super.renderBackground(pGuiGraphics);
    }
}
