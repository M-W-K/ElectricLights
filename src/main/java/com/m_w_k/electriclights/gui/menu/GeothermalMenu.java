package com.m_w_k.electriclights.gui.menu;

import com.m_w_k.electriclights.registry.ELGUIRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class GeothermalMenu extends AbstractExtendableGeneratorMenu {

    public GeothermalMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory, ELGUIRegistry.GEOTHERMAL_MENU.get());
    }

    public GeothermalMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ELGUIRegistry.GEOTHERMAL_MENU.get());
    }

    public int getLavaFactor() {
        return this.data.get(1);
    }
}
