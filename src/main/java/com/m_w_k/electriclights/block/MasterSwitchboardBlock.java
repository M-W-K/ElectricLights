package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.util.GraphNode;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MasterSwitchboardBlock extends BaseEntityBlock {
    public static final BooleanProperty DISABLED = BooleanProperty.create("disabled");

    public MasterSwitchboardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(DISABLED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(DISABLED);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(DISABLED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
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

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block p_55669_, BlockPos p_55670_, boolean p_55671_) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(DISABLED);
            if (flag != level.hasNeighborSignal(pos)) toggleState(state, level, pos);
        }
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_54644_, BlockHitResult p_54645_) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockState blockstate = this.toggleState(state, level, pos);
            float f = blockstate.getValue(DISABLED) ? 0.6F : 0.5F;
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            level.gameEvent(player, blockstate.getValue(DISABLED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
            return InteractionResult.CONSUME;
        }
    }

    private BlockState toggleState(BlockState state, Level level, BlockPos pos) {
        state = state.cycle(DISABLED);
        level.setBlock(pos, state, 2);
        if (level.getBlockEntity(pos) instanceof MasterSwitchboardBlockEntity switchboard) switchboard.forceUpdate();
        return state;
    }
}
