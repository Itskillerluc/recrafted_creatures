package io.github.itskillerluc.recrafted_creatures.screen.component;

import com.google.common.collect.Lists;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.FetchBlocksPacket;
import io.github.itskillerluc.recrafted_creatures.networking.packets.SetBeaverSettingsPacket;
import io.github.itskillerluc.recrafted_creatures.screen.BeaverScreen;
import io.github.itskillerluc.recrafted_creatures.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class StructureList extends ObjectSelectionList<StructureEntry> {
    private final List<StructureEntry> structures = Lists.newArrayList();
    @Nullable
    private String filter;
    private final BeaverScreen screen;

    public StructureList(BeaverScreen screen, Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
        this.screen = screen;
        setRenderTopAndBottom(false);
        setRenderBackground(false);
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    protected void renderDecorations(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        if (getHovered() != null) {
            pGuiGraphics.renderTooltip(minecraft.font, Component.literal(getHovered().getStructureName()), pMouseX, pMouseY);
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return getLeft() + getRowWidth();
    }

    protected void enableScissor(GuiGraphics pGuiGraphics) {
        pGuiGraphics.enableScissor(this.x0, this.y0, this.x1, this.y1);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.fillGradient(getLeft(), getTop(), getLeft() + getWidth(), getTop() + getHeight(), 0, 0xC0101010, 0xD0101010);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public void updateStructureList(Collection<ResourceLocation> names, double pScrollAmount) {
        this.updateFiltersAndScroll(names.stream().map(name -> new StructureEntry(minecraft, name, getLeft(), getTop(), getLeft() + width, getTop() + height)).collect(Collectors.toCollection(ArrayList::new)), pScrollAmount);
    }

    private void sortStructureEntries() {
        this.structures.sort(Comparator.comparing(StructureEntry::getStructureName));
    }

    private void updateFiltersAndScroll(Collection<StructureEntry> structures, double pScrollAmount) {
        this.structures.clear();
        this.structures.addAll(structures);
        this.sortStructureEntries();
        this.updateFilteredStructures();
        this.replaceEntries(this.structures);
        this.setScrollAmount(pScrollAmount);
    }

    private void updateFilteredStructures() {
        if (this.filter != null) {
            this.structures.removeIf(entry -> !entry.getStructureName().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.structures);
        }

    }

    public void setFilter(String pFilter) {
        this.filter = pFilter;
    }

    public boolean isEmpty() {
        return this.structures.isEmpty();
    }

    public void addStructure(ResourceLocation structure) {
        for(StructureEntry entry : this.structures) {
            if (entry.getLocation().equals(structure)) {
                entry.setRemoved(false);
                return;
            }
        }
        StructureEntry entry = new StructureEntry(minecraft, structure, getLeft(), getTop(), getLeft() + width, getTop() + height);

        this.addEntry(entry);
        this.structures.add(entry);
    }

    public void removeStructure(ResourceLocation structure) {
        for(StructureEntry entry : this.structures) {
            if (entry.getLocation().equals(structure)) {
                entry.setRemoved(true);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            if (getSelected() == null) return false;
            NetworkChannel.CHANNEL.sendToServer(new FetchBlocksPacket(getSelected().getLocation(), screen.beaver.getId()));
            screen.rotationCycleButton.active = true;
            screen.mirrorCycleButton.active = true;
            screen.show.active = true;
            screen.build.active = false;
            NetworkChannel.CHANNEL.sendToServer(new SetBeaverSettingsPacket(screen.beaver.getId(), screen.mirror, screen.rotation, screen.showVar, Util.GetNonNullElseGet(getSelected(), StructureEntry::getStructureName, null), screen.shouldBuild));
            return true;
        }
        return false;
    }
}
