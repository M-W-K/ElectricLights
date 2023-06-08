package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.util.ELBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ElectricRelayBlock extends AbstractRelayBlock {
    public static final BooleanProperty DISABLED = ELBlockStateProperties.DISABLED;

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        int light = state.getValue(LIGHTSTATE);
        boolean disabled = state.getValue(DISABLED);
        return (!disabled && light == 0) ? 0 : 1;
    }

    public ElectricRelayBlock(Properties properties) {
        super(properties, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(DISABLED);
        super.createBlockStateDefinition(blockStateBuilder);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(DISABLED,false);
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
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, blockstate.getValue(DISABLED) ? 0.5F : 0.6F);
            level.gameEvent(player, blockstate.getValue(DISABLED) ? GameEvent.BLOCK_DEACTIVATE : GameEvent.BLOCK_ACTIVATE, pos);
            return InteractionResult.CONSUME;
        }
    }

    private BlockState toggleState(BlockState state, Level level, BlockPos pos) {
        state = state.cycle(DISABLED);
        boolean disabled = state.getValue(DISABLED);
        // disconnect ourselves from the network if disabled, or reconnect if we've been enabled
        handleSelfGraphNode(level, pos, !disabled);
        if (disabled) state = state.setValue(LIGHTSTATE, 0);
        level.setBlock(pos, state, 2);
        return state;
    }
}
