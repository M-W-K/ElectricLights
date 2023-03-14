package com.m_w_k.electriclights.blockentity;

import com.m_w_k.electriclights.ELConfig;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.gui.menu.SolarMenu;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SolarBlockEntity extends ExtendableGeneratorBlockEntity{

    public SolarBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ELBlockEntityRegistry.SOLAR_GENERATOR.get());
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("menu." + ElectricLightsMod.MODID + ".solar");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new SolarMenu(id, playerInventory, this, this.dataAccess);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarBlockEntity self) {
        if (!level.isClientSide()) {
            // buff our capacity based on production
            int trueEnergyCap = self.softEnergyCap * 1000 * (2 + self.extensionPositions.size());
            if (trueEnergyCap > self.energyGenerated && level.canSeeSky(pos.above())) {
                self.misc = solarCoefficient(level);
                int operable = 2;
                for (BlockPos extension : self.extensionPositions) {
                    if (level.canSeeSky(extension.above())) operable += 1;
                }
                self.energyGenerated += (operable * ELConfig.SERVER.solarEnergyFactor() * self.misc) / 1000; // base production at 10 panels, or 8 extensions
            }
        }
    }

    /**
     * Finds a solar coefficient from 0 to 100 based on the time of day.
     */
    private static int solarCoefficient(Level level) {
        long time = level.getDayTime();
        int darkBright = (int) Math.floor(level.getMoonBrightness() * 8 + 1); // max night brightness is 9, varies based on moon phase
        double factor;
        if (time < 12000) { // day
            return 100;
        } else if (time < 13000) { // dusk
            // interpolate between day and night brightness
            factor = (time - 12000) / 1000f;
            return (int) Math.floor(100 * (1 - factor) + darkBright * factor);
        } else if (time < 23000) { // night
            return darkBright;
        } else { // sunrise
            // interpolate between night and day brightness
            factor = (time - 23000) / 1000f;
            return (int) Math.floor(darkBright * (1 - factor) + 100 * factor);
        }
    }

    @Override
    public void noteExtension(boolean registerNew, BlockPos pos) {
        super.noteExtension(registerNew, pos);
    }
}
