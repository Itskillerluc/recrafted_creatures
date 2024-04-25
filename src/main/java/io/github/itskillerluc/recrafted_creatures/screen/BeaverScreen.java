package io.github.itskillerluc.recrafted_creatures.screen;

import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.advancement.BeaverBuildTrigger;
import io.github.itskillerluc.recrafted_creatures.advancement.OwlDeliveryTrigger;
import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import io.github.itskillerluc.recrafted_creatures.menu.BeaverMenu;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.FetchBlocksPacket;
import io.github.itskillerluc.recrafted_creatures.networking.packets.SetBeaverSettingsPacket;
import io.github.itskillerluc.recrafted_creatures.screen.component.BlockList;
import io.github.itskillerluc.recrafted_creatures.screen.component.StructureEntry;
import io.github.itskillerluc.recrafted_creatures.screen.component.StructureList;
import io.github.itskillerluc.recrafted_creatures.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class BeaverScreen extends AbstractContainerScreen<BeaverMenu> {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(RecraftedCreatures.MODID, "textures/gui/beaver_gui.png");
    private static final Component SEARCH_HINT = Component.translatable("gui.socialInteractions.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    static final Component EMPTY_SEARCH = Component.translatable("gui.socialInteractions.search_empty").withStyle(ChatFormatting.GRAY);
    public final Beaver beaver;
    public BlockList blockList;
    public StructureList structureList;
    EditBox searchBox;
    EditBox searchBoxBlocks;
    public CycleButton<Mirror> mirrorCycleButton;
    public CycleButton<Rotation> rotationCycleButton;
    public Button show;
    public Button build;
    private String lastSearch = "";
    public Mirror mirror = Mirror.NONE;
    public Rotation rotation = Rotation.NONE;
    public boolean showVar = false;
    public boolean shouldBuild = false;
    public BeaverScreen(BeaverMenu pMenu, Inventory pPlayerInventory, Component title) {
        super(pMenu, pPlayerInventory, title);
        this.beaver = pMenu.beaver;
        imageWidth = 256;
        imageHeight = 179;
        titleLabelX = 70;
        inventoryLabelX = 70;
        inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.font,  i + 4, j + 4, 56, 13, SEARCH_HINT) {
            protected MutableComponent createNarrationMessage() {
                return !BeaverScreen.this.searchBox.getValue().isEmpty() && BeaverScreen.this.structureList.isEmpty() ? super.createNarrationMessage().append(", ").append(BeaverScreen.EMPTY_SEARCH) : super.createNarrationMessage();
            }
        };
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(s);
        this.searchBox.setHint(SEARCH_HINT);
        this.searchBox.setResponder(this::checkSearchStringUpdate);
        this.addRenderableWidget(this.searchBox);

        String sb = this.searchBoxBlocks != null ? this.searchBoxBlocks.getValue() : "";
        this.searchBoxBlocks = new EditBox(this.font,  i + 195, j + 4, 56, 13, SEARCH_HINT) {
            protected MutableComponent createNarrationMessage() {
                return !BeaverScreen.this.searchBoxBlocks.getValue().isEmpty() && BeaverScreen.this.blockList.isEmpty() ? super.createNarrationMessage().append(", ").append(BeaverScreen.EMPTY_SEARCH) : super.createNarrationMessage();
            }
        };
        this.searchBoxBlocks.setMaxLength(16);
        this.searchBoxBlocks.setVisible(true);
        this.searchBoxBlocks.setTextColor(16777215);
        this.searchBoxBlocks.setValue(sb);
        this.searchBoxBlocks.setHint(SEARCH_HINT);
        this.searchBoxBlocks.setResponder(this::checkSearchStringUpdateBlock);
        this.addRenderableWidget(this.searchBoxBlocks);

        blockList = new BlockList(this, minecraft, 57, 72, j + 19, j + 91, 20);
        blockList.setLeftPos(i + 195);

        structureList = new StructureList(this, minecraft, 52, 72, j + 19, j + 91, 20);
        structureList.setLeftPos(i + 3);
        for (ResourceLocation structure : menu.structures) {
            structureList.addStructure(structure);
        }

        mirrorCycleButton = this.addRenderableWidget(CycleButton.builder(Mirror::symbol).withValues(Mirror.values()).displayOnlyValue().withInitialValue(Mirror.NONE).create(i + 196 - 60, j + 40, 50, 20, Component.literal("MIRROR"), (p_169843_, p_169844_) -> {
            mirror = p_169844_;
            NetworkChannel.CHANNEL.sendToServer(new SetBeaverSettingsPacket(beaver.getId(), mirror, rotation, showVar, Util.GetNonNullElseGet(structureList.getSelected(), StructureEntry::getStructureName, null), shouldBuild));
        }));

        rotationCycleButton = this.addRenderableWidget(CycleButton.<Rotation>builder(rot -> switch (rot) {
                    case NONE -> Component.literal("0");
                    case CLOCKWISE_90 -> Component.literal("90");
                    case CLOCKWISE_180 -> Component.literal("180");
                    case COUNTERCLOCKWISE_90 -> Component.literal("270");
                }).withValues(Rotation.values()).displayOnlyValue().withInitialValue(Rotation.NONE).create(i + 70, j + 40, 50, 20, Component.literal("ROTATION"), (p_169843_, p_169844_) -> {
            rotation = p_169844_;
            NetworkChannel.CHANNEL.sendToServer(new SetBeaverSettingsPacket(beaver.getId(), mirror, rotation, showVar,Util.GetNonNullElseGet(structureList.getSelected(), StructureEntry::getStructureName, null), shouldBuild));
        }));

        show = addRenderableWidget(Button.builder(Component.translatableWithFallback("gui.beaver.show", "Show"), pButton -> {
            showVar = true;
            NetworkChannel.CHANNEL.sendToServer(new SetBeaverSettingsPacket(beaver.getId(), mirror, rotation, showVar, Util.GetNonNullElseGet(structureList.getSelected(), StructureEntry::getStructureName, null),shouldBuild));
            onClose();
        }).size(50, 20).pos(i + 70, j + 62).build());

        build = addRenderableWidget(Button.builder(Component.translatableWithFallback("gui.beaver.build", "Build"), pButton -> {
            shouldBuild = true;
            NetworkChannel.CHANNEL.sendToServer(new SetBeaverSettingsPacket(beaver.getId(), mirror, rotation, showVar, Util.GetNonNullElseGet(structureList.getSelected(), StructureEntry::getStructureName, null),shouldBuild));
            onClose();
        }).size(50, 20).pos(i + 196 - 60, j + 62).build());

        show.active = false;
        mirrorCycleButton.active = false;
        rotationCycleButton.active = false;
        build.active = false;

        mirrorCycleButton.setValue(menu.beaver.getEntityData().get(Beaver.MIRROR));
        mirror = menu.beaver.getEntityData().get(Beaver.MIRROR);
        rotationCycleButton.setValue(menu.beaver.getEntityData().get(Beaver.ROTATION));
        rotation = menu.beaver.getEntityData().get(Beaver.ROTATION);
        structureList.setSelected(structureList.children().stream().filter(child -> child.getStructureName().equals(menu.beaver.getEntityData().get((Beaver.BUILD_NAME)))).findFirst().orElse(null));
        if (structureList.getSelected() != null) {
            NetworkChannel.CHANNEL.sendToServer(new FetchBlocksPacket(structureList.getSelected().getLocation(), beaver.getId()));
            rotationCycleButton.active = true;
            mirrorCycleButton.active = true;
            show.active = true;
            NetworkChannel.CHANNEL.sendToServer(new SetBeaverSettingsPacket(beaver.getId(), mirror, rotation, showVar, Util.GetNonNullElseGet(structureList.getSelected(), StructureEntry::getStructureName, null), shouldBuild));
        }
        addRenderableWidget(blockList);
        addRenderableWidget(structureList);
    }

    private void checkSearchStringUpdate(String string) {
        string = string.toLowerCase(Locale.ROOT);
        if (!string.equals(this.lastSearch)) {
            this.structureList.setFilter(string);
            this.lastSearch = string;
            this.structureList.updateStructureList(menu.structures, this.structureList.getScrollAmount());
        }
    }

    private void checkSearchStringUpdateBlock(String string) {
        string = string.toLowerCase(Locale.ROOT);
        if (!string.equals(this.lastSearch)) {
            this.blockList.setFilter(string);
            this.lastSearch = string;
            if (structureList.getSelected() != null) {
                NetworkChannel.CHANNEL.sendToServer(new FetchBlocksPacket(structureList.getSelected().getLocation(), beaver.getId()));
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(BACKGROUND, i, j, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        blockList.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        structureList.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        structureList.mouseClicked(pMouseX, pMouseY, pButton);
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
