package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiLineTextWidget extends AbstractWidget {
   private final MultiLineLabel multiLineLabel;
   private final int lineHeight;
   private final boolean centered;

   private MultiLineTextWidget(MultiLineLabel p_261977_, Font p_262183_, Component p_261676_, boolean p_262069_) {
      super(0, 0, p_261977_.getWidth(), p_261977_.getLineCount() * 9, p_261676_);
      this.multiLineLabel = p_261977_;
      this.lineHeight = 9;
      this.centered = p_262069_;
      this.active = false;
   }

   public static MultiLineTextWidget createCentered(int p_261960_, Font p_261715_, Component p_262061_) {
      MultiLineLabel multilinelabel = MultiLineLabel.create(p_261715_, p_262061_, p_261960_);
      return new MultiLineTextWidget(multilinelabel, p_261715_, p_262061_, true);
   }

   public static MultiLineTextWidget create(int p_261657_, Font p_261723_, Component p_261933_) {
      MultiLineLabel multilinelabel = MultiLineLabel.create(p_261723_, p_261933_, p_261657_);
      return new MultiLineTextWidget(multilinelabel, p_261723_, p_261933_, false);
   }

   protected void updateWidgetNarration(NarrationElementOutput p_261618_) {
   }

   public void renderButton(PoseStack p_261473_, int p_261774_, int p_261640_, float p_261514_) {
      if (this.centered) {
         this.multiLineLabel.renderCentered(p_261473_, this.getX() + this.getWidth() / 2, this.getY(), this.lineHeight, 16777215);
      } else {
         this.multiLineLabel.renderLeftAligned(p_261473_, this.getX(), this.getY(), this.lineHeight, 16777215);
      }

   }
}