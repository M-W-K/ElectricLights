package com.m_w_k.electriclights.registry;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ELBlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ElectricLightsMod.MODID);

    private static final BlockBehaviour.Properties METALLIC = Block.Properties.copy(Blocks.LANTERN).lightLevel((a) -> 0);
    private static final BlockBehaviour.Properties WOODEN = METALLIC.sound(SoundType.BAMBOO);
    private static final BlockBehaviour.Properties HEAVY_COPPER = BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.COPPER);

    public static final RegistryObject<Block> ELECTRIC_LIGHT = BLOCKS.register("electric_light", () -> new BurnOutAbleLightBlock(METALLIC));
    public static final RegistryObject<Block> DRAGON_LIGHT = BLOCKS.register("dragon_light", () -> new BaseLightBlock(METALLIC));
    public static final RegistryObject<Block> ELECTRIC_RELAY = BLOCKS.register("electric_relay", () -> new ElectricRelayBlock(WOODEN));
    public static final RegistryObject<Block> SWITCHBOARD_BLOCK = BLOCKS.register("master_switchboard", () -> new MasterSwitchboardBlock(HEAVY_COPPER));
    public static final RegistryObject<Block> ALTERNATOR_BLOCK = BLOCKS.register("alternator_generator", () -> new AlternatorBlock(Block.Properties.copy(Blocks.BLAST_FURNACE)));
    public static final RegistryObject<Block> SOLAR_BLOCK = BLOCKS.register("solar_generator", () -> new SolarBlock(Block.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Block> SOLAR_EXTENSION_BLOCK = BLOCKS.register("solar_extension", () -> new GeneratorExtensionBlock(Block.Properties.copy(Blocks.IRON_BLOCK), ExtendableGeneratorBlock.GeneratorType.SOLAR));
    public static final RegistryObject<Block> VOLTAGE_COIL_L_BLOCK = BLOCKS.register("voltage_coil_l", () -> new VoltageBlock(HEAVY_COPPER, 2));
    public static final RegistryObject<Block> VOLTAGE_COIL_M_BLOCK = BLOCKS.register("voltage_coil_m", () -> new VoltageBlock(HEAVY_COPPER, 3));
    public static final RegistryObject<Block> VOLTAGE_COIL_H_BLOCK = BLOCKS.register("voltage_coil_h", () -> new VoltageBlock(HEAVY_COPPER, 4));

}
