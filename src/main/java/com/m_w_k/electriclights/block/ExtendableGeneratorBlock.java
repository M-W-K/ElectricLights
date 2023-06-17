package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.blockentity.ExtendableGeneratorBlockEntity;
import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.util.GraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExtendableGeneratorBlock extends BaseEntityBlock {
    private final GeneratorType generatorType;

    public ExtendableGeneratorBlock(BlockBehaviour.Properties properties, GeneratorType generatorType) {
        super(properties);
        this.generatorType = generatorType;
    }
    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type);
    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                ELGraphHandler.addGraphNodeAndAutoConnect(new GraphNode(pos, GraphNode.NodeType.GENERATOR), level);
            }
            super.onPlace(state, level, pos, newState, isMoving);
        }
    }
    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ExtendableGeneratorBlockEntity extendableGeneratorBlockEntity) player.openMenu(extendableGeneratorBlockEntity);
            return InteractionResult.CONSUME;
        } else return InteractionResult.SUCCESS;
    }
    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                ELGraphHandler.removeGraphNode(new GraphNode(pos, GraphNode.NodeType.GENERATOR), level);
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
     * Used so that generators and their extensions can recognize each other.
     */
    public enum GeneratorType {
        SOLAR, GEOTHERMAL
    }
    GeneratorType getType() {
        return generatorType;
    }
}
