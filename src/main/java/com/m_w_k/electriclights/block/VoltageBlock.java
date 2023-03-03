package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoltageBlock extends Block {
    private final int voltage; // voltage determines light brightness and energy consumption, must be between -1 and 4 else things will break
    public VoltageBlock(Properties properties, int voltage) {
        super(properties);
        this.voltage = voltage;
    }
    public int getVoltage() {
        return voltage;
    }
    private void refreshAboveSwitchboard(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos.above());
        if (blockEntity != null && blockEntity.getClass() == MasterSwitchboardBlockEntity.class) {
            MasterSwitchboardBlockEntity switchboard = (MasterSwitchboardBlockEntity) blockEntity;
            switchboard.refresh(level);
        }
    }
    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (!level.isClientSide()) {
            refreshAboveSwitchboard(level, pos);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                refreshAboveSwitchboard(level, pos);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
