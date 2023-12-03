package io.github.itskillerluc.recrafted_creatures.screen.component;

import com.google.common.collect.Lists;
import io.github.itskillerluc.recrafted_creatures.entity.Owl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import javax.annotation.Nullable;
import java.util.*;

public class DeliveryList extends ContainerObjectSelectionList<DeliveryEntry> {
    private final List<DeliveryEntry> players = Lists.newArrayList();
    @Nullable
    private String filter;
    private final Owl owl;

    public DeliveryList(Owl owl, Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
        this.owl = owl;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    protected void enableScissor(GuiGraphics pGuiGraphics) {
        pGuiGraphics.enableScissor(this.x0, this.y0 + 4, this.x1, this.y1);
    }

    public void updatePlayerList(Collection<UUID> pIds, double pScrollAmount) {
        Map<UUID, DeliveryEntry> map = new HashMap<>();
        this.addOnlinePlayers(pIds, map);
        this.updateFiltersAndScroll(map.values(), pScrollAmount);
    }

    private void addOnlinePlayers(Collection<UUID> pIds, Map<UUID, DeliveryEntry> deliveryMap) {
        ClientPacketListener clientpacketlistener = this.minecraft.player.connection;

        for(UUID uuid : pIds) {
            PlayerInfo playerinfo = clientpacketlistener.getPlayerInfo(uuid);
            if (playerinfo != null) {
                deliveryMap.put(uuid, new DeliveryEntry(owl ,this.minecraft, uuid, playerinfo.getProfile().getName(), playerinfo::getSkinLocation));
            }
        }

    }

    private void sortPlayerEntries() {
        this.players.sort(Comparator.comparing(DeliveryEntry::getPlayerName));
    }

    private void updateFiltersAndScroll(Collection<DeliveryEntry> pPlayers, double pScrollAmount) {
        this.players.clear();
        this.players.addAll(pPlayers);
        this.sortPlayerEntries();
        this.updateFilteredPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(pScrollAmount);
    }

    private void updateFilteredPlayers() {
        if (this.filter != null) {
            this.players.removeIf(entry -> !entry.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.players);
        }

    }

    public void setFilter(String pFilter) {
        this.filter = pFilter;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public void addPlayer(PlayerInfo pPlayerInfo) {
        UUID uuid = pPlayerInfo.getProfile().getId();

        for(DeliveryEntry entry : this.players) {
            if (entry.getPlayerId().equals(uuid)) {
                entry.setRemoved(false);
                return;
            }
        }

        boolean flag = pPlayerInfo.hasVerifiableChat();
        DeliveryEntry deliveryEntry = new DeliveryEntry(owl, this.minecraft, pPlayerInfo.getProfile().getId(), pPlayerInfo.getProfile().getName(), pPlayerInfo::getSkinLocation);
        this.addEntry(deliveryEntry);
        this.players.add(deliveryEntry);
    }

    public void removePlayer(UUID pId) {
        for(DeliveryEntry entry : this.players) {
            if (entry.getPlayerId().equals(pId)) {
                entry.setRemoved(true);
                return;
            }
        }
    }
}
