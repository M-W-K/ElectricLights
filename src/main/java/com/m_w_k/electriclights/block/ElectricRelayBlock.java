package com.m_w_k.electriclights.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.m_w_k.electriclights.util.ELBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ElectricRelayBlock extends AbstractRelayBlock {
    protected static final Map<Direction, VoxelShape> RELAY_FLOOR_AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.SOUTH, Shapes.or(
                    Block.box(2, 0, 1, 14, 2, 15),
                    Block.box(10, 2, 10, 12, 3, 12),
                    Block.box(6, 2, 4, 10, 3, 6),
                    Block.box(14, 0, 8, 15, 2, 11)),
            Direction.WEST, Shapes.or(
                    Block.box(1, 0, 2, 15, 2, 14),
                    Block.box(4, 2, 10, 6, 3, 12),
                    Block.box(10, 2, 6, 12, 3, 10),
                    Block.box(5, 0, 14, 8, 2, 15)),
            Direction.NORTH, Shapes.or(
                    Block.box(2, 0, 1, 14, 2, 15),
                    Block.box(4, 2, 4, 6, 3, 6),
                    Block.box(6, 2, 10, 10, 3, 12),
                    Block.box(1, 0, 5, 2, 2, 8)),
            Direction.EAST, Shapes.or(
                    Block.box(1, 0, 2, 15, 2, 14),
                    Block.box(10, 2, 4, 12, 3, 6),
                    Block.box(4, 2, 6, 6, 3, 10),
                    Block.box(8, 0, 1, 11, 2, 2))));
    protected static final Map<Direction, VoxelShape> RELAY_WALL_AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.SOUTH, Shapes.or(
                    Block.box(2, 1, 0, 14, 15, 2),
                    Block.box(4, 10, 2, 6, 12, 3),
                    Block.box(6, 4, 2, 10, 6, 3),
                    Block.box(1, 8, 0, 2, 11, 2)),
            Direction.WEST, Shapes.or(
                    Block.box(14, 1, 2, 16, 15, 14),
                    Block.box(13, 10, 4, 14, 12, 6),
                    Block.box(13, 4, 6, 14, 6, 10),
                    Block.box(14, 8, 1, 16, 11, 2)),
            Direction.NORTH, Shapes.or(
                    Block.box(2, 1, 14, 14, 15, 16),
                    Block.box(10, 10, 13, 12, 12, 14),
                    Block.box(6, 4, 13, 10, 6, 14),
                    Block.box(14, 8, 14, 15, 11, 16)),
            Direction.EAST, Shapes.or(
                    Block.box(0, 1, 2, 2, 15, 14),
                    Block.box(2, 10, 10, 3, 12, 12),
                    Block.box(2, 4, 6, 3, 6, 10),
                    Block.box(0, 8, 14, 2, 11, 15))));
    protected static final Map<Direction, VoxelShape> RELAY_CEILING_AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.SOUTH, Shapes.or(
                    Block.box(2, 14, 1, 14, 16, 15),
                    Block.box(10, 13, 4, 12, 14, 6),
                    Block.box(6, 13, 10, 10, 14, 12),
                    Block.box(14, 14, 5, 15, 16, 8)),
            Direction.WEST, Shapes.or(
                    Block.box(1, 14, 2, 15, 16, 14),
                    Block.box(10, 13, 10, 12, 14, 12),
                    Block.box(4, 13, 6, 6, 14, 10),
                    Block.box(8, 14, 14, 11, 16, 15)),
            Direction.NORTH, Shapes.or(
                    Block.box(2, 14, 1, 14, 16, 15),
                    Block.box(4, 13, 10, 6, 14, 12),
                    Block.box(6, 13, 4, 10, 14, 6),
                    Block.box(1, 14, 8, 2, 16, 11)),
            Direction.EAST, Shapes.or(
                    Block.box(1, 14, 2, 15, 16, 14),
                    Block.box(4, 13, 4, 6, 14, 6),
                    Block.box(10, 13, 6, 12, 14, 10),
                    Block.box(5, 14, 1, 8, 16, 2))));

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

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_153475_, BlockPos p_153476_, CollisionContext p_153477_) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> RELAY_FLOOR_AABBS.get(state.getValue(FACING));
            case CEILING -> RELAY_CEILING_AABBS.get(state.getValue(FACING));
            default -> RELAY_WALL_AABBS.get(state.getValue(FACING));
        };
    }
}
