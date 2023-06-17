package com.m_w_k.electriclights.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.m_w_k.electriclights.util.GraphNode;
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
            Block.box(4, 0, 4, 12, 9, 12),
            Block.box(3, 9, 3, 13, 11, 13),
            Block.box(5, 11, 5, 11, 13, 11));
    protected static final VoxelShape LIGHT_CEILING_AABB = Shapes.or(
            Block.box(4, 1, 4, 12, 10, 12),
            Block.box(3, 10, 3, 13, 12, 13),
            Block.box(5, 12, 5, 11, 14, 11));
    protected static final Map<Direction, VoxelShape> LIGHT_WALL_AABBS = Maps.newEnumMap(ImmutableMap.of(
            Direction.SOUTH, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(7.5, 13.5, 0.5, 8.5, 14.5, 10.5),
                    Block.box(6, 12, 0, 10, 15, 1)),
            Direction.WEST, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(5.5, 13.5, 7.5, 15.5, 14.5, 8.5),
                    Block.box(15, 12, 6, 16, 15, 10)),
            Direction.NORTH, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(7.5, 13.5, 5.5, 8.5, 14.5, 15.5),
                    Block.box(6, 12, 15, 10, 15, 16)),
            Direction.EAST, Shapes.or(LIGHT_FLOOR_AABB,
                    Block.box(0.5, 13.5, 7.5, 10.5, 14.5, 8.5),
                    Block.box(0, 12, 6, 1, 15, 10))));

    public BaseLightBlock(Properties properties) {
        super(properties, GraphNode.NodeType.LIGHT);
    }
    public BaseLightBlock(Properties properties, GraphNode.NodeType type) {
        super(properties, type);
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
        return switch (state.getValue(FACE)) {
            case FLOOR -> LIGHT_FLOOR_AABB;
            case CEILING -> LIGHT_CEILING_AABB;
            default -> LIGHT_WALL_AABBS.get(state.getValue(FACING));
        };
    }
}
