package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.slf4j.Logger;

public class BiomeParametersDumpReport implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path topPath;
   private final CompletableFuture<HolderLookup.Provider> registries;

   public BiomeParametersDumpReport(PackOutput p_256322_, CompletableFuture<HolderLookup.Provider> p_256222_) {
      this.topPath = p_256322_.getOutputFolder(PackOutput.Target.REPORTS).resolve("biome_parameters");
      this.registries = p_256222_;
   }

   public CompletableFuture<?> run(CachedOutput p_254091_) {
      return this.registries.thenCompose((p_258200_) -> {
         DynamicOps<JsonElement> dynamicops = RegistryOps.create(JsonOps.INSTANCE, p_258200_);
         HolderGetter<Biome> holdergetter = p_258200_.lookupOrThrow(Registries.BIOME);
         return CompletableFuture.allOf(MultiNoiseBiomeSource.Preset.getPresets().map((p_255488_) -> {
            MultiNoiseBiomeSource multinoisebiomesource = p_255488_.getSecond().biomeSource(holdergetter, false);
            return dumpValue(this.createPath(p_255488_.getFirst()), p_254091_, dynamicops, MultiNoiseBiomeSource.CODEC, multinoisebiomesource);
         }).toArray((p_253398_) -> {
            return new CompletableFuture[p_253398_];
         }));
      });
   }

   private static <E> CompletableFuture<?> dumpValue(Path p_254407_, CachedOutput p_254093_, DynamicOps<JsonElement> p_253788_, Encoder<E> p_254276_, E p_254073_) {
      Optional<JsonElement> optional = p_254276_.encodeStart(p_253788_, p_254073_).resultOrPartial((p_236195_) -> {
         LOGGER.error("Couldn't serialize element {}: {}", p_254407_, p_236195_);
      });
      return optional.isPresent() ? DataProvider.saveStable(p_254093_, optional.get(), p_254407_) : CompletableFuture.completedFuture((Object)null);
   }

   private Path createPath(ResourceLocation p_236179_) {
      return this.topPath.resolve(p_236179_.getNamespace()).resolve(p_236179_.getPath() + ".json");
   }

   public final String getName() {
      return "Biome Parameters";
   }
}