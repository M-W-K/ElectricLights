package com.m_w_k.electriclights.gui.menu;

import com.m_w_k.electriclights.ELConfig;
import com.m_w_k.electriclights.blockentity.ExtendableGeneratorBlockEntity;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import com.m_w_k.electriclights.registry.ELGUIRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;

public class SolarMenu extends AbstractExtendableGeneratorMenu {

    public SolarMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory, ELGUIRegistry.SOLAR_MENU.get());
    }

    public SolarMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ELGUIRegistry.SOLAR_MENU.get());
    }

    public int getSolarFactor() {
        return this.data.get(1);
    }
    public double getEnergyScaled() {
        return getEnergy() / (ExtendableGeneratorBlockEntity.getSoftEnergyCap() * (ELConfig.SERVER.solarEnergyFactor() / 2d) * (2d + getExtensionsCount()) + MasterSwitchboardBlockEntity.getMaxEnergy());
    }
}
