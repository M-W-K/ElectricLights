package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CenteredStringWidget extends AbstractWidget {
   private int color = 16777215;
   private final Font font;

   public CenteredStringWidget(Component p_253700_, Font p_253680_) {
      this(0, 0, p_253680_.width(p_253700_.getVisualOrderText()), 9, p_253700_, p_253680_);
   }

   public CenteredStringWidget(int p_254411_, int p_253849_, Component p_253990_, Font p_254312_) {
      this(0, 0, p_254411_, p_253849_, p_253990_, p_254312_);
   }

   public CenteredStringWidget(int p_254032_, int p_254061_, int p_253972_, int p_254158_, Component p_253696_, Font p_253991_) {
      super(p_254032_, p_254061_, p_253972_, p_254158_, p_253696_);
      this.font = p_253991_;
      this.active = false;
   }

   public CenteredStringWidget color(int p_254088_) {
      this.color = p_254088_;
      return this;
   }

   public void updateWidgetNarration(NarrationElementOutput p_253688_) {
   }

   public void renderButton(PoseStack p_254045_, int p_253926_, int p_253645_, float p_253784_) {
      drawCenteredString(p_254045_, this.font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 9) / 2, this.color);
   }
}