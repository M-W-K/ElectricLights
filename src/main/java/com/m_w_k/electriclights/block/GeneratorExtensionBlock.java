package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.block.ExtendableGeneratorBlock.GeneratorType;
import com.m_w_k.electriclights.blockentity.ExtendableGeneratorBlockEntity;
import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.util.GraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GeneratorExtensionBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    private final GeneratorType generatorType;

    public GeneratorExtensionBlock(Properties p_49795_, GeneratorType generatorType) {
        super(p_49795_);
        this.generatorType = generatorType;
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(WATERLOGGED, false)
        );
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(WATERLOGGED);
    }

    @Override
    public BlockState updateShape(BlockState state1, Direction direction, BlockState state2, LevelAccessor levelAccessor, BlockPos pos1, BlockPos pos2) {
        if (state1.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(pos1, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(state1, direction, state2, levelAccessor, pos1, pos2);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                List<GraphNode> generators = ELGraphHandler.getGenerators(level);
                for (GraphNode generator : generators) {
                    BlockPos genPos = generator.getPos();
                    if (ELGraphHandler.areConnected(genPos, pos) && level.getBlockState(genPos).getBlock() instanceof ExtendableGeneratorBlock extendableGeneratorBlock
                            && extendableGeneratorBlock.getType() == generatorType && level.getBlockEntity(genPos) instanceof ExtendableGeneratorBlockEntity extendableGeneratorBlockEntity) {
                        extendableGeneratorBlockEntity.noteExtension(true, pos);
                        break; // Make sure we only register with one matching generator
                    }
                }
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
                List<GraphNode> generators = ELGraphHandler.getGenerators(level);
                for (GraphNode generator : generators) {
                    BlockPos genPos = generator.getPos();
                    if (ELGraphHandler.areConnected(genPos, pos) && level.getBlockState(genPos).getBlock() instanceof ExtendableGeneratorBlock extendableGeneratorBlock
                            && extendableGeneratorBlock.getType() == generatorType && level.getBlockEntity(genPos) instanceof ExtendableGeneratorBlockEntity extendableGeneratorBlockEntity)
                        extendableGeneratorBlockEntity.noteExtension(false, pos);
                        // make sure we unregister from all matching generators
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public PushReaction getPistonPushReaction(BlockState p_153494_) {
        return PushReaction.IGNORE;
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState p_153469_, BlockGetter p_153470_, BlockPos p_153471_, PathComputationType p_153472_) {
        return p_153472_ == PathComputationType.WATER && p_153470_.getFluidState(p_153471_).is(FluidTags.WATER);
    }
}
