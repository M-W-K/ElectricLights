package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.util.GraphNode;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, ELBlockEntityRegistry.MASTER_SWITCHBOARD.get(), MasterSwitchboardBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (!level.isClientSide()) {
            ELGraphHandler.addGraphNodeAndAutoConnect(new GraphNode(pos, GraphNode.NodeType.SWITCHBOARD), level);
            ElectricLightsMod.manageLoadedChunks((ServerLevel) level, pos, true);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                ELGraphHandler.removeGraphNode(new GraphNode(pos, GraphNode.NodeType.SWITCHBOARD), level);
                ElectricLightsMod.manageLoadedChunks((ServerLevel) level, pos, false);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
