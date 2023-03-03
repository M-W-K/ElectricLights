package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.GraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class ElectricRelayBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Direction> FACING = BlockStateProperties.FACING;
    public static final Property<Integer> LIGHTSTATE = IntegerProperty.create("light_state", 0, 4);

    private final boolean isLight;

    /**
     * Direct override in order to force the game to set this block's light emission to what is determined by our LIGHTSTATE.
     */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
    {
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
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        // TODO add detection for whether the face we're trying to attach to can support us
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Direction direction = context.getClickedFace();
            BlockPos blockpos = context.getClickedPos();
            FluidState fluidstate = context.getLevel().getFluidState(blockpos);
            state.setValue(FACING, direction).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
        }
        return state;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(WATERLOGGED, FACING, LIGHTSTATE);
    }


    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (!level.isClientSide()) {
            ElectricLightsMod.addGraphNodeAndAutoConnect(new GraphNode(pos, isLight, false), level);
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
                ElectricLightsMod.removeGraphNode(new GraphNode(pos, isLight, false), level);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
