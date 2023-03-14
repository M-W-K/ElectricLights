package com.m_w_k.electriclights.registry;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.blockentity.AlternatorBlockEntity;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import com.m_w_k.electriclights.blockentity.SolarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.m_w_k.electriclights.registry.ELBlockRegistry.*;

public class ELBlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ElectricLightsMod.MODID);

    public static final RegistryObject<BlockEntityType<MasterSwitchboardBlockEntity>> MASTER_SWITCHBOARD = BLOCK_ENTITIES.register("master_switchboard", () -> BlockEntityType.Builder.of(MasterSwitchboardBlockEntity::new, SWITCHBOARD_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<AlternatorBlockEntity>> ALTERNATOR_GENERATOR = BLOCK_ENTITIES.register("alternator_generator", () -> BlockEntityType.Builder.of(AlternatorBlockEntity::new, ALTERNATOR_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarBlockEntity>> SOLAR_GENERATOR = BLOCK_ENTITIES.register("solar_generator", () -> BlockEntityType.Builder.of(SolarBlockEntity::new, SOLAR_BLOCK.get()).build(null));
}
