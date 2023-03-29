package com.m_w_k.electriclights.registry;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.gui.menu.AlternatorMenu;
import com.m_w_k.electriclights.gui.menu.GeothermalMenu;
import com.m_w_k.electriclights.gui.menu.SolarMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ELGUIRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ElectricLightsMod.MODID);

    public static final RegistryObject<MenuType<AlternatorMenu>> ALTERNATOR_MENU = MENU_TYPES.register("alternator_generator", () -> new MenuType<>(AlternatorMenu::new));
    public static final RegistryObject<MenuType<SolarMenu>> SOLAR_MENU = MENU_TYPES.register("solar_generator", () -> new MenuType<>(SolarMenu::new));
    public static final RegistryObject<MenuType<GeothermalMenu>> GEOTHERMAL_MENU = MENU_TYPES.register("geothermal_generator", () -> new MenuType<>(GeothermalMenu::new));
}
