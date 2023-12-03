package io.github.itskillerluc.recrafted_creatures.screen.component;

import com.google.common.collect.ImmutableList;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.DeliveryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class DeliveryEntry extends ContainerObjectSelectionList.Entry<DeliveryEntry> {
    private final Minecraft minecraft;
    private final List<AbstractWidget> children;
    private Button deliverButton;
    private final UUID id;
    private final String playerName;
    private final Supplier<ResourceLocation> skinGetter;
    private final BooleanSupplier canSend;
    private boolean isRemoved;
    public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
    public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

    public DeliveryEntry(Owl owl, Minecraft minecraft, UUID pId, String playerName, Supplier<ResourceLocation> skinGetter) {
        this.minecraft = minecraft;
        this.id = pId;
        this.canSend = () -> !owl.getMainHandItem().isEmpty();
        this.playerName = playerName;
        this.skinGetter = skinGetter;
        boolean flag1 = !minecraft.player.getUUID().equals(pId);
        if (flag1) {
            this.deliverButton = Button.builder(Component.translatableWithFallback("gui.delivery.send", "Send!"), button -> {
                NetworkChannel.CHANNEL.sendToServer(new DeliveryPacket(pId, owl.getUUID()));
                Minecraft.getInstance().setScreen(null);
            }).pos(0, 0).size(40, 20).build();
            this.children = new ArrayList<>();
            this.children.add(this.deliverButton);
        } else {
            this.children = ImmutableList.of();
        }

    }

    public void render(@NotNull GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
        int i = pLeft + 4;
        int j = pTop + (pHeight - 24) / 2;
        int k = i + 24 + 4;
        int l;
        pGuiGraphics.fill(pLeft, pTop, pLeft + pWidth, pTop + pHeight, BG_FILL);
        l = pTop + (pHeight - 9) / 2;

        PlayerFaceRenderer.draw(pGuiGraphics, this.skinGetter.get(), i, j, 24);
        pGuiGraphics.drawString(this.minecraft.font, this.playerName, k, l, PLAYERNAME_COLOR, false);
        if (this.isRemoved) {
            pGuiGraphics.fill(i, j, i + 24, j + 24, SKIN_SHADE);
        }

        if (deliverButton != null) {
            this.deliverButton.active = canSend.getAsBoolean();
            deliverButton.setX(pLeft + pWidth - 50);
            deliverButton.setY(pTop + pHeight / 2 - deliverButton.getHeight()/2);
            deliverButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    public @NotNull List<? extends GuiEventListener> children() {
        return this.children;
    }

    public @NotNull List<? extends NarratableEntry> narratables() {
        return this.children;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public UUID getPlayerId() {
        return this.id;
    }

    public void setRemoved(boolean pIsRemoved) {
        this.isRemoved = pIsRemoved;
    }

    public boolean isRemoved() {
        return this.isRemoved;
    }
}
