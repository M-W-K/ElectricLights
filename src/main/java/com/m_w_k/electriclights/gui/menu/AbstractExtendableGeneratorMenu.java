package com.m_w_k.electriclights.gui.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractExtendableGeneratorMenu extends AbstractContainerMenu {
    public final Container container;
    public final ContainerData data;
    public final Level level;

    public AbstractExtendableGeneratorMenu(int containerId, Inventory playerInventory, MenuType<?> menuType) {
        this(containerId, playerInventory, new SimpleContainer(0), new SimpleContainerData(3), menuType);
    }

    protected AbstractExtendableGeneratorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, MenuType<?> menuType) {
        super(menuType, containerId);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        this.level = playerInventory.player.getLevel();
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
            if (index >= 0 && index < 27) {
                if (!this.moveItemStackTo(itemStack1, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 27 && index < 36 && !this.moveItemStackTo(itemStack1, 0, 27, false)) {
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

    public int getExtensionsCount() {
        return this.data.get(0);
    }
    public int getEnergy() {
        return this.data.get(2);
    }
}
