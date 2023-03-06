package com.m_w_k.electriclights.registry;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.block.AlternatorBlock;
import com.m_w_k.electriclights.block.ElectricRelayBlock;
import com.m_w_k.electriclights.block.MasterSwitchboardBlock;
import com.m_w_k.electriclights.block.VoltageBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ELBlocksRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ElectricLightsMod.MODID);

    public static final RegistryObject<Block> ELECTRIC_LIGHT = BLOCKS.register("electric_light", () -> new ElectricRelayBlock(Block.Properties.copy(Blocks.LANTERN).lightLevel((a) -> 0), true));
    public static final RegistryObject<Block> ELECTRIC_RELAY = BLOCKS.register("electric_relay", () -> new ElectricRelayBlock(Block.Properties.copy(Blocks.LANTERN).lightLevel((a) -> 0).sound(SoundType.BAMBOO)));
    public static final RegistryObject<Block> SWITCHBOARD_BLOCK = BLOCKS.register("master_switchboard", () -> new MasterSwitchboardBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL)));
    public static final RegistryObject<Block> ALTERNATOR_BLOCK = BLOCKS.register("alternator_generator", () -> new AlternatorBlock(Block.Properties.copy(Blocks.BLAST_FURNACE)));
    public static final RegistryObject<Block> VOLTAGE_COIL_L_BLOCK = BLOCKS.register("voltage_coil_l", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.COPPER), 2));
    public static final RegistryObject<Block> VOLTAGE_COIL_M_BLOCK = BLOCKS.register("voltage_coil_m", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.COPPER), 3));
    public static final RegistryObject<Block> VOLTAGE_COIL_H_BLOCK = BLOCKS.register("voltage_coil_h", () -> new VoltageBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.COPPER), 4));

}
