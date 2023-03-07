package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.item.BurnOutAbleLightBlockItem;
import com.m_w_k.electriclights.registry.ELItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BurnOutAbleLightBlock extends ElectricRelayBlock {
    public static final Property<Integer> AGE = BlockStateProperties.AGE_7;
    public BurnOutAbleLightBlock(Properties properties) {
        super(properties, true);
    }
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(AGE) == 7 ? 0 : super.getLightEmission(state, level, pos);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(AGE);
        super.createBlockStateDefinition(blockStateBuilder);
    }
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getItemInHand().getItem() instanceof BurnOutAbleLightBlockItem item) {
            return this.defaultBlockState().setValue(AGE,item.getInitialState());
        }
        return null;
    }
    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isHolding(ELItemsRegistry.REDSTONE_BULB.get()) && state.getBlock() instanceof BurnOutAbleLightBlock && state.getValue(AGE) != 0) {
            if (!level.isClientSide()) {
                ItemStack bulbs = player.getItemInHand(hand);
                bulbs.setCount(bulbs.getCount() - 1);
                state.setValue(AGE, 0);
                return InteractionResult.CONSUME;
            } else return InteractionResult.SUCCESS;
        } else return InteractionResult.PASS;
    }

}
