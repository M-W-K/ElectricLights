package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.blockentity.GeothermalBlockEntity;
import com.m_w_k.electriclights.registry.ELBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeothermalBlock extends ExtendableGeneratorBlock {
    public GeothermalBlock(Properties properties) {
        super(properties, GeneratorType.GEOTHERMAL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeothermalBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, ELBlockEntityRegistry.GEOTHERMAL_GENERATOR.get(), GeothermalBlockEntity::tick);
    }
}
