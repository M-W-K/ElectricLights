package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
   private final Function<TagKey<Block>, TagBuilder> blockTags;

   /**
    * @deprecated Forge: Use the {@link #ItemTagsProvider(PackOutput, CompletableFuture, TagsProvider, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
    */
   @Deprecated
   public ItemTagsProvider(PackOutput p_255871_, CompletableFuture<HolderLookup.Provider> p_256035_, TagsProvider<Block> p_256467_) {
      this(p_255871_, p_256035_, p_256467_, "vanilla", null);
   }
   public ItemTagsProvider(PackOutput p_255871_, CompletableFuture<HolderLookup.Provider> p_256035_, TagsProvider<Block> p_256467_, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(p_255871_, Registries.ITEM, p_256035_, (p_255790_) -> {
         return p_255790_.builtInRegistryHolder().key();
      }, modId, existingFileHelper);
      this.blockTags = p_256467_::getOrCreateRawBuilder;
   }

   protected void copy(TagKey<Block> p_206422_, TagKey<Item> p_206423_) {
      TagBuilder tagbuilder = this.getOrCreateRawBuilder(p_206423_);
      TagBuilder tagbuilder1 = this.blockTags.apply(p_206422_);
      tagbuilder1.build().forEach(tagbuilder::add);
   }
}
