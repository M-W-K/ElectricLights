package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final PackOutput.PathProvider pathProvider;
   protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
   protected final ResourceKey<? extends Registry<T>> registryKey;
   protected final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();
   protected final String modId;
   @org.jetbrains.annotations.Nullable
   protected final net.minecraftforge.common.data.ExistingFileHelper existingFileHelper;
   private final net.minecraftforge.common.data.ExistingFileHelper.IResourceType resourceType;
   private final net.minecraftforge.common.data.ExistingFileHelper.IResourceType elementResourceType; // FORGE: Resource type for validating required references to datapack registry elements.

   /**
    * @deprecated Forge: Use the {@linkplain #TagsProvider(PackOutput, ResourceKey, CompletableFuture, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
    */
   @Deprecated
   protected TagsProvider(PackOutput p_256596_, ResourceKey<? extends Registry<T>> p_255886_, CompletableFuture<HolderLookup.Provider> p_256513_) {
      this(p_256596_, p_255886_, p_256513_, "vanilla", null);
   }
   protected TagsProvider(PackOutput p_256596_, ResourceKey<? extends Registry<T>> p_255886_, CompletableFuture<HolderLookup.Provider> p_256513_, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      this.pathProvider = p_256596_.createPathProvider(PackOutput.Target.DATA_PACK, TagManager.getTagDir(p_255886_));
      this.lookupProvider = p_256513_;
      this.registryKey = p_255886_;
      this.modId = modId;
      this.existingFileHelper = existingFileHelper;
      this.resourceType = new net.minecraftforge.common.data.ExistingFileHelper.ResourceType(net.minecraft.server.packs.PackType.SERVER_DATA, ".json", TagManager.getTagDir(p_255886_));
      this.elementResourceType = new net.minecraftforge.common.data.ExistingFileHelper.ResourceType(net.minecraft.server.packs.PackType.SERVER_DATA, ".json", net.minecraftforge.common.ForgeHooks.prefixNamespace(p_255886_.location()));
   }

   // Forge: Allow customizing the path for a given tag or returning null
   @org.jetbrains.annotations.Nullable
   protected Path getPath(ResourceLocation id) {
      return this.pathProvider.json(id);
   }

   public String getName() {
      return "Tags for " + this.registryKey.location() + " mod id " + this.modId;
   }

   protected abstract void addTags(HolderLookup.Provider p_256380_);

   public CompletableFuture<?> run(CachedOutput p_253684_) {
      return this.lookupProvider.thenCompose((p_255494_) -> {
         this.builders.clear();
         this.addTags(p_255494_);
         HolderLookup.RegistryLookup<T> registrylookup = p_255494_.lookup(this.registryKey).orElseThrow(() -> {
            // FORGE: Throw a more descriptive error message if this is a Forge registry without tags enabled
            if (net.minecraftforge.registries.RegistryManager.ACTIVE.getRegistry(this.registryKey) != null) {
               return new IllegalStateException("Forge registry " + this.registryKey.location() + " does not have support for tags");
            }
            return new IllegalStateException("Registry " + this.registryKey.location() + " not found");
         });
         Predicate<ResourceLocation> predicate = (p_255496_) -> {
            return registrylookup.get(ResourceKey.create(this.registryKey, p_255496_)).isPresent();
         };
         return CompletableFuture.allOf(this.builders.entrySet().stream().map((p_255499_) -> {
            ResourceLocation resourcelocation = p_255499_.getKey();
            TagBuilder tagbuilder = p_255499_.getValue();
            List<TagEntry> list = tagbuilder.build();
            List<TagEntry> list1 = list.stream().filter((p_255492_) -> {
               return !p_255492_.verifyIfPresent(predicate, this.builders::containsKey);
            }).filter(this::missing).toList(); // Forge: Add validation via existing resources
            if (!list1.isEmpty()) {
               throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", resourcelocation, list1.stream().map(Objects::toString).collect(Collectors.joining(","))));
            } else {
               JsonElement jsonelement = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(list, false)).getOrThrow(false, LOGGER::error);
               Path path = this.getPath(resourcelocation);
               if (path == null) return CompletableFuture.completedFuture(null); // Forge: Allow running this data provider without writing it. Recipe provider needs valid tags.
               return DataProvider.saveStable(p_253684_, jsonelement, path);
            }
         }).toArray((p_253442_) -> {
            return new CompletableFuture[p_253442_];
         }));
      });
   }

   private boolean missing(TagEntry reference) {
      // Optional tags should not be validated

      if (reference.isRequired()) {
         return existingFileHelper == null || !existingFileHelper.exists(reference.getId(), reference.isTag() ? resourceType : elementResourceType);
      }
      return false;
   }

   protected TagsProvider.TagAppender<T> tag(TagKey<T> p_206425_) {
      TagBuilder tagbuilder = this.getOrCreateRawBuilder(p_206425_);
      return new TagsProvider.TagAppender<>(tagbuilder, modId);
   }

   protected TagBuilder getOrCreateRawBuilder(TagKey<T> p_236452_) {
      return this.builders.computeIfAbsent(p_236452_.location(), (p_236442_) -> {
         existingFileHelper.trackGenerated(p_236442_, resourceType);
         return TagBuilder.create();
      });
   }

   public static class TagAppender<T> implements net.minecraftforge.common.extensions.IForgeTagAppender<T> {
      private final TagBuilder builder;
      private final String modId;

      protected TagAppender(TagBuilder p_236454_, String modId) {
         this.builder = p_236454_;
         this.modId = modId;
      }

      public final TagsProvider.TagAppender<T> add(ResourceKey<T> p_256138_) {
         this.builder.addElement(p_256138_.location());
         return this;
      }

      @SafeVarargs
      public final TagsProvider.TagAppender<T> add(ResourceKey<T>... p_211102_) {
         for(ResourceKey<T> resourcekey : p_211102_) {
            this.builder.addElement(resourcekey.location());
         }

         return this;
      }

      public TagsProvider.TagAppender<T> addOptional(ResourceLocation p_176840_) {
         this.builder.addOptionalElement(p_176840_);
         return this;
      }

      public TagsProvider.TagAppender<T> addTag(TagKey<T> p_206429_) {
         this.builder.addTag(p_206429_.location());
         return this;
      }

      public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation p_176842_) {
         this.builder.addOptionalTag(p_176842_);
         return this;
      }

      public TagsProvider.TagAppender<T> add(TagEntry tag) {
          builder.add(tag);
          return this;
      }

      public TagBuilder getInternalBuilder() {
          return builder;
      }

      public String getModID() {
          return modId;
      }
   }
}
