package com.m_w_k.electriclights;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class GraphNode {
    private final BlockPos pos;
    private final boolean isLight;
    private final String specialType; // can be SWITCHBOARD, GENERATOR

    public GraphNode(BlockPos pos, boolean isLight, String specialType) {
        this.pos = pos;
        this.isLight = isLight;
        this.specialType = specialType;
    }

    public BlockPos getPos() {
        return pos;
    }
    public boolean isLight() {
        return isLight;
    }
    public String getSpecialType() {
        return specialType;
    }

    @Override
    public @NotNull String toString() {
        if (specialType == null) return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" " + isLight);
        else return String.valueOf(pos.getX()).concat(" " + pos.getY()).concat(" " + pos.getZ()).concat(" " + specialType);
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
