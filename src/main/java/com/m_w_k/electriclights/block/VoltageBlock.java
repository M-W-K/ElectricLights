package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoltageBlock extends Block {
    protected static final VoxelShape COIL_AABB = Shapes.or(
            Block.box(0,14,0,16,16,16),
            Block.box(0,0,0,16,2,16),
            Block.box(1,13,1,15,14,15),
            Block.box(1,2,1,15,3,15),
            Block.box(2,3,2,14,13,14));
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
        if (blockEntity instanceof MasterSwitchboardBlockEntity switchboard) {
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

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_153475_, BlockPos p_153476_, CollisionContext p_153477_) {
        return COIL_AABB;
    }
}
