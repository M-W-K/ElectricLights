package com.m_w_k.electriclights.util;

import com.m_w_k.electriclights.ElectricLightsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ELTags {
    public static class Items {
        public static final TagKey<Item> AGEABLE_LIGHT = tag("ageable_light");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(ElectricLightsMod.MODID, name));
        }
    }
}
