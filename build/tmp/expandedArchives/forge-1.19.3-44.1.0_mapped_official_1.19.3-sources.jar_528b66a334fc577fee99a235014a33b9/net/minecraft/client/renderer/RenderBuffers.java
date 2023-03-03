package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBuffers {
   private final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
   private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (p_247930_) -> {
      p_247930_.put(Sheets.solidBlockSheet(), this.fixedBufferPack.builder(RenderType.solid()));
      p_247930_.put(Sheets.cutoutBlockSheet(), this.fixedBufferPack.builder(RenderType.cutout()));
      p_247930_.put(Sheets.bannerSheet(), this.fixedBufferPack.builder(RenderType.cutoutMipped()));
      p_247930_.put(Sheets.translucentCullBlockSheet(), this.fixedBufferPack.builder(RenderType.translucent()));
      put(p_247930_, Sheets.shieldSheet());
      put(p_247930_, Sheets.bedSheet());
      put(p_247930_, Sheets.shulkerBoxSheet());
      put(p_247930_, Sheets.signSheet());
      put(p_247930_, Sheets.hangingSignSheet());
      put(p_247930_, Sheets.chestSheet());
      put(p_247930_, RenderType.translucentNoCrumbling());
      put(p_247930_, RenderType.armorGlint());
      put(p_247930_, RenderType.armorEntityGlint());
      put(p_247930_, RenderType.glint());
      put(p_247930_, RenderType.glintDirect());
      put(p_247930_, RenderType.glintTranslucent());
      put(p_247930_, RenderType.entityGlint());
      put(p_247930_, RenderType.entityGlintDirect());
      put(p_247930_, RenderType.waterMask());
      ModelBakery.DESTROY_TYPES.forEach((p_173062_) -> {
         put(p_247930_, p_173062_);
      });
   });
   private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
   private final MultiBufferSource.BufferSource crumblingBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
   private final OutlineBufferSource outlineBufferSource = new OutlineBufferSource(this.bufferSource);

   private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> p_110102_, RenderType p_110103_) {
      p_110102_.put(p_110103_, new BufferBuilder(p_110103_.bufferSize()));
   }

   public ChunkBufferBuilderPack fixedBufferPack() {
      return this.fixedBufferPack;
   }

   public MultiBufferSource.BufferSource bufferSource() {
      return this.bufferSource;
   }

   public MultiBufferSource.BufferSource crumblingBufferSource() {
      return this.crumblingBufferSource;
   }

   public OutlineBufferSource outlineBufferSource() {
      return this.outlineBufferSource;
   }
}