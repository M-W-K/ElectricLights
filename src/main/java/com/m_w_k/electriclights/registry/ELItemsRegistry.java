package com.m_w_k.electriclights.registry;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.item.BurnOutAbleLightBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.m_w_k.electriclights.registry.ELBlockRegistry.*;

public class ELItemsRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ElectricLightsMod.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ElectricLightsMod.MODID);

    public static final RegistryObject<Item> REDSTONE_SILICATE = ITEMS.register("redstone_silicate", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REDSTONE_BULB = ITEMS.register("redstone_bulb", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REDSTONE_CHIP = ITEMS.register("redstone_chip", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DRAGON_BULB = ITEMS.register("dragon_bulb", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILICATE_BOARD = ITEMS.register("silicate_board", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_LIGHT_0_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_0", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),0));
    public static final RegistryObject<Item> ELECTRIC_LIGHT_1_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_1", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),1));
    public static final RegistryObject<Item> ELECTRIC_LIGHT_2_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_2", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),2));
    public static final RegistryObject<Item> ELECTRIC_LIGHT_3_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_3", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),3));
    public static final RegistryObject<Item> ELECTRIC_LIGHT_4_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_4", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),4));
    public static final RegistryObject<Item> ELECTRIC_LIGHT_5_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_5", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),5));
    public static final RegistryObject<Item> ELECTRIC_LIGHT_6_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_6", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),6));
    public static final RegistryObject<Item> ELECTRIC_LIGHT_7_BLOCK_ITEM = BLOCK_ITEMS.register("electric_light_7", () -> new BurnOutAbleLightBlockItem(ELECTRIC_LIGHT.get(), new Item.Properties(),7));
    public static final RegistryObject<Item> DRAGON_LIGHT_BLOCK_ITEM = BLOCK_ITEMS.register("dragon_light", () -> new BlockItem(DRAGON_LIGHT.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELECTRIC_RELAY_BLOCK_ITEM = BLOCK_ITEMS.register("electric_relay", () -> new BlockItem(ELECTRIC_RELAY.get(), new Item.Properties()));
    public static final RegistryObject<Item> SWITCHBOARD_BLOCK_ITEM = BLOCK_ITEMS.register("master_switchboard", () -> new BlockItem(SWITCHBOARD_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> ALTERNATOR_BLOCK_ITEM = BLOCK_ITEMS.register("alternator_generator", () -> new BlockItem(ALTERNATOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> SOLAR_BLOCK_ITEM = BLOCK_ITEMS.register("solar_generator", () -> new BlockItem(SOLAR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> GEOTHERMAL_BLOCK_ITEM = BLOCK_ITEMS.register("geothermal_generator", () -> new BlockItem(GEOTHERMAL_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> SOLAR_EXTENSION_BLOCK_ITEM = BLOCK_ITEMS.register("solar_extension", () -> new BlockItem(SOLAR_EXTENSION_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> GEOTHERMAL_EXTENSION_BLOCK_ITEM = BLOCK_ITEMS.register("geothermal_extension", () -> new BlockItem(GEOTHERMAL_EXTENSION_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_L_BLOCK_ITEM = BLOCK_ITEMS.register("voltage_coil_l", () -> new BlockItem(VOLTAGE_COIL_L_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_M_BLOCK_ITEM = BLOCK_ITEMS.register("voltage_coil_m", () -> new BlockItem(VOLTAGE_COIL_M_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> VOLTAGE_COIL_H_BLOCK_ITEM = BLOCK_ITEMS.register("voltage_coil_h", () -> new BlockItem(VOLTAGE_COIL_H_BLOCK.get(), new Item.Properties()));

    static final List<RegistryObject<Item>> tabItems = new ArrayList<>(Arrays.asList(
            REDSTONE_SILICATE,
            SILICATE_BOARD,
            REDSTONE_CHIP,
            REDSTONE_BULB,
            DRAGON_BULB,

            ELECTRIC_LIGHT_0_BLOCK_ITEM,
            DRAGON_LIGHT_BLOCK_ITEM,
            ELECTRIC_RELAY_BLOCK_ITEM,
            SWITCHBOARD_BLOCK_ITEM,
            VOLTAGE_COIL_L_BLOCK_ITEM,
            VOLTAGE_COIL_M_BLOCK_ITEM,
            VOLTAGE_COIL_H_BLOCK_ITEM,
            ALTERNATOR_BLOCK_ITEM,
            SOLAR_BLOCK_ITEM,
            GEOTHERMAL_BLOCK_ITEM,
            SOLAR_EXTENSION_BLOCK_ITEM,
            GEOTHERMAL_EXTENSION_BLOCK_ITEM
    ));

    protected static void addCreative(CreativeModeTabEvent.BuildContents event)
    {

        if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS) {
            for (RegistryObject<Item> item : tabItems) {
                event.accept(item);
            }
        }
    }
}
