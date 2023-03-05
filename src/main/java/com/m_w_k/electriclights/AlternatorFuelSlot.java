package com.m_w_k.electriclights;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlternatorFuelSlot extends Slot {
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
