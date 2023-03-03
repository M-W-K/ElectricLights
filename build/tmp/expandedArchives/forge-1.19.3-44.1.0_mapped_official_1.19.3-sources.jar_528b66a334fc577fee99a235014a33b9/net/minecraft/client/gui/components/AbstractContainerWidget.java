package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerEventHandler {
   @Nullable
   private GuiEventListener focused;
   private boolean dragging;

   public AbstractContainerWidget(int p_254009_, int p_254384_, int p_254479_, int p_254140_, Component p_254161_) {
      super(p_254009_, p_254384_, p_254479_, p_254140_, p_254161_);
   }

   public void renderButton(PoseStack p_254455_, int p_254289_, int p_253627_, float p_254198_) {
      for(AbstractWidget abstractwidget : this.getContainedChildren()) {
         abstractwidget.render(p_254455_, p_254289_, p_253627_, p_254198_);
      }

   }

   public boolean isMouseOver(double p_253720_, double p_254125_) {
      for(AbstractWidget abstractwidget : this.getContainedChildren()) {
         if (abstractwidget.isMouseOver(p_253720_, p_254125_)) {
            return true;
         }
      }

      return false;
   }

   public void mouseMoved(double p_253612_, double p_253828_) {
      this.getContainedChildren().forEach((p_253796_) -> {
         p_253796_.mouseMoved(p_253612_, p_253828_);
      });
   }

   public List<? extends GuiEventListener> children() {
      return this.getContainedChildren();
   }

   protected abstract List<? extends AbstractWidget> getContainedChildren();

   public boolean isDragging() {
      return this.dragging;
   }

   public void setDragging(boolean p_254253_) {
      this.dragging = p_254253_;
   }

   public boolean mouseScrolled(double p_253702_, double p_254077_, double p_254162_) {
      boolean flag = false;

      for(AbstractWidget abstractwidget : this.getContainedChildren()) {
         if (abstractwidget.isMouseOver(p_253702_, p_254077_) && abstractwidget.mouseScrolled(p_253702_, p_254077_, p_254162_)) {
            flag = true;
         }
      }

      return flag || super.mouseScrolled(p_253702_, p_254077_, p_254162_);
   }

   public boolean changeFocus(boolean p_254016_) {
      return ContainerEventHandler.super.changeFocus(p_254016_);
   }

   @Nullable
   protected GuiEventListener getHovered() {
      for(AbstractWidget abstractwidget : this.getContainedChildren()) {
         if (abstractwidget.isHovered) {
            return abstractwidget;
         }
      }

      return null;
   }

   @Nullable
   public GuiEventListener getFocused() {
      return this.focused;
   }

   public void setFocused(@Nullable GuiEventListener p_254365_) {
      this.focused = p_254365_;
   }

   public void updateWidgetNarration(NarrationElementOutput p_253937_) {
      GuiEventListener guieventlistener = this.getHovered();
      if (guieventlistener != null) {
         if (guieventlistener instanceof NarrationSupplier) {
            NarrationSupplier narrationsupplier = (NarrationSupplier)guieventlistener;
            narrationsupplier.updateNarration(p_253937_.nest());
         }
      } else {
         GuiEventListener guieventlistener1 = this.getFocused();
         if (guieventlistener1 != null && guieventlistener1 instanceof NarrationSupplier) {
            NarrationSupplier narrationsupplier1 = (NarrationSupplier)guieventlistener1;
            narrationsupplier1.updateNarration(p_253937_.nest());
         }
      }

   }

   public NarratableEntry.NarrationPriority narrationPriority() {
      if (!this.isHovered && this.getHovered() == null) {
         return this.focused != null ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
      } else {
         return NarratableEntry.NarrationPriority.HOVERED;
      }
   }

   public void setX(int p_253732_) {
      for(AbstractWidget abstractwidget : this.getContainedChildren()) {
         int i = abstractwidget.getX() + (p_253732_ - this.getX());
         abstractwidget.setX(i);
      }

      super.setX(p_253732_);
   }

   public void setY(int p_253703_) {
      for(AbstractWidget abstractwidget : this.getContainedChildren()) {
         int i = abstractwidget.getY() + (p_253703_ - this.getY());
         abstractwidget.setY(i);
      }

      super.setY(p_253703_);
   }

   public Optional<GuiEventListener> getChildAt(double p_254552_, double p_253874_) {
      return ContainerEventHandler.super.getChildAt(p_254552_, p_253874_);
   }

   public boolean mouseClicked(double p_253616_, double p_254012_, int p_254048_) {
      return ContainerEventHandler.super.mouseClicked(p_253616_, p_254012_, p_254048_);
   }

   public boolean mouseReleased(double p_253760_, double p_253738_, int p_254511_) {
      return ContainerEventHandler.super.mouseReleased(p_253760_, p_253738_, p_254511_);
   }

   public boolean mouseDragged(double p_253786_, double p_254228_, int p_254486_, double p_254182_, double p_254329_) {
      return ContainerEventHandler.super.mouseDragged(p_253786_, p_254228_, p_254486_, p_254182_, p_254329_);
   }

   @OnlyIn(Dist.CLIENT)
   protected abstract static class AbstractChildWrapper {
      public final AbstractWidget child;
      public final LayoutSettings.LayoutSettingsImpl layoutSettings;

      protected AbstractChildWrapper(AbstractWidget p_254351_, LayoutSettings p_253670_) {
         this.child = p_254351_;
         this.layoutSettings = p_253670_.getExposed();
      }

      public int getHeight() {
         return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
      }

      public int getWidth() {
         return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
      }

      public void setX(int p_253789_, int p_253842_) {
         float f = (float)this.layoutSettings.paddingLeft;
         float f1 = (float)(p_253842_ - this.child.getWidth() - this.layoutSettings.paddingRight);
         int i = (int)Mth.lerp(this.layoutSettings.xAlignment, f, f1);
         this.child.setX(i + p_253789_);
      }

      public void setY(int p_253610_, int p_254525_) {
         float f = (float)this.layoutSettings.paddingTop;
         float f1 = (float)(p_254525_ - this.child.getHeight() - this.layoutSettings.paddingBottom);
         int i = (int)Mth.lerp(this.layoutSettings.yAlignment, f, f1);
         this.child.setY(i + p_253610_);
      }
   }
}