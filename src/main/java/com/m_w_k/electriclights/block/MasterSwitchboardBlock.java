package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.GraphNode;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MasterSwitchboardBlock extends BaseEntityBlock {

    public MasterSwitchboardBlock(Properties properties) {
        super(properties);
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new MasterSwitchboardBlockEntity(pos,state);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ElectricLightsMod.MASTER_SWITCHBOARD.get(), MasterSwitchboardBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (!level.isClientSide()) {
            ElectricLightsMod.addGraphNodeAndAutoConnect(new GraphNode(pos, false, true), level);
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
                ElectricLightsMod.removeGraphNode(new GraphNode(pos, false, true), level);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    // TODO delete this once power generation is working
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity != null && entity.getClass() == MasterSwitchboardBlockEntity.class) {
                MasterSwitchboardBlockEntity switchboard = (MasterSwitchboardBlockEntity) entity;
                switchboard.receiveEnergy(800, false);
            }
            return InteractionResult.CONSUME;
        } else return InteractionResult.SUCCESS;
    }
}
