package io.github.itskillerluc.recrafted_creatures.menu;

import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.FetchBlocksPacket;
import io.github.itskillerluc.recrafted_creatures.registries.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class BeaverMenu extends AbstractContainerMenu {
    public final List<ResourceLocation> structures;
    public final Beaver beaver;
    private final List<ItemStack> neededMaterials;

    public BeaverMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf extraData) {
        this(pContainerId, pPlayerInventory, extraData.readList(FriendlyByteBuf::readResourceLocation), (Beaver) pPlayerInventory.player.level().getEntity(extraData.readInt()), extraData.readList(FriendlyByteBuf::readItem));
    }
    public BeaverMenu(int pContainerId, Inventory pPlayerInventory, List<ResourceLocation> structures, Beaver beaver, List<ItemStack> neededMaterials) {
        super(MenuRegistry.BEAVER_MENU.get(), pContainerId);
        this.structures = structures;
        this.beaver = beaver;
        this.neededMaterials = neededMaterials;

        final var yOffset = 97;
        final var xOffset = 49;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, xOffset + j * 18, yOffset + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pPlayerInventory, k, xOffset + k * 18, yOffset + 58));
        };
        addSlot(new Slot(beaver.inventory, 0, 120, 15) {
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return beaver.requiredMaterials.stream().anyMatch(item -> item.is(pStack.getItem()) && 0 != item.getCount() - beaver.materials.stream().filter(itemm -> itemm.is(item.getItem())).findFirst().orElse(new ItemStack(Items.AIR, 0)).getCount());
            }

            @Override
            public void setChanged() {
                super.setChanged();
                NetworkChannel.CHANNEL.sendToServer(new FetchBlocksPacket(new ResourceLocation(beaver.getEntityData().get(Beaver.BUILD_NAME)), beaver.getId()));
            }

            @Override
            public boolean isActive() {
                return !beaver.getEntityData().get(Beaver.BUILD_NAME).isEmpty();
            }
        });
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 1;  // must be the number of slots you have!
    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return beaver.isAlive() && this.beaver.distanceTo(pPlayer) < 15.0F;
    }

    public List<ItemStack> getNeededMaterials() {
        return neededMaterials.stream().filter(item -> item.getCount() > 0).toList();
    }
}
