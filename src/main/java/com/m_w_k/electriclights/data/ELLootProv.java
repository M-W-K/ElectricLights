package com.m_w_k.electriclights.data;

import com.m_w_k.electriclights.registry.ELBlockRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ELLootProv extends LootTableProvider {
    public ELLootProv(DataGenerator gen) {
        super(gen.getPackOutput(), Set.of(), List.of(new LootTableProvider.SubProviderEntry(BlockGen::new, LootContextParamSets.BLOCK)));
    }
    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        map.forEach((location, table) -> LootTables.validate(validationtracker, location, table));
    }

    private static class BlockGen extends VanillaBlockLoot {
        @Override
        protected void generate()
        {
            blocks().forEach(this::dropSelf);
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return blocks()::iterator;
        }

        private Stream<Block> blocks()
        {
            return ELBlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get);
        }

    }
}
