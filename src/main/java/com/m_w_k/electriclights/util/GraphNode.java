package com.m_w_k.electriclights.util;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class GraphNode {
    private final BlockPos pos;
    private final NodeType type;

    public GraphNode(BlockPos pos, NodeType type) {
        this.pos = pos;
        this.type = type;
    }

    public BlockPos getPos() {
        return pos;
    }
    public boolean isLight() {
        return type.isLight();
    }
    public NodeType getType() {
        return type;
    }

    @Override
    public @NotNull String toString() {
        return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" " + type);
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public boolean equals(Object o) {
        return (o instanceof GraphNode) && (toString().equals(o.toString()));
    }

    public enum NodeType {
        RELAY, LIGHT, SWITCHBOARD, GENERATOR;
        public boolean isSpecial() {
            return (this == SWITCHBOARD) || (this == GENERATOR);
        }
        public boolean isLight() {
            return this == LIGHT;
        }
        public boolean isSwitchboard() {
            return this == SWITCHBOARD;
        }
        public boolean isGenerator() {
            return this == GENERATOR;
        }
    }
}
