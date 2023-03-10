package com.m_w_k.electriclights.item;

import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;

public class BurnOutAbleLightBlockItem extends ItemNameBlockItem {
    private final int initialState;
    public BurnOutAbleLightBlockItem(Block block, Properties properties, int initialState) {
        super(block, properties);
        this.initialState = initialState;
    }

    public int getInitialState() {
        return initialState;
    }
}
