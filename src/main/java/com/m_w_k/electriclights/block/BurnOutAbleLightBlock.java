package com.m_w_k.electriclights.block;

import com.m_w_k.electriclights.item.BurnOutAbleLightBlockItem;
import com.m_w_k.electriclights.registry.ELBlockRegistry;
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
        BlockState state = getStateLogic(context, this.defaultBlockState());
        if (state != null && context.getItemInHand().getItem() instanceof BurnOutAbleLightBlockItem item) {
            state = state.setValue(AGE,item.getInitialState());
        }
        return state;
    }
    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getBlock() instanceof BurnOutAbleLightBlock && ((player.isHolding(ELItemsRegistry.REDSTONE_BULB.get()) && state.getValue(AGE) != 0) || player.isHolding(ELItemsRegistry.DRAGON_BULB.get()))) {
            if (!level.isClientSide()) {
                ItemStack bulbs = player.getItemInHand(hand);
                if (player.isHolding(ELItemsRegistry.DRAGON_BULB.get())) {
                    level.setBlockAndUpdate(pos, ELBlockRegistry.DRAGON_LIGHT.get().defaultBlockState()
                            .setValue(ElectricRelayBlock.LIGHTSTATE, state.getValue(ElectricRelayBlock.LIGHTSTATE))
                            .setValue(ElectricRelayBlock.WATERLOGGED, state.getValue(ElectricRelayBlock.WATERLOGGED))
                            .setValue(ElectricRelayBlock.FACING, state.getValue(ElectricRelayBlock.FACING)));
                }
                else level.setBlockAndUpdate(pos, state.setValue(AGE, 0));
                bulbs.setCount(bulbs.getCount() - 1);
                return InteractionResult.CONSUME;
            } else return InteractionResult.SUCCESS;
        } else return InteractionResult.PASS;
    }

}
