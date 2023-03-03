package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.client.renderer.texture.atlas.sources.Unstitcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteSources {
   private static final BiMap<ResourceLocation, SpriteSourceType> TYPES = HashBiMap.create();
   public static final SpriteSourceType SINGLE_FILE = register("single", SingleFile.CODEC);
   public static final SpriteSourceType DIRECTORY = register("directory", DirectoryLister.CODEC);
   public static final SpriteSourceType FILTER = register("filter", SourceFilter.CODEC);
   public static final SpriteSourceType UNSTITCHER = register("unstitch", Unstitcher.CODEC);
   public static Codec<SpriteSourceType> TYPE_CODEC = ResourceLocation.CODEC.flatXmap((p_261510_) -> {
      SpriteSourceType spritesourcetype = TYPES.get(p_261510_);
      return spritesourcetype != null ? DataResult.success(spritesourcetype) : DataResult.error("Unknown type " + p_261510_);
   }, (p_261880_) -> {
      ResourceLocation resourcelocation = TYPES.inverse().get(p_261880_);
      return p_261880_ != null ? DataResult.success(resourcelocation) : DataResult.error("Unknown type " + resourcelocation);
   });
   public static Codec<SpriteSource> CODEC = TYPE_CODEC.dispatch(SpriteSource::type, SpriteSourceType::codec);
   public static Codec<List<SpriteSource>> FILE_CODEC = RecordCodecBuilder.create((p_262122_) -> {
      return p_262122_.group(CODEC.listOf().fieldOf("sources").forGetter((p_261792_) -> {
         return p_261792_;
      })).apply(p_262122_, (p_261581_) -> {
         return p_261581_;
      });
   });

   private static SpriteSourceType register(String p_262175_, Codec<? extends SpriteSource> p_261464_) {
      SpriteSourceType spritesourcetype = new SpriteSourceType(p_261464_);
      ResourceLocation resourcelocation = new ResourceLocation(p_262175_);
      SpriteSourceType spritesourcetype1 = TYPES.putIfAbsent(resourcelocation, spritesourcetype);
      if (spritesourcetype1 != null) {
         throw new IllegalStateException("Duplicate registration " + resourcelocation);
      } else {
         return spritesourcetype;
      }
   }
}