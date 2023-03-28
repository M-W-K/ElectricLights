package com.m_w_k.electriclights.util;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class ELBlockStateProperties {
    public static final Property<Integer> LIGHTSTATE = IntegerProperty.create("light_state", 0, 4);
    public static final BooleanProperty DISABLED = BooleanProperty.create("disabled");
}
