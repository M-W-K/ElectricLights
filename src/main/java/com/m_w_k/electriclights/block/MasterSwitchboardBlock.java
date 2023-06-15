package com.m_w_k.electriclights.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.m_w_k.electriclights.network.ELPacketHandler;
import com.m_w_k.electriclights.network.SwitchboardHumPacket;
import com.m_w_k.electriclights.util.ELBreaker;
import com.m_w_k.electriclights.util.ELBlockStateProperties;
import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.util.GraphNode;
import com.m_w_k.electriclights.blockentity.MasterSwitchboardBlockEntity;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MasterSwitchboardBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, ELBreaker {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Integer> LIGHTSTATE = ELBlockStateProperties.LIGHTSTATE;
    public static final BooleanProperty DISABLED = ELBlockStateProperties.DISABLED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected static final Map<Direction, VoxelShape> SWITCHBOARD_AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.SOUTH, Shapes.or(
                    Block.box(0, 0, 0, 16, 3, 16),
                    Block.box(0, 3, 4, 16, 16, 16)),
            Direction.WEST, Shapes.or(
                    Block.box(0, 0, 0, 16, 3, 16),
                    Block.box(0, 3, 0, 12, 16, 16)),
            Direction.NORTH, Shapes.or(
                    Block.box(0, 0, 0, 16, 3, 16),
                    Block.box(0, 3, 0, 16, 16, 12)),
            Direction.EAST, Shapes.or(
                    Block.box(0, 0, 0, 16, 3, 16),
                    Block.box(4, 3, 0, 16, 16, 16))));

    public MasterSwitchboardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(WATERLOGGED, false)
                        .setValue(LIGHTSTATE, 0)
                        .setValue(DISABLED, false)
                        .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(FACING, WATERLOGGED, LIGHTSTATE, DISABLED, POWERED);
        super.createBlockStateDefinition(blockStateBuilder);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            BlockPos clickedPos = context.getClickedPos();

            state = state.setValue(FACING, context.getHorizontalDirection());
            state = state.setValue(WATERLOGGED, context.getLevel().getFluidState(clickedPos).getType() == Fluids.WATER);
            if (context.getLevel().hasNeighborSignal(clickedPos)) {
                return state.setValue(DISABLED, true).setValue(POWERED, true);
            }
            return state.setValue(DISABLED, false).setValue(POWERED, false);
        } else return null;
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
                ELPacketHandler.sendToNearClients(new SwitchboardHumPacket(pos, false), pos, 16, level);
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
            boolean flag = level.hasNeighborSignal(pos);
            if (flag != state.getValue(POWERED)) {
                state = state.setValue(POWERED, flag);
                if (state.getValue(DISABLED) != flag) state = setDisabled(state, level, pos, flag);
                else level.setBlock(pos, state, 2);

                if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
        }
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_54644_, BlockHitResult p_54645_) {
        return used(state, level, pos, player, p_54644_, p_54645_);
    }

    public BlockState setDisabled(BlockState state, Level level, BlockPos pos, boolean disabled) {
        if (level.getBlockEntity(pos) instanceof MasterSwitchboardBlockEntity switchboard) {
            switchboard.forceUpdate();
            switchboard.setTicksToSoundUpdate(8);
        }
        return ELBreaker.super.setDisabled(state, level, pos, disabled);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        // TODO use a BER instead of a basic model
        return RenderShape.MODEL;
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_153475_, BlockPos p_153476_, CollisionContext p_153477_) {
        return SWITCHBOARD_AABBS.get(state.getValue(FACING));
    }
}
