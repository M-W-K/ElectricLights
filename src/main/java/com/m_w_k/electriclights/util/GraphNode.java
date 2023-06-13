package com.m_w_k.electriclights.util;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class GraphNode {
    private final BlockPos pos;
    private final NodeType type;
    private int misc;

    public GraphNode(BlockPos pos, NodeType type) {
        this.pos = pos;
        this.type = type;
        this.misc = 0;
    }

    public GraphNode(BlockPos pos, NodeType type, int misc) {
        this.pos = pos;
        this.type = type;
        this.misc = misc;
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
    public int getMisc() {
        return misc;
    }
    public void setMisc(int misc) {
        this.misc = misc;
    }

    @Override
    public @NotNull String toString() {
        return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" " + type);
    }

    public @NotNull String toStringMisc() {
        // non-zero misc must be stored on graph save, but should not affect identity operations that use .toString()
        if (misc == 0) return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" " + type);
        return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" " + type).concat(" " + misc);
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
            return this == SWITCHBOARD || this == GENERATOR;
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
