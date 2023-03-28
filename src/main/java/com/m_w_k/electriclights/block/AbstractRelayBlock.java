package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.util.ELBlockStateProperties;
import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.util.GraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRelayBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Direction> FACING = BlockStateProperties.FACING;
    public static final Property<Integer> LIGHTSTATE = ELBlockStateProperties.LIGHTSTATE;

    private final boolean isLight;

    public AbstractRelayBlock(Properties properties, boolean isLight) {
        super(properties);
        this.isLight = isLight;
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(WATERLOGGED, false)
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LIGHTSTATE, 0)
        );
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return getStateLogic(context, this.defaultBlockState());
    }

    @Nullable
    protected BlockState getStateLogic(BlockPlaceContext context, BlockState state) {
        BlockPos blockpos = context.getClickedPos();

        FluidState fluidstate = context.getLevel().getFluidState(blockpos);
        state = state.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);

        for (Direction candidate : context.getNearestLookingDirections()) {
            state = state.setValue(FACING, candidate);
            if (state.canSurvive(context.getLevel(), blockpos)) {
                return state;
            }
        }
        return null;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(WATERLOGGED, FACING, LIGHTSTATE);
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state1, Direction direction, BlockState state2, LevelAccessor levelAccessor, BlockPos pos1, BlockPos pos2) {
        if (state1.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(pos1, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return direction == state1.getValue(FACING) && !state1.canSurvive(levelAccessor, pos1) ? Blocks.AIR.defaultBlockState() : state1;
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        return canSupportCenter(levelReader, pos.relative(state.getValue(FACING)), state.getValue(FACING).getOpposite());
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                handleSelfGraphNode(level, pos, true);
            }
            super.onPlace(state, level, pos, newState, isMoving);
        }
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                handleSelfGraphNode(level, pos, false);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    protected void handleSelfGraphNode(Level level, BlockPos pos, boolean addNode) {
        if (addNode) {
            ELGraphHandler.addGraphNodeAndAutoConnect(new GraphNode(pos, isLight ? GraphNode.NodeType.LIGHT : GraphNode.NodeType.RELAY), level);

        } else {
            ELGraphHandler.removeGraphNode(new GraphNode(pos, isLight ? GraphNode.NodeType.LIGHT : GraphNode.NodeType.RELAY), level);

        }
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public PushReaction getPistonPushReaction(BlockState p_153494_) {
        return PushReaction.DESTROY;
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState p_153469_, BlockGetter p_153470_, BlockPos p_153471_, PathComputationType p_153472_) {
        return false;
    }
}
