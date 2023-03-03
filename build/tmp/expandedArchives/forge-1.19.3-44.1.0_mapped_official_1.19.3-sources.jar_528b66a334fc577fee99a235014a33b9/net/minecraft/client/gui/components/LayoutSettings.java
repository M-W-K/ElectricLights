package net.minecraft.client.gui.components;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LayoutSettings {
   LayoutSettings padding(int p_254453_);

   LayoutSettings padding(int p_254359_, int p_253915_);

   LayoutSettings padding(int p_254280_, int p_254433_, int p_254281_, int p_254429_);

   LayoutSettings paddingLeft(int p_254387_);

   LayoutSettings paddingTop(int p_253887_);

   LayoutSettings paddingRight(int p_253832_);

   LayoutSettings paddingBottom(int p_253769_);

   LayoutSettings paddingHorizontal(int p_254346_);

   LayoutSettings paddingVertical(int p_253665_);

   LayoutSettings align(float p_253636_, float p_253644_);

   LayoutSettings alignHorizontally(float p_253733_);

   LayoutSettings alignVertically(float p_253889_);

   default LayoutSettings alignHorizontallyLeft() {
      return this.alignHorizontally(0.0F);
   }

   default LayoutSettings alignHorizontallyCenter() {
      return this.alignHorizontally(0.5F);
   }

   default LayoutSettings alignHorizontallyRight() {
      return this.alignHorizontally(1.0F);
   }

   default LayoutSettings alignVerticallyTop() {
      return this.alignVertically(0.0F);
   }

   default LayoutSettings alignVerticallyMiddle() {
      return this.alignVertically(0.5F);
   }

   default LayoutSettings alignVerticallyBottom() {
      return this.alignVertically(1.0F);
   }

   LayoutSettings copy();

   LayoutSettings.LayoutSettingsImpl getExposed();

   static LayoutSettings defaults() {
      return new LayoutSettings.LayoutSettingsImpl();
   }

   @OnlyIn(Dist.CLIENT)
   public static class LayoutSettingsImpl implements LayoutSettings {
      public int paddingLeft;
      public int paddingTop;
      public int paddingRight;
      public int paddingBottom;
      public float xAlignment;
      public float yAlignment;

      public LayoutSettingsImpl() {
      }

      public LayoutSettingsImpl(LayoutSettings.LayoutSettingsImpl p_253774_) {
         this.paddingLeft = p_253774_.paddingLeft;
         this.paddingTop = p_253774_.paddingTop;
         this.paddingRight = p_253774_.paddingRight;
         this.paddingBottom = p_253774_.paddingBottom;
         this.xAlignment = p_253774_.xAlignment;
         this.yAlignment = p_253774_.yAlignment;
      }

      public LayoutSettings.LayoutSettingsImpl padding(int p_254565_) {
         return this.padding(p_254565_, p_254565_);
      }

      public LayoutSettings.LayoutSettingsImpl padding(int p_253623_, int p_254049_) {
         return this.paddingHorizontal(p_253623_).paddingVertical(p_254049_);
      }

      public LayoutSettings.LayoutSettingsImpl padding(int p_254562_, int p_253631_, int p_254232_, int p_254447_) {
         return this.paddingLeft(p_254562_).paddingRight(p_254232_).paddingTop(p_253631_).paddingBottom(p_254447_);
      }

      public LayoutSettings.LayoutSettingsImpl paddingLeft(int p_254043_) {
         this.paddingLeft = p_254043_;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingTop(int p_253613_) {
         this.paddingTop = p_253613_;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingRight(int p_254534_) {
         this.paddingRight = p_254534_;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingBottom(int p_253856_) {
         this.paddingBottom = p_253856_;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingHorizontal(int p_254235_) {
         return this.paddingLeft(p_254235_).paddingRight(p_254235_);
      }

      public LayoutSettings.LayoutSettingsImpl paddingVertical(int p_254267_) {
         return this.paddingTop(p_254267_).paddingBottom(p_254267_);
      }

      public LayoutSettings.LayoutSettingsImpl align(float p_253978_, float p_254141_) {
         this.xAlignment = p_253978_;
         this.yAlignment = p_254141_;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl alignHorizontally(float p_254209_) {
         this.xAlignment = p_254209_;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl alignVertically(float p_254286_) {
         this.yAlignment = p_254286_;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl copy() {
         return new LayoutSettings.LayoutSettingsImpl(this);
      }

      public LayoutSettings.LayoutSettingsImpl getExposed() {
         return this;
      }
   }
}