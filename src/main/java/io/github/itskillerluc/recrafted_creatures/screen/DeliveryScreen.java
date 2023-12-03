package io.github.itskillerluc.recrafted_creatures.screen;

import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import io.github.itskillerluc.recrafted_creatures.screen.component.DeliveryList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class DeliveryScreen extends Screen {
    protected static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");
    private static final Component SEARCH_HINT = Component.translatable("gui.socialInteractions.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    static final Component EMPTY_SEARCH = Component.translatable("gui.socialInteractions.search_empty").withStyle(ChatFormatting.GRAY);

    DeliveryList deliveryList;
    private boolean initialized;
    EditBox searchBox;
    final Owl owl;
    private String lastSearch = "";


    public DeliveryScreen(Owl owl) {
        super(Component.translatableWithFallback("gui.deliver.title", "Delivery"));
        this.owl = owl;
    }

    @Override
    protected void init() {
        if (this.initialized) {
            this.deliveryList.updateSize(this.width, this.height, 88, this.listEnd());
        } else {
            this.deliveryList = new DeliveryList(owl, this.minecraft, this.width, this.height, 88, this.listEnd(), 36);
        }

        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.font, this.marginX() + 29, 75, 198, 13, SEARCH_HINT) {
            protected MutableComponent createNarrationMessage() {
                return !DeliveryScreen.this.searchBox.getValue().isEmpty() && DeliveryScreen.this.deliveryList.isEmpty() ? super.createNarrationMessage().append(", ").append(DeliveryScreen.EMPTY_SEARCH) : super.createNarrationMessage();
            }
        };
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(s);
        this.searchBox.setHint(SEARCH_HINT);
        this.searchBox.setResponder(this::checkSearchStringUpdate);
        this.addWidget(this.searchBox);
        this.addWidget(this.deliveryList);

        this.deliveryList.updatePlayerList(this.minecraft.player.connection.getOnlinePlayerIds(), deliveryList.getScrollAmount());
        this.initialized = true;
    }

    private int windowHeight() {
        return Math.max(52, this.height - 128 - 16);
    }

    private int listEnd() {
        return 80 + this.windowHeight() - 8;
    }

    private int marginX() {
        return (this.width - 238) / 2;
    }

    private void checkSearchStringUpdate(String string) {
        string = string.toLowerCase(Locale.ROOT);
        if (!string.equals(this.lastSearch)) {
            this.deliveryList.setFilter(string);
            this.lastSearch = string;
            this.deliveryList.updatePlayerList(this.minecraft.player.connection.getOnlinePlayerIds(), this.deliveryList.getScrollAmount());
        }
    }

    public void renderBackground(GuiGraphics pGuiGraphics) {
        int i = this.marginX() + 3;
        super.renderBackground(pGuiGraphics);
        pGuiGraphics.blitNineSliced(TEXTURE_LOCATION, i, 64, 236, this.windowHeight() + 16, 8, 236, 34, 1, 1);
        pGuiGraphics.blit(TEXTURE_LOCATION, i + 10, 76, 243, 1, 12, 12);
    }
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);

        if (!this.deliveryList.isEmpty()) {
            this.deliveryList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        } else if (!this.searchBox.getValue().isEmpty()) {
            pGuiGraphics.drawCenteredString(this.minecraft.font, EMPTY_SEARCH, this.width / 2, (72 + this.listEnd()) / 2, -1);
        }

        this.searchBox.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (!this.searchBox.isFocused() && minecraft.options.keyInventory.getKey().getValue() == pKeyCode) {
            this.minecraft.setScreen((Screen)null);
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onAddPlayer(PlayerInfo pPlayerInfo) {
        this.deliveryList.addPlayer(pPlayerInfo);
    }

    public void onRemovePlayer(UUID pId) {
        this.deliveryList.removePlayer(pId);
    }
}
