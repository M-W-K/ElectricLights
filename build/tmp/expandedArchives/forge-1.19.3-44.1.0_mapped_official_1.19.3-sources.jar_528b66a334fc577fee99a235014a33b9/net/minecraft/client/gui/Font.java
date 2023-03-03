package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class Font {
   private static final float EFFECT_DEPTH = 0.01F;
   private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
   public static final int ALPHA_CUTOFF = 8;
   public final int lineHeight = 9;
   public final RandomSource random = RandomSource.create();
   private final Function<ResourceLocation, FontSet> fonts;
   final boolean filterFishyGlyphs;
   private final StringSplitter splitter;

   public Font(Function<ResourceLocation, FontSet> p_243253_, boolean p_243245_) {
      this.fonts = p_243253_;
      this.filterFishyGlyphs = p_243245_;
      this.splitter = new StringSplitter((p_92722_, p_92723_) -> {
         return this.getFontSet(p_92723_.getFont()).getGlyphInfo(p_92722_, this.filterFishyGlyphs).getAdvance(p_92723_.isBold());
      });
   }

   FontSet getFontSet(ResourceLocation p_92864_) {
      return this.fonts.apply(p_92864_);
   }

   public int drawShadow(PoseStack p_92751_, String p_92752_, float p_92753_, float p_92754_, int p_92755_) {
      return this.drawInternal(p_92752_, p_92753_, p_92754_, p_92755_, p_92751_.last().pose(), true, this.isBidirectional());
   }

   public int drawShadow(PoseStack p_92757_, String p_92758_, float p_92759_, float p_92760_, int p_92761_, boolean p_92762_) {
      return this.drawInternal(p_92758_, p_92759_, p_92760_, p_92761_, p_92757_.last().pose(), true, p_92762_);
   }

   public int draw(PoseStack p_92884_, String p_92885_, float p_92886_, float p_92887_, int p_92888_) {
      return this.drawInternal(p_92885_, p_92886_, p_92887_, p_92888_, p_92884_.last().pose(), false, this.isBidirectional());
   }

   public int drawShadow(PoseStack p_92745_, FormattedCharSequence p_92746_, float p_92747_, float p_92748_, int p_92749_) {
      return this.drawInternal(p_92746_, p_92747_, p_92748_, p_92749_, p_92745_.last().pose(), true);
   }

   public int drawShadow(PoseStack p_92764_, Component p_92765_, float p_92766_, float p_92767_, int p_92768_) {
      return this.drawInternal(p_92765_.getVisualOrderText(), p_92766_, p_92767_, p_92768_, p_92764_.last().pose(), true);
   }

   public int draw(PoseStack p_92878_, FormattedCharSequence p_92879_, float p_92880_, float p_92881_, int p_92882_) {
      return this.drawInternal(p_92879_, p_92880_, p_92881_, p_92882_, p_92878_.last().pose(), false);
   }

   public int draw(PoseStack p_92890_, Component p_92891_, float p_92892_, float p_92893_, int p_92894_) {
      return this.drawInternal(p_92891_.getVisualOrderText(), p_92892_, p_92893_, p_92894_, p_92890_.last().pose(), false);
   }

   public String bidirectionalShaping(String p_92802_) {
      try {
         Bidi bidi = new Bidi((new ArabicShaping(8)).shape(p_92802_), 127);
         bidi.setReorderingMode(0);
         return bidi.writeReordered(2);
      } catch (ArabicShapingException arabicshapingexception) {
         return p_92802_;
      }
   }

   private int drawInternal(String p_254064_, float p_254270_, float p_254152_, int p_254242_, Matrix4f p_254345_, boolean p_254516_, boolean p_254277_) {
      if (p_254064_ == null) {
         return 0;
      } else {
         MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
         int i = this.drawInBatch(p_254064_, p_254270_, p_254152_, p_254242_, p_254516_, p_254345_, multibuffersource$buffersource, false, 0, 15728880, p_254277_);
         multibuffersource$buffersource.endBatch();
         return i;
      }
   }

   private int drawInternal(FormattedCharSequence p_253879_, float p_254094_, float p_254476_, int p_254424_, Matrix4f p_253964_, boolean p_254201_) {
      MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
      int i = this.drawInBatch(p_253879_, p_254094_, p_254476_, p_254424_, p_254201_, p_253964_, multibuffersource$buffersource, false, 0, 15728880);
      multibuffersource$buffersource.endBatch();
      return i;
   }

   public int drawInBatch(String p_254471_, float p_253772_, float p_254307_, int p_254485_, boolean p_253711_, Matrix4f p_253933_, MultiBufferSource p_254054_, boolean p_254550_, int p_253707_, int p_254557_) {
      return this.drawInBatch(p_254471_, p_253772_, p_254307_, p_254485_, p_253711_, p_253933_, p_254054_, p_254550_, p_253707_, p_254557_, this.isBidirectional());
   }

   public int drawInBatch(String p_253974_, float p_253825_, float p_254295_, int p_254570_, boolean p_253977_, Matrix4f p_254299_, MultiBufferSource p_253904_, boolean p_254255_, int p_253819_, int p_254055_, boolean p_254383_) {
      return this.drawInternal(p_253974_, p_253825_, p_254295_, p_254570_, p_253977_, p_254299_, p_253904_, p_254255_, p_253819_, p_254055_, p_254383_);
   }

   public int drawInBatch(Component p_253635_, float p_253857_, float p_253765_, int p_254057_, boolean p_253617_, Matrix4f p_254371_, MultiBufferSource p_254194_, boolean p_253802_, int p_254399_, int p_254208_) {
      return this.drawInBatch(p_253635_.getVisualOrderText(), p_253857_, p_253765_, p_254057_, p_253617_, p_254371_, p_254194_, p_253802_, p_254399_, p_254208_);
   }

   public int drawInBatch(FormattedCharSequence p_253999_, float p_254380_, float p_254273_, int p_253824_, boolean p_253968_, Matrix4f p_253820_, MultiBufferSource p_253785_, boolean p_253815_, int p_254414_, int p_254065_) {
      return this.drawInternal(p_253999_, p_254380_, p_254273_, p_253824_, p_253968_, p_253820_, p_253785_, p_253815_, p_254414_, p_254065_);
   }

   public void drawInBatch8xOutline(FormattedCharSequence p_168646_, float p_168647_, float p_168648_, int p_168649_, int p_168650_, Matrix4f p_254170_, MultiBufferSource p_168652_, int p_168653_) {
      int i = adjustColor(p_168650_);
      Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(p_168652_, 0.0F, 0.0F, i, false, p_254170_, Font.DisplayMode.NORMAL, p_168653_);

      for(int j = -1; j <= 1; ++j) {
         for(int k = -1; k <= 1; ++k) {
            if (j != 0 || k != 0) {
               float[] afloat = new float[]{p_168647_};
               int l = j;
               int i1 = k;
               p_168646_.accept((p_168661_, p_168662_, p_168663_) -> {
                  boolean flag = p_168662_.isBold();
                  FontSet fontset = this.getFontSet(p_168662_.getFont());
                  GlyphInfo glyphinfo = fontset.getGlyphInfo(p_168663_, this.filterFishyGlyphs);
                  font$stringrenderoutput.x = afloat[0] + (float)l * glyphinfo.getShadowOffset();
                  font$stringrenderoutput.y = p_168648_ + (float)i1 * glyphinfo.getShadowOffset();
                  afloat[0] += glyphinfo.getAdvance(flag);
                  return font$stringrenderoutput.accept(p_168661_, p_168662_.withColor(i), p_168663_);
               });
            }
         }
      }

      Font.StringRenderOutput font$stringrenderoutput1 = new Font.StringRenderOutput(p_168652_, p_168647_, p_168648_, adjustColor(p_168649_), false, p_254170_, Font.DisplayMode.POLYGON_OFFSET, p_168653_);
      p_168646_.accept(font$stringrenderoutput1);
      font$stringrenderoutput1.finish(0, p_168647_);
   }

   private static int adjustColor(int p_92720_) {
      return (p_92720_ & -67108864) == 0 ? p_92720_ | -16777216 : p_92720_;
   }

   private int drawInternal(String p_254506_, float p_253981_, float p_254366_, int p_254180_, boolean p_254142_, Matrix4f p_254236_, MultiBufferSource p_253724_, boolean p_254305_, int p_254008_, int p_254344_, boolean p_253878_) {
      if (p_253878_) {
         p_254506_ = this.bidirectionalShaping(p_254506_);
      }

      p_254180_ = adjustColor(p_254180_);
      Matrix4f matrix4f = new Matrix4f(p_254236_);
      if (p_254142_) {
         this.renderText(p_254506_, p_253981_, p_254366_, p_254180_, true, p_254236_, p_253724_, p_254305_, p_254008_, p_254344_);
         matrix4f.translate(SHADOW_OFFSET);
      }

      p_253981_ = this.renderText(p_254506_, p_253981_, p_254366_, p_254180_, false, matrix4f, p_253724_, p_254305_, p_254008_, p_254344_);
      return (int)p_253981_ + (p_254142_ ? 1 : 0);
   }

   private int drawInternal(FormattedCharSequence p_254116_, float p_253739_, float p_254328_, int p_253965_, boolean p_254144_, Matrix4f p_254539_, MultiBufferSource p_253987_, boolean p_254306_, int p_254337_, int p_253932_) {
      p_253965_ = adjustColor(p_253965_);
      Matrix4f matrix4f = new Matrix4f(p_254539_);
      if (p_254144_) {
         this.renderText(p_254116_, p_253739_, p_254328_, p_253965_, true, p_254539_, p_253987_, p_254306_, p_254337_, p_253932_);
         matrix4f.translate(SHADOW_OFFSET);
      }

      p_253739_ = this.renderText(p_254116_, p_253739_, p_254328_, p_253965_, false, matrix4f, p_253987_, p_254306_, p_254337_, p_253932_);
      return (int)p_253739_ + (p_254144_ ? 1 : 0);
   }

   private float renderText(String p_253621_, float p_254326_, float p_254139_, int p_254332_, boolean p_254523_, Matrix4f p_254195_, MultiBufferSource p_254524_, boolean p_253751_, int p_253921_, int p_253648_) {
      Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(p_254524_, p_254326_, p_254139_, p_254332_, p_254523_, p_254195_, p_253751_, p_253648_);
      StringDecomposer.iterateFormatted(p_253621_, Style.EMPTY, font$stringrenderoutput);
      return font$stringrenderoutput.finish(p_253921_, p_254326_);
   }

   private float renderText(FormattedCharSequence p_253728_, float p_253634_, float p_254551_, int p_254369_, boolean p_254446_, Matrix4f p_253807_, MultiBufferSource p_253975_, boolean p_253685_, int p_253959_, int p_253723_) {
      Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(p_253975_, p_253634_, p_254551_, p_254369_, p_254446_, p_253807_, p_253685_, p_253723_);
      p_253728_.accept(font$stringrenderoutput);
      return font$stringrenderoutput.finish(p_253959_, p_253634_);
   }

   void renderChar(BakedGlyph p_254105_, boolean p_254001_, boolean p_254262_, float p_254256_, float p_253753_, float p_253629_, Matrix4f p_254014_, VertexConsumer p_253852_, float p_254317_, float p_253809_, float p_253870_, float p_254287_, int p_253905_) {
      p_254105_.render(p_254262_, p_253753_, p_253629_, p_254014_, p_253852_, p_254317_, p_253809_, p_253870_, p_254287_, p_253905_);
      if (p_254001_) {
         p_254105_.render(p_254262_, p_253753_ + p_254256_, p_253629_, p_254014_, p_253852_, p_254317_, p_253809_, p_253870_, p_254287_, p_253905_);
      }

   }

   public int width(String p_92896_) {
      return Mth.ceil(this.splitter.stringWidth(p_92896_));
   }

   public int width(FormattedText p_92853_) {
      return Mth.ceil(this.splitter.stringWidth(p_92853_));
   }

   public int width(FormattedCharSequence p_92725_) {
      return Mth.ceil(this.splitter.stringWidth(p_92725_));
   }

   public String plainSubstrByWidth(String p_92838_, int p_92839_, boolean p_92840_) {
      return p_92840_ ? this.splitter.plainTailByWidth(p_92838_, p_92839_, Style.EMPTY) : this.splitter.plainHeadByWidth(p_92838_, p_92839_, Style.EMPTY);
   }

   public String plainSubstrByWidth(String p_92835_, int p_92836_) {
      return this.splitter.plainHeadByWidth(p_92835_, p_92836_, Style.EMPTY);
   }

   public FormattedText substrByWidth(FormattedText p_92855_, int p_92856_) {
      return this.splitter.headByWidth(p_92855_, p_92856_, Style.EMPTY);
   }

   public void drawWordWrap(FormattedText p_92858_, int p_92859_, int p_92860_, int p_92861_, int p_92862_) {
      Matrix4f matrix4f = Transformation.identity().getMatrix();

      for(FormattedCharSequence formattedcharsequence : this.split(p_92858_, p_92861_)) {
         this.drawInternal(formattedcharsequence, (float)p_92859_, (float)p_92860_, p_92862_, matrix4f, false);
         p_92860_ += 9;
      }

   }

   public int wordWrapHeight(String p_92921_, int p_92922_) {
      return 9 * this.splitter.splitLines(p_92921_, p_92922_, Style.EMPTY).size();
   }

   public int wordWrapHeight(FormattedText p_239134_, int p_239135_) {
      return 9 * this.splitter.splitLines(p_239134_, p_239135_, Style.EMPTY).size();
   }

   public List<FormattedCharSequence> split(FormattedText p_92924_, int p_92925_) {
      return Language.getInstance().getVisualOrder(this.splitter.splitLines(p_92924_, p_92925_, Style.EMPTY));
   }

   public boolean isBidirectional() {
      return Language.getInstance().isDefaultRightToLeft();
   }

   public StringSplitter getSplitter() {
      return this.splitter;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum DisplayMode {
      NORMAL,
      SEE_THROUGH,
      POLYGON_OFFSET;
   }

   @OnlyIn(Dist.CLIENT)
   class StringRenderOutput implements FormattedCharSink {
      final MultiBufferSource bufferSource;
      private final boolean dropShadow;
      private final float dimFactor;
      private final float r;
      private final float g;
      private final float b;
      private final float a;
      private final Matrix4f pose;
      private final Font.DisplayMode mode;
      private final int packedLightCoords;
      float x;
      float y;
      @Nullable
      private List<BakedGlyph.Effect> effects;

      private void addEffect(BakedGlyph.Effect p_92965_) {
         if (this.effects == null) {
            this.effects = Lists.newArrayList();
         }

         this.effects.add(p_92965_);
      }

      public StringRenderOutput(MultiBufferSource p_254320_, float p_253717_, float p_254007_, int p_254225_, boolean p_253676_, Matrix4f p_253797_, boolean p_254512_, int p_253792_) {
         this(p_254320_, p_253717_, p_254007_, p_254225_, p_253676_, p_253797_, p_254512_ ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, p_253792_);
      }

      public StringRenderOutput(MultiBufferSource p_181365_, float p_181366_, float p_181367_, int p_181368_, boolean p_181369_, Matrix4f p_254510_, Font.DisplayMode p_181371_, int p_181372_) {
         this.bufferSource = p_181365_;
         this.x = p_181366_;
         this.y = p_181367_;
         this.dropShadow = p_181369_;
         this.dimFactor = p_181369_ ? 0.25F : 1.0F;
         this.r = (float)(p_181368_ >> 16 & 255) / 255.0F * this.dimFactor;
         this.g = (float)(p_181368_ >> 8 & 255) / 255.0F * this.dimFactor;
         this.b = (float)(p_181368_ & 255) / 255.0F * this.dimFactor;
         this.a = (float)(p_181368_ >> 24 & 255) / 255.0F;
         this.pose = p_254510_;
         this.mode = p_181371_;
         this.packedLightCoords = p_181372_;
      }

      public boolean accept(int p_92967_, Style p_92968_, int p_92969_) {
         FontSet fontset = Font.this.getFontSet(p_92968_.getFont());
         GlyphInfo glyphinfo = fontset.getGlyphInfo(p_92969_, Font.this.filterFishyGlyphs);
         BakedGlyph bakedglyph = p_92968_.isObfuscated() && p_92969_ != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(p_92969_);
         boolean flag = p_92968_.isBold();
         float f3 = this.a;
         TextColor textcolor = p_92968_.getColor();
         float f;
         float f1;
         float f2;
         if (textcolor != null) {
            int i = textcolor.getValue();
            f = (float)(i >> 16 & 255) / 255.0F * this.dimFactor;
            f1 = (float)(i >> 8 & 255) / 255.0F * this.dimFactor;
            f2 = (float)(i & 255) / 255.0F * this.dimFactor;
         } else {
            f = this.r;
            f1 = this.g;
            f2 = this.b;
         }

         if (!(bakedglyph instanceof EmptyGlyph)) {
            float f5 = flag ? glyphinfo.getBoldOffset() : 0.0F;
            float f4 = this.dropShadow ? glyphinfo.getShadowOffset() : 0.0F;
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));
            Font.this.renderChar(bakedglyph, flag, p_92968_.isItalic(), f5, this.x + f4, this.y + f4, this.pose, vertexconsumer, f, f1, f2, f3, this.packedLightCoords);
         }

         float f6 = glyphinfo.getAdvance(flag);
         float f7 = this.dropShadow ? 1.0F : 0.0F;
         if (p_92968_.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
         }

         if (p_92968_.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, f3));
         }

         this.x += f6;
         return true;
      }

      public float finish(int p_92962_, float p_92963_) {
         if (p_92962_ != 0) {
            float f = (float)(p_92962_ >> 24 & 255) / 255.0F;
            float f1 = (float)(p_92962_ >> 16 & 255) / 255.0F;
            float f2 = (float)(p_92962_ >> 8 & 255) / 255.0F;
            float f3 = (float)(p_92962_ & 255) / 255.0F;
            this.addEffect(new BakedGlyph.Effect(p_92963_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
         }

         if (this.effects != null) {
            BakedGlyph bakedglyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));

            for(BakedGlyph.Effect bakedglyph$effect : this.effects) {
               bakedglyph.renderEffect(bakedglyph$effect, this.pose, vertexconsumer, this.packedLightCoords);
            }
         }

         return this.x;
      }
   }
}