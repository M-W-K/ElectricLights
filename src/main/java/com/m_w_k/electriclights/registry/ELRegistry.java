package com.m_w_k.electriclights.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public class ELRegistry {
    public static void registerThings(IEventBus modEventBus) {
        ELBlockRegistry.BLOCKS.register(modEventBus);
        ELItemsRegistry.ITEMS.register(modEventBus);
        ELItemsRegistry.BLOCK_ITEMS.register(modEventBus);
        ELBlockEntityRegistry.BLOCK_ENTITIES.register(modEventBus);
        ELGUIRegistry.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(ELItemsRegistry::addCreative);
    }
}
