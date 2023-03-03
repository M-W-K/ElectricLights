package com.m_w_k.electriclights;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class GraphNode {
    private final BlockPos pos;
    private final boolean isLight;
    private final boolean isSwitchboard;

    public GraphNode(BlockPos pos, boolean isLight, boolean isSwitchboard) {
        this.pos = pos;
        this.isLight = isLight;
        this.isSwitchboard = isSwitchboard;
    }

    public BlockPos getPos() {
        return pos;
    }
    public boolean isLight() {
        return isLight;
    }
    public boolean isSwitchboard() {
        return isSwitchboard;
    }

    @Override
    public @NotNull String toString() {
        if (!isSwitchboard) return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" " + isLight);
        else return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" SWITCHBOARD");
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public boolean equals(Object o) {
        return (o instanceof GraphNode) && (toString().equals(o.toString()));
    }
}
