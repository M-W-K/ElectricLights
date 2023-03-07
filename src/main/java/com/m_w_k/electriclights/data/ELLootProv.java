package com.m_w_k.electriclights.data;

import com.m_w_k.electriclights.block.BurnOutAbleLightBlock;
import com.m_w_k.electriclights.registry.ELBlockRegistry;
import com.m_w_k.electriclights.registry.ELItemsRegistry;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
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
            blocks().forEach(block -> {
                if (block == ELBlockRegistry.ELECTRIC_LIGHT.get()) {
                    add(block, createDropsForBurnableLights());
                } else dropSelf(block);
            });
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

        private LootTable.Builder createDropsForBurnableLights() {
            // ugly as sin, but it somehow works
            // don't ask how much internet delving I did in order to come up with this
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_0_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 0))))
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_1_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 1))))
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_2_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 2))))
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_3_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 3))))
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_4_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 4))))
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_5_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 5))))
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_6_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 6))))
                    .add(LootItem.lootTableItem(ELItemsRegistry.ELECTRIC_LIGHT_7_BLOCK_ITEM.get())
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ELBlockRegistry.ELECTRIC_LIGHT.get()).setProperties(
                                    StatePropertiesPredicate.Builder.properties().hasProperty(BurnOutAbleLightBlock.AGE, 7))))

                    ;
            return LootTable.lootTable().withPool(poolBuilder);
        }

    }
}
