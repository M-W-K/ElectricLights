package com.m_w_k.electriclights.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.m_w_k.electriclights.ELConfig;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.gui.menu.GeothermalMenu;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.Map;

public class GeothermalBlockEntity extends ExtendableGeneratorBlockEntity{
    private final static Map<Block, Integer> conductanceMap = Maps.newHashMap(ImmutableMap.of(
            Blocks.MAGMA_BLOCK, 1,
            Blocks.OBSIDIAN, 3,
            Blocks.NETHERRACK, 3,
            Blocks.DRIPSTONE_BLOCK, 4,
            Blocks.STONE, 5,
            Blocks.GRANITE, 5,
            Blocks.DIORITE, 5,
            Blocks.ANDESITE, 5,
            Blocks.AIR, 7
    ));

    private final static int tickRate = 20;
    private int ticks = 0;
    private final static int conductanceLimit = 15;
    private final static double optimalFactor = 1.2;

    public GeothermalBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ELBlockEntityRegistry.GEOTHERMAL_GENERATOR.get());
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("menu." + ElectricLightsMod.MODID + ".geothermal");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new GeothermalMenu(id, playerInventory, this, this.dataAccess);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeothermalBlockEntity self) {
        if (!level.isClientSide()) {
            if (tickRate <= self.ticks) {
                self.ticks = 0;
                if (softEnergyCap * tickRate > self.energyGenerated) {
                    double factor = lavaFactor(level, pos) * 2;
                    for (BlockPos extension : self.extensionPositions) {
                        factor += lavaFactor(level, extension);
                    }
                    self.misc = (int) (factor * 100 / (2d + self.extensionPositions.size()));
                    self.energyGenerated += factor * ELConfig.SERVER.geothermalEnergyFactor() * tickRate;
                }
            } else self.ticks ++;
        }
    }

    /**
     * Finds a lava factor from 0 to optimalFactor based on lava underneath the provided position
     */
    private static double lavaFactor(Level level, BlockPos pos) {
        boolean searching = true;
        double lavaCount = 0;
        int limit = conductanceLimit;
        while (searching) {
            pos = pos.below();
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.is(Fluids.LAVA)) lavaCount += 1;
            else if (fluidState.is(Fluids.FLOWING_LAVA)) lavaCount += 0.5;
            else if (fluidState.is(Fluids.EMPTY)) {
                Block block = level.getBlockState(pos).getBlock();
                searching = false;
                Integer conductance = conductanceMap.get(block);
                if (conductance != null) {
                    limit -= conductance;
                    lavaCount += Math.max(0, 3 - conductance) / 10d;
                    if (limit > 0) searching = true;
                }
            }
        }
        return optimalFactor - optimalFactor / (lavaCount + 1);
    }
}
