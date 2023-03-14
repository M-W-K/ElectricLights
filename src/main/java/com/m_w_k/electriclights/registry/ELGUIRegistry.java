package com.m_w_k.electriclights.registry;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.gui.menu.AlternatorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ELGUIRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ElectricLightsMod.MODID);

    public static final RegistryObject<MenuType<AlternatorMenu>> ALTERNATOR_MENU = MENU_TYPES.register("alternator_generator", () -> new MenuType<>(AlternatorMenu::new));
}
