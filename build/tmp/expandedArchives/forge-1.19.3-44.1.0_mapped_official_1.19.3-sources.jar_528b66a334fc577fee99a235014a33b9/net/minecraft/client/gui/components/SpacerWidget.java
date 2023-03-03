package net.minecraft.client.gui.components;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpacerWidget extends AbstractWidget {
   public SpacerWidget(int p_254230_, int p_254000_) {
      this(0, 0, p_254230_, p_254000_);
   }

   public SpacerWidget(int p_254564_, int p_254131_, int p_254068_, int p_253628_) {
      super(p_254564_, p_254131_, p_254068_, p_253628_, Component.empty());
   }

   public void updateWidgetNarration(NarrationElementOutput p_254188_) {
   }

   public boolean changeFocus(boolean p_262103_) {
      return false;
   }

   public static AbstractWidget width(int p_254322_) {
      return new SpacerWidget(p_254322_, 0);
   }

   public static AbstractWidget height(int p_254563_) {
      return new SpacerWidget(0, p_254563_);
   }
}