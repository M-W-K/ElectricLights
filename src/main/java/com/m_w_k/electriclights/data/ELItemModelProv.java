package com.m_w_k.electriclights.data;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.registry.ELBlockRegistry;
import com.m_w_k.electriclights.registry.ELItemsRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ELItemModelProv extends ItemModelProvider {
    public ELItemModelProv(PackOutput output,ExistingFileHelper existingFileHelper) {
        super(output, ElectricLightsMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleBlockItem(ELBlockRegistry.VOLTAGE_COIL_L_BLOCK, ELItemsRegistry.VOLTAGE_COIL_L_BLOCK_ITEM);
        simpleBlockItem(ELBlockRegistry.VOLTAGE_COIL_M_BLOCK, ELItemsRegistry.VOLTAGE_COIL_M_BLOCK_ITEM);
        simpleBlockItem(ELBlockRegistry.VOLTAGE_COIL_H_BLOCK, ELItemsRegistry.VOLTAGE_COIL_H_BLOCK_ITEM);
        item(ELItemsRegistry.DRAGON_LIGHT_BLOCK_ITEM, "item/light_display_blueprint", "block/light_4", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_0_BLOCK_ITEM, "item/light_display_blueprint", "block/light_4", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_1_BLOCK_ITEM, "item/light_display_blueprint", "block/light_3", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_2_BLOCK_ITEM, "item/light_display_blueprint", "block/light_3", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_3_BLOCK_ITEM, "item/light_display_blueprint", "block/light_3", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_4_BLOCK_ITEM, "item/light_display_blueprint", "block/light_2", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_5_BLOCK_ITEM, "item/light_display_blueprint", "block/light_2", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_6_BLOCK_ITEM, "item/light_display_blueprint", "block/light_2", "2");
        item(ELItemsRegistry.ELECTRIC_LIGHT_7_BLOCK_ITEM, "item/light_display_blueprint", "block/light_0", "2");

        simpleItem(ELItemsRegistry.REDSTONE_SILICATE, "item/redstone_silicate");
        simpleItem(ELItemsRegistry.REDSTONE_CHIP, "item/redstone_chip");
        simpleItem(ELItemsRegistry.SILICATE_BOARD, "item/silicate_board");
        simpleItem(ELItemsRegistry.REDSTONE_BULB, "item/redstone_bulb");
        simpleItem(ELItemsRegistry.DRAGON_BULB, "item/dragon_bulb");
    }

    public void simpleBlockItem(RegistryObject<Block> blockRegistryObject, RegistryObject<Item> itemRegistryObject, String texturePath, String textureNum) {
        String pathBlock = blockRegistryObject.getId().getPath();
        String pathItem = itemRegistryObject.getId().getPath();
        texturePath = "electriclights:" + texturePath;
        this.withExistingParent(pathItem, "electriclights:block/".concat(pathBlock))
                .texture(textureNum, texturePath);
    }
    public void simpleBlockItem(RegistryObject<Block> blockRegistryObject, RegistryObject<Item> itemRegistryObject) {
        String pathBlock = blockRegistryObject.getId().getPath();
        String pathItem = itemRegistryObject.getId().getPath();
        this.withExistingParent(pathItem, "electriclights:block/".concat(pathBlock));
    }
    public void item(RegistryObject<Item> registryObject, String parentModelPath, String texturePath, String textureNum) {
        String pathItem = registryObject.getId().getPath();
        texturePath = "electriclights:" + texturePath;
        this.withExistingParent(pathItem, "electriclights:".concat(parentModelPath))
                .texture(textureNum, texturePath);
    }
    public void simpleItem(RegistryObject<Item> registryObject, String texturePath) {
        String pathItem = registryObject.getId().getPath();
        texturePath = "electriclights:" + texturePath;
        this.withExistingParent(pathItem, "minecraft:item/generated")
                .texture("layer0", texturePath);
    }
}
