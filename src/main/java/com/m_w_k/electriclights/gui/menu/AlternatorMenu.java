package com.m_w_k.electriclights.gui.menu;

import com.m_w_k.electriclights.blockentity.AlternatorBlockEntity;
import com.m_w_k.electriclights.registry.ELGUIRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AlternatorMenu extends AbstractContainerMenu {
    public final Container container;
    public final ContainerData data;
    public final Level level;

    public AlternatorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(3));
    }

    public AlternatorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ELGUIRegistry.ALTERNATOR_MENU.get(), containerId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        this.level = playerInventory.player.getLevel();
        this.addSlot(new AlternatorFuelSlot(this, container, 0, 67, 51));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemStack = itemStack1.copy();
            if (index != 0) {
                if (this.isFuel(itemStack1)) {
                    if (!this.moveItemStackTo(itemStack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 1 && index < 28) {
                    if (!this.moveItemStackTo(itemStack1, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 28 && index < 37 && !this.moveItemStackTo(itemStack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack1, 1, 37, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }
            if (itemStack1.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack1);
        }
        return itemStack;
    }

    public boolean isFuel(ItemStack stack) {
        return AlternatorBlockEntity.burnable(stack);
    }
    public boolean isLit() {
        return this.data.get(0) > 0;
    }
    public int getLitTimeRemainingScaled() {
        // prevent / by zero crash in edge cases
        if (this.data.get(1) == 0) return 0;

        return getLitTimeRemaining() * 10 / this.data.get(1);
    }
    public int getLitTimeRemaining() {
        return this.data.get(1) - this.data.get(0);
    }
    public int getEnergyStored() {
        return this.data.get(2);
    }

    private static class AlternatorFuelSlot extends Slot {
        private final AlternatorMenu menu;

        public AlternatorFuelSlot(AlternatorMenu menu, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.menu.isFuel(stack);
        }
    }
}
