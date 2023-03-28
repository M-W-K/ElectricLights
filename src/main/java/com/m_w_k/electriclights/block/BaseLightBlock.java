package com.m_w_k.electriclights.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class BaseLightBlock extends AbstractRelayBlock {
    protected static final VoxelShape LIGHT_FLOOR_AABB = Shapes.or(
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D),
            Block.box(3.0D, 9.0D, 3.0D, 13.0D, 11.0D, 13.0D),
            Block.box(5.0D, 11.0D, 5.0D, 11.0D, 13.0D, 11.0D));
    protected static final VoxelShape LIGHT_CEILING_AABB = Shapes.or(
            Block.box(4.0D, 1.0D, 4.0D, 12.0D, 10.0D, 12.0D),
            Block.box(3.0D, 10.0D, 3.0D, 13.0D, 12.0D, 13.0D),
            Block.box(5.0D, 12.0D, 5.0D, 11.0D, 14.0D, 11.0D));
    protected static final Map<Direction, VoxelShape> LIGHT_WALL_AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(7.5D, 13.5D, 0.5D, 8.5D, 14.5D, 10.5D),
                    Block.box(6.0D, 12.0D, 0.0D, 10.0D, 15.0D, 1.0D)),
            Direction.EAST, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(5.5D, 13.5D, 7.5D, 15.5D, 14.5D, 8.5D),
                    Block.box(15.0D, 12.0D, 6.0D, 16.0D, 15.0D, 10.0D)),
            Direction.SOUTH, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(7.5D, 13.5D, 5.5D, 8.5D, 14.5D, 15.5D),
                    Block.box(6.0D, 12.0D, 15.0D, 10.0D, 15.0D, 16.0D)),
            Direction.WEST, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(0.5D, 13.5D, 7.5D, 10.5D, 14.5D, 8.5D),
                    Block.box(0.0D, 12.0D, 6.0D, 1.0D, 15.0D, 10.0D))));

    public BaseLightBlock(Properties properties) {
        super(properties, true);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        int light = state.getValue(LIGHTSTATE);
        if (light == 1 || light == 0) return light;
        else return light + 12;
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_153475_, BlockPos p_153476_, CollisionContext p_153477_) {
        return switch (state.getValue(FACING)) {
            case DOWN -> LIGHT_FLOOR_AABB;
            case UP -> LIGHT_CEILING_AABB;
            default -> LIGHT_WALL_AABBS.get(state.getValue(FACING));
        };
    }
}
