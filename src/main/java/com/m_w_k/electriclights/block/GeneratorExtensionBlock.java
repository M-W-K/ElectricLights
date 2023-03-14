package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.block.ExtendableGeneratorBlock.GeneratorType;
import com.m_w_k.electriclights.blockentity.ExtendableGeneratorBlockEntity;
import com.m_w_k.electriclights.util.ELGraphHandler;
import com.m_w_k.electriclights.util.GraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class GeneratorExtensionBlock extends Block {
    private final GeneratorType generatorType;

    public GeneratorExtensionBlock(Properties p_49795_, GeneratorType generatorType) {
        super(p_49795_);
        this.generatorType = generatorType;
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
}
