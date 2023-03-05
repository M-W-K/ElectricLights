package com.m_w_k.electriclights.Alternator;

import com.m_w_k.electriclights.Alternator.AlternatorFuelSlot;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.blockentity.AlternatorBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AlternatorMenu extends AbstractContainerMenu {
    public final Container container;
    public final ContainerData data;
    public final Level level;

    public AlternatorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(2));
    }

    public AlternatorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ElectricLightsMod.ALTERNATOR_MENU.get(), containerId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        this.level = playerInventory.player.getLevel();
        this.addSlot(new AlternatorFuelSlot(this, container, 0, 73, 53));
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
                } else if (index >= 28 && index < 37 && !this.moveItemStackTo(itemStack1, 2, 29, false)) {
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
        if (this.container instanceof AlternatorBlockEntity alternatorBlock) {
            return alternatorBlock.isLit();
        } else return false;
    }
    public int getLitTimeRemainingScaled() {
        if (this.container instanceof AlternatorBlockEntity alternatorBlock) {
            return (alternatorBlock.getLitDuration() - alternatorBlock.getLitTime()) / alternatorBlock.getLitDuration();
        } else return 0;
    }
}
