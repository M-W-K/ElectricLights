package com.m_w_k.electriclights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public interface ELBreaker {
    BooleanProperty DISABLED = ELBlockStateProperties.DISABLED;
    BooleanProperty POWERED = BlockStateProperties.POWERED;

    default InteractionResult used(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_54644_, BlockHitResult p_54645_) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockState blockstate = this.toggleDisabled(state, level, pos);
            level.gameEvent(player, blockstate.getValue(DISABLED) ? GameEvent.BLOCK_DEACTIVATE : GameEvent.BLOCK_ACTIVATE, pos);
            return InteractionResult.CONSUME;
        }
    }

    default void neighborChange(BlockState state, Level level, BlockPos pos, Block p_55669_, BlockPos p_55670_, boolean p_55671_) {
        if (!level.isClientSide) {
            boolean flag = level.hasNeighborSignal(pos);
            if (flag != state.getValue(POWERED)) {
                state = state.setValue(POWERED, flag);
                if (state.getValue(DISABLED) != flag) state = setDisabled(state, level, pos, flag);
                else level.setBlock(pos, state, 2);

                if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                    level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
                }
            }
        }
    }

    default BlockState toggleDisabled(BlockState state, Level level, BlockPos pos) {
        return setDisabled(state, level, pos, !state.getValue(DISABLED));
    }

    default BlockState setDisabled(BlockState state, Level level, BlockPos pos, boolean disabled) {
        state = state.setValue(DISABLED, disabled);
        level.setBlock(pos, state, 2);
        // TODO get a custom breaker sound
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, state.getValue(DISABLED) ? 0.5F : 0.6F);
        return state;
    }
}
