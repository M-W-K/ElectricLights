package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.util.GraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricRelayBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Direction> FACING = BlockStateProperties.FACING;
    public static final Property<Integer> LIGHTSTATE = IntegerProperty.create("light_state", 0, 4);
    protected static final VoxelShape LIGHT_FLOOR_AABB = Shapes.or(
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D),
            Block.box(3.0D, 9.0D, 3.0D, 13.0D, 11.0D, 13.0D),
            Block.box(5.0D, 11.0D, 5.0D, 11.0D, 13.0D, 11.0D));

    private final boolean isLight;

    /**
     * Direct override in order to force the game to set this block's light emission to what is determined by our LIGHTSTATE.
     */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
    {
        // TODO implement a blown-bulb mechanism based on state changes?
        // use 10% chance and an age blockstate, have 100% chance if waterlogged - remove the cannot glow while waterlogged logic
        if (isLight) {
            int light = state.getValue(LIGHTSTATE);
            if (light == 1 || light == 0) return light;
            else return light * 2 + 7;
        } else return super.getLightEmission(state, level, pos);
    }

    public ElectricRelayBlock(Properties properties, boolean isLight) {
        super(properties);
        this.isLight = isLight;
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(WATERLOGGED, false)
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LIGHTSTATE, 0)
        );
    }
    public ElectricRelayBlock(Properties properties) {
        this(properties, false);
    }
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = this.defaultBlockState();
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

    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (!level.isClientSide()) {
            ELGraphHandler.addGraphNodeAndAutoConnect(new GraphNode(pos, isLight ? GraphNode.NodeType.LIGHT : GraphNode.NodeType.RELAY), level);
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
                ELGraphHandler.removeGraphNode(new GraphNode(pos, isLight ? GraphNode.NodeType.LIGHT : GraphNode.NodeType.RELAY), level);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState p_153474_, BlockGetter p_153475_, BlockPos p_153476_, CollisionContext p_153477_) {
        return LIGHT_FLOOR_AABB;
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
